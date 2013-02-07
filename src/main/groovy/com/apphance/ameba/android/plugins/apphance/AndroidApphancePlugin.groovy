package com.apphance.ameba.android.plugins.apphance

import com.apphance.ameba.*
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import com.apphance.ameba.android.plugins.test.ApphanceNetworkHelper
import com.apphance.ameba.apphance.ApphanceProperty
import com.apphance.ameba.apphance.PrepareApphanceSetupOperation
import com.apphance.ameba.apphance.ShowApphancePropertiesOperation
import com.apphance.ameba.apphance.VerifyApphanceSetupOperation
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.util.Preconditions
import groovy.json.JsonSlurper
import org.apache.http.util.EntityUtils
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import static com.apphance.ameba.util.file.FileManager.MAX_RECURSION_LEVEL
import static groovy.io.FileType.FILES

/**
 * Adds Apphance in automated way.
 *
 */
@Mixin(Preconditions)
class AndroidApphancePlugin implements Plugin<Project> {

    static Logger l = Logging.getLogger(AndroidApphancePlugin.class)

    ProjectHelper projectHelper
    ProjectReleaseConfiguration releaseConfiguration
    ProjectConfiguration conf
    AndroidManifestHelper manifestHelper
    AndroidProjectConfiguration androidConf

    static final JAR_PATTERN = ~/.*android\.(pre\-)?production\-(\d+\.)+\d+\.jar/

    @Override
    public void apply(Project project) {
        PluginHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class)
        use(PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.releaseConfiguration = ProjectReleaseCategory.getProjectReleaseConfiguration(project)
            this.conf = project.getProjectConfiguration()
            this.manifestHelper = new AndroidManifestHelper()
            this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)

            prepareApphanceJarVersion(project)
            preProcessBuildsWithApphance(project)
            prepareConvertLogsToApphance(project)
            prepareConvertLogsToAndroid(project)
            prepareRemoveApphanceFromManifest(project)
            project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
            project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
        }
    }

    private void prepareApphanceJarVersion(Project project) {
        project.configurations {
            apphance
        }

        project.configurations.apphance {
            resolutionStrategy.cacheDynamicVersionsFor 0, 'minutes'
        }

        project.repositories {
            maven { url 'https://dev.polidea.pl/artifactory/libs-releases-local/' }
            maven { url 'https://dev.polidea.pl/artifactory/libs-snapshots-local/' }
        }
    }

    void prepareConvertLogsToApphance(project) {
        def task = project.task('convertLogsToApphance')
        task.description = "Converts all logs to apphance from android logs for the source project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << { replaceLogsWithApphance(project.rootDir, project.ant) }
    }

    void prepareConvertLogsToAndroid(project) {
        def task = project.task('convertLogsToAndroid')
        task.description = "Converts all logs to android from apphance logs for the source project"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << { replaceLogsWithAndroid(project.rootDir, project.ant) }
    }

    void prepareRemoveApphanceFromManifest(project) {
        def task = project.task('removeApphanceFromManifest')
        task.description = "Remove apphance-only entries from manifest"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << {
            androidConf.variants.each { variant ->
                if (androidConf.debugRelease[variant] == 'Release') {
                    removeApphanceFromManifest(project, variant)
                }
            }
        }
    }

    private static String EVENT_LOG_WIDGET_PACKAGE = "com.apphance.android.eventlog.widget"
    private static String EVENT_LOG_ACTIVITY_PACKAGE = "com.apphance.android.eventlog.activity"

    private void replaceViewsWithApphance(File directory, AntBuilder ant) {
        l.lifecycle("Replacing android views with apphance loggable versions for ${directory}")
        replaceViewWithApphance(directory, "Button", ant)
        replaceViewWithApphance(directory, "CheckBox", ant)
        replaceViewWithApphance(directory, "EditText", ant)
        replaceViewWithApphance(directory, "ImageButton", ant)
        replaceViewWithApphance(directory, "ListView", ant)
        replaceViewWithApphance(directory, "RadioGroup", ant)
        replaceViewWithApphance(directory, "SeekBar", ant)
        replaceViewWithApphance(directory, "Spinner", ant)
        replaceViewWithApphance(directory, "TextView", ant)

        replaceActivityWithApphance(directory, "Activity", ant)
        replaceActivityWithApphance(directory, "ActivityGroup", ant)
    }

    private void replaceActivityWithApphance(File directory, String activityName, AntBuilder ant) {
        replaceActivityExtendsWithApphance(directory, activityName, ant)
    }

    private void replaceViewWithApphance(File directory, String viewName, AntBuilder ant) {
        replaceViewExtendsWithApphance(directory, viewName, ant);
        replaceTagResourcesOpeningTag(directory, viewName, EVENT_LOG_WIDGET_PACKAGE + "." + viewName, ant);
        replaceTagResourcesClosingTag(directory, viewName, EVENT_LOG_WIDGET_PACKAGE + "." + viewName, ant);
    }

    private void replaceActivityExtendsWithApphance(File directory, String activityName, AntBuilder ant) {
        String newClassName = EVENT_LOG_ACTIVITY_PACKAGE + "." + activityName
        l.info("Replacing extends with Apphance for ${activityName} to ${newClassName}")
        ant.replace(casesensitive: 'true', token: "extends ${activityName} ",
                value: "extends ${newClassName} ", summary: true) {
            fileset(dir: new File(directory, 'src')) { include(name: '**/*.java') }
        }
    }


    private void replaceViewExtendsWithApphance(File directory, String viewName, AntBuilder ant) {
        String newClassName = EVENT_LOG_WIDGET_PACKAGE + "." + viewName
        l.info("Replacing extends with Apphance for ${viewName} to ${newClassName}")
        ant.replace(casesensitive: 'true', token: "extends ${viewName} ",
                value: "extends ${newClassName} ", summary: true) {
            fileset(dir: new File(directory, 'src')) { include(name: '**/*.java') }
        }
    }

    private void replaceTagResourcesOpeningTag(File directory, String tagName, String replacement, AntBuilder ant) {
        l.info("Replacing tag resources with Apphance for ${tagName} to ${replacement}")
        ant.replace(casesensitive: 'true', token: "<${tagName} ",
                value: "<${replacement} ", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replaceregexp(flags: 'gm') {
            regexp(pattern: "<${tagName}(\\s*)")
            substitution(expression: "<${replacement}\\1")
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replace(casesensitive: 'true', token: "<${tagName}>",
                value: "<${replacement}>", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
    }

    private void replaceTagResourcesClosingTag(File directory, String tagName, String replacement, AntBuilder ant) {
        l.info("Replacing tag resources with Apphance for ${tagName} to ${replacement}")
        ant.replace(casesensitive: 'true', token: "</${tagName} ",
                value: "</${replacement} ", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replaceregexp(flags: 'gm') {
            regexp(pattern: "</${tagName}(\\s*)")
            substitution(expression: "</${replacement}\\1")
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
        ant.replace(casesensitive: 'true', token: "</${tagName}>",
                value: "</${replacement}>", summary: true) {
            fileset(dir: new File(directory, 'res/layout')) { include(name: '**/*.xml') }
        }
    }


    def getMainApplicationFile(File directory) {
        String mainApplicationFileName = manifestHelper.getApplicationName(directory)
        mainApplicationFileName = mainApplicationFileName.replace('.', '/')
        mainApplicationFileName = mainApplicationFileName + '.java'
        mainApplicationFileName = 'src/' + mainApplicationFileName
        File f
        if (new File(directory, mainApplicationFileName).exists()) {
            f = new File(directory, mainApplicationFileName)
            return [f, false]
        } else {
            String mainActivityName = manifestHelper.getMainActivityName(directory)
            mainActivityName = mainActivityName.replace('.', '/')
            mainActivityName = mainActivityName + '.java'
            mainActivityName = 'src/' + mainActivityName
            if (!(new File(directory, mainActivityName)).exists()) {
                f = null
            } else {
                f = new File(directory, mainActivityName)
            }
            return [f, true]
        }
    }

    public void preProcessBuildsWithApphance(Project project) {
        use(PropertyCategory) {
            def uploadTasksToAdd = [:]
            androidConf.buildableVariants.each { variant ->
                File dir = getVariantDir(project, variant)
                String apphanceMode = apphanceMode(project)
                if (androidConf.debugRelease.get(variant) == 'Debug') {
                    def task = project["buildDebug-${variant}"]
                    task.doFirst {
                        addApphanceToProject(dir,
                                project.ant,
                                project[ApphanceProperty.APPLICATION_KEY.propertyName],
                                apphanceMode,
                                project[ApphanceProperty.APPHANCE_LOG_EVENTS.propertyName].equals("true"),
                                project)
                    }
                    uploadTasksToAdd.put(task.name, variant)
                } else {
                    def task = project["buildRelease-${variant}"]
                    task.doFirst {
                        removeApphanceFromManifest(dir)
                        replaceLogsWithAndroid(dir, project.ant)
                    }
                }
            }
            uploadTasksToAdd.each { key, value ->
                prepareSingleBuildUpload(project, value, project."${key}")
            }
        }
    }

    private File getVariantDir(Project project, String variant) {
        variant != null ? androidConf.tmpDirs[variant] : project.rootDir
    }

    private addApphanceToProject(File directory,
                                 AntBuilder ant,
                                 String appKey,
                                 String apphanceMode,
                                 boolean logEvents,
                                 Project project) {
        if (!checkIfApphancePresent(directory)) {
            l.lifecycle("Apphance not found in project: ${directory}")
            File mainFile
            boolean isActivity
            (mainFile, isActivity) = getMainApplicationFile(directory)
            if (mainFile != null) {
                replaceLogsWithApphance(directory, ant)
                if (logEvents) {
                    replaceViewsWithApphance(directory, ant)
                }
                addApphanceInit(directory, mainFile, appKey, apphanceMode, logEvents, isActivity)
                copyApphanceJar(directory, project)
                addApphanceToManifest(directory)
            }
        } else {
            l.lifecycle("Apphance found in project: ${directory}")
        }
    }

    private replaceLogsWithAndroid(File directory, AntBuilder ant) {
        l.lifecycle("Replacing apphance logs with android in ${directory}")
        ant.replace(casesensitive: 'true', token: 'import com.apphance.android.Log;',
                value: 'import android.util.Log;', summary: true) {
            fileset(dir: new File(directory, 'src')) { include(name: '**/*.java') }
        }
    }

    private replaceLogsWithApphance(File directory, AntBuilder ant) {
        l.lifecycle("Replacing Android logs with Apphance in ${directory}")
        ant.replace(casesensitive: 'true', token: 'import android.util.Log;',
                value: 'import com.apphance.android.Log;', summary: true) {
            fileset(dir: new File(directory, 'src')) { include(name: '**/*.java') }
        }
    }

    private removeApphanceFromManifest(File directory) {
        l.lifecycle("Remove apphance from manifest")
        manifestHelper.removeApphance(directory)
        File apphanceRemovedManifest = new File(conf.logDirectory, "AndroidManifest-with-apphance-removed.xml")
        l.lifecycle("Manifest used for this build is stored in ${apphanceRemovedManifest}")
        if (apphanceRemovedManifest.exists()) {
            l.lifecycle('removed manifest exists')
            apphanceRemovedManifest.delete()
        }
        apphanceRemovedManifest << new File(directory, "AndroidManifest.xml").text
    }

    private addApphanceToManifest(File directory) {
        l.lifecycle("Adding apphance to manifest")
        manifestHelper.addApphanceToManifest(directory)
    }


    private void addApphanceInitIfOnCreateMissing(String onCreateMethod, String importApphance, File mainFile, File newMainClassFile) {
        boolean onCreateAdded = false
        newMainClassFile.delete()
        newMainClassFile.withWriter { out ->
            mainFile.eachLine { line ->
                if (line.matches('.*class.*extends.*\\{.*') && !onCreateAdded) {
                    out.println(line << onCreateMethod)
                    onCreateAdded = true
                } else if (line.matches('package\\s*.*')) {
                    out.println(line << importApphance)
                } else {
                    out.println(line)
                }
            }
        }
    }

    private void addApphanceInitToExistingOnCreate(String startSession, String importApphance, File mainFile, File newMainClassFile) {
        boolean onCreateAdded = false
        boolean searchingForOpeningBrace = false
        newMainClassFile.withWriter { out ->
            mainFile.eachLine { line ->
                if (line.matches('.*void\\sonCreate\\(.*') && !onCreateAdded) {
                    searchingForOpeningBrace = true
                } else if (line.matches('package\\s*.*')) {
                    line = "${line} ${importApphance}"
                }
                if (!onCreateAdded && searchingForOpeningBrace && line.matches('.*\\{.*')) {
                    out.println(line.replaceAll('\\{', "{ ${startSession}"))
                    onCreateAdded = true
                } else {
                    out.println(line)
                }
            }
        }
        if (!onCreateAdded) {
            l.warn("Could not find onCreate(). Apphance not added.")
        }
    }

    private boolean isOnCreatePresent(File mainFile) {
        boolean present = false
        mainFile.eachLine { line, lineNumber ->
            if (line.matches('.*void.*onCreate\\(.*')) {
                present = true
            }
        }
        return present
    }

    String apphanceMode(Project project) {
        ((project.hasProperty(ApphanceProperty.APPHANCE_MODE.propertyName) && project[ApphanceProperty.APPHANCE_MODE.propertyName].equals("QA"))) ?
            "Apphance.Mode.QA"
        : "Apphance.Mode.Silent"
    }


    private def addApphanceInit(File directory, File mainFile, String appKey,
                                String apphanceMode, boolean logEvents, boolean isActivity) {
        l.lifecycle("Adding apphance init to file " + mainFile)
        File newMainClassFile = new File(directory, "newMainClassFile.java")
        String startSession = "Apphance.startNewSession(this, \"${appKey}\", ${apphanceMode});"
        if (logEvents) {
            startSession = startSession + "com.apphance.android.eventlog.EventLog.setInvertedIdMap(this);";
        }
        String importApphance = 'import com.apphance.android.Apphance;'
        boolean onCreatePresent = isOnCreatePresent(mainFile)
        if (onCreatePresent) {
            addApphanceInitToExistingOnCreate(startSession, importApphance, mainFile, newMainClassFile)
        } else {
            String onCreateMethod
            if (isActivity) {
                onCreateMethod = " public void onCreate(final Bundle savedInstanceState) { super.onCreate(savedInstanceState); ${startSession} } "
            } else {
                onCreateMethod = " public void onCreate() { super.onCreate(); ${startSession} } "
            }
            addApphanceInitIfOnCreateMissing(onCreateMethod, importApphance, mainFile, newMainClassFile)
        }
        mainFile.delete()
        mainFile << newMainClassFile.text
        newMainClassFile.delete()
    }

    private copyApphanceJar(File directory, Project project) {

        def apphanceLibDependency = prepareApphanceLibDependency(project)

        def libsDir = new File(directory, 'libs')
        libsDir.mkdirs()

        libsDir.eachFileMatch(JAR_PATTERN) {
            l.lifecycle("Removing old apphance jar: " + it.name)
            it.delete()
        }
        l.lifecycle("Copying apphance jar: to ${libsDir}")

        try {
            project.copy {
                from { project.configurations.apphance }
                into libsDir
            }
        } catch (e) {
            def msg = "Error while resolving dependency: '$apphanceLibDependency'"
            l.error("""$msg.
To solve the problem add correct dependency to gradle.properties file or add -Dapphance.lib=<apphance.lib> to invocation.
Dependency should be added in gradle style to 'apphance.lib' entry""")
            throw new GradleException(msg)
        }
    }

    def prepareApphanceLibDependency(Project p) {
        def apphanceLibDependency
        use(PropertyCategory) {
            apphanceLibDependency = p.readPropertyOrEnvironmentVariable('apphance.lib', true)
            apphanceLibDependency ?
                p.dependencies { apphance apphanceLibDependency } :
                p.dependencies { apphance 'com.apphance:android.pre-production:1.8+' }
        }
        apphanceLibDependency
    }

    public boolean checkIfApphancePresent(File directory) {
        boolean found = false
        directory.traverse([type: FILES, maxDepth: MAX_RECURSION_LEVEL]) { file ->
            if (file.name.endsWith('.java')) {
                file.eachLine {
                    if (it.contains("Apphance.startNewSession")) {
                        found = true
                    }
                }
            }
        }
        if (!found) {
            File libsDir = new File(directory, 'libs/')
            if (libsDir.exists()) {
                libsDir.eachFileMatch(JAR_PATTERN) { found = true }
            }
        }
        if (!found) {
            found = manifestHelper.isApphanceActivityPresent(directory)
        }
        if (!found) {
            found = manifestHelper.isApphanceInstrumentationPresent(directory)
        }
        return found
    }

    void prepareSingleBuildUpload(Project project, String variantName, buildTask) {

        def uploadTask = project.task("upload${variantName.toLowerCase().capitalize()}")

        uploadTask.description = "Uploads apk & image_montage to Apphance server"
        uploadTask.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE

        uploadTask << {

            def builder = new AndroidSingleVariantApkBuilder(project, androidConf)
            def builderInfo = builder.buildApkArtifactBuilderInfo(variantName, 'Debug')
            def releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)

            //TODO gradle.properties + validation
            String user = project['apphanceUserName']
            String pass = project['apphancePassword']
            //TODO gradle.properties + validation

            String key = project[ApphanceProperty.APPLICATION_KEY.propertyName]
            ApphanceNetworkHelper networkHelper = null

            try {
                networkHelper = new ApphanceNetworkHelper(user, pass)

                def response = networkHelper.updateArtifactQuery(key, conf.versionString, conf.versionCode, false, ['apk', 'image_montage'])
                l.lifecycle("Upload version query response: ${response.statusLine}")

                throwIfCondition(!response.entity, "Error while uploading version query, empty response received")

                def resp = new JsonSlurper().parseText(response.entity.content.text)

                response = networkHelper.uploadResource(builderInfo.originalFile, resp.update_urls.apk, 'apk')
                l.lifecycle("Upload apk response: ${response.statusLine}")
                EntityUtils.consume(response.entity)

                response = networkHelper.uploadResource(releaseConf.imageMontageFile.location, resp.update_urls.image_montage, 'image_montage')
                l.lifecycle("Upload image_montage response: ${response.statusLine}")
                EntityUtils.consume(response.entity)

            } catch (e) {
                def msg = "Error while uploading artifact to apphance: ${e.message}"
                l.error(msg)
                throw new GradleException(msg)
            } finally {
                networkHelper?.closeConnection()
            }
        }
        uploadTask.dependsOn(buildTask)
        uploadTask.dependsOn('prepareImageMontage')
    }

    static public final String DESCRIPTION =
        """This is the plugin that links Ameba with Apphance service.

The plugin provides integration with Apphance service. It performs the
following tasks: adding Apphance on-the-fly while building the application
(for all Debug builds), removing Apphance on-the-fly while building the application
(for all Release builds), submitting the application to apphance at release time.
"""
}
