package com.apphance.ameba.android.plugins.apphance

import java.io.File

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration;
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever;
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin

class AndroidApphancePlugin implements Plugin<Project>{

    static Logger logger = Logging.getLogger(AndroidApphancePlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    File findbugsHomeDir
    AndroidManifestHelper manifestHelper
    AndroidProjectConfiguration androidConf

    public void apply(Project project) {
        ProjectHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class)
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            manifestHelper = new AndroidManifestHelper()
            this.androidConf = new AndroidProjectConfigurationRetriever().getAndroidProjectConfiguration(project)
            preprocessBuildsWithApphance(project)
            prepareConvertLogsToApphance(project)
            prepareConvertLogsToAndroid(project)
            prepareRemoveApphaceFromManifest(project)
            prepareRestoreManifestBeforeApphance(project)
            project.prepareSetup.prepareSetupOperations << new PrepareApphanceSetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyApphanceSetupOperation()
            project.showSetup.showSetupOperations << new ShowApphancePropertiesOperation()
        }
    }

    void prepareConvertLogsToApphance(project) {
        def task = project.task('convertLogsToApphance')
        task.description = "Converts all logs to apphance from android logs for Debug builds"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << {
            androidConf.variants.each { variant ->
                if (androidConf.debugRelease[variant] == 'Debug') {
                    replaceLogsWithApphance(project, variant)
                }
            }
        }
    }

    void prepareConvertLogsToAndroid(project) {
        def task = project.task('convertLogsToAndroid')
        task.description = "Converts all logs to android from apphance logs for Release builds"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << {
            androidConf.variants.each { variant ->
                if (androidConf.debugRelease[variant] == 'Release') {
                    replaceLogsWithAndroid(project, variant)
                }
            }
        }
    }

    void prepareRemoveApphaceFromManifest(project) {
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

    void prepareRestoreManifestBeforeApphance(project) {
        def task = project.task('restoreManifestBeforeApphance')
        task.description = "Restore manifest to before apphance replacement"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << {
            androidConf.variants.each { variant ->
                if (androidConf.debugRelease[variant] == 'Release') {
                    restoreManifestBeforeApphanceRemoval(project, variant)
                }
            }
        }
    }

    private static String EVENT_LOG_PACKAGE = "com.apphance.android.eventlog.widget"

    private void replaceViewsWithApphance(Project project, String variant) {

        if (project[ApphanceProperty.APPHANCE_LOG_EVENTS.propertyName].equals("true")) {
            logger.lifecycle("Replacing android views with apphance loggable versions for ${variant}")
            replaceViewWithApphance(project, variant, "Button")
            replaceViewWithApphance(project, variant, "CheckBox")
            replaceViewWithApphance(project, variant, "EditText")
            replaceViewWithApphance(project, variant, "ImageButton")
            replaceViewWithApphance(project, variant, "ListView")
            replaceViewWithApphance(project, variant, "RadioGroup")
            replaceViewWithApphance(project, variant, "SeekBar")
            replaceViewWithApphance(project, variant, "Spinner")
            replaceViewWithApphance(project, variant, "TextView")
        }
    }

    private void replaceViewWithApphance(Project project, String variant, String viewName) {
        //invertRFile(project, variant, EVENT_LOG_PACKAGE);
        replaceViewExtendsWithApphance(project, variant, viewName);
        replaceTagResourcesOpeningTag(project, variant, viewName, EVENT_LOG_PACKAGE+"."+viewName);
        replaceTagResourcesClosingTag(project, variant, viewName, EVENT_LOG_PACKAGE+"."+viewName);

    }

    private void replaceViewExtendsWithApphance(Project project, String variant, String viewName) {
        project.ant.replace(casesensitive: 'true', token : 'extends '+viewName,
                        value: 'extends '+EVENT_LOG_PACKAGE+"."+viewName, summary: true) {
                            fileset(dir: new File(androidConf.tmpDirs[variant], 'src')) { include (name : '**/*.java') }
                        }
    }

    private void replaceTagResourcesOpeningTag(Project project, String variant, String tagName, String replacement) {
        project.ant.replace(casesensitive: 'true', token : '<'+tagName+" ",
                        value: '<'+replacement+" ", summary: true) {
                            fileset(dir: new File(androidConf.tmpDirs[variant], 'res/layout')) { include (name : '**/*.xml') }
                        }
    }

    private void replaceTagResourcesClosingTag(Project project, String variant, String tagName, String replacement) {
        project.ant.replace(casesensitive: 'true', token : '</'+tagName+" ",
                        value: '</'+replacement+" ", summary: true) {
                            fileset(dir: new File(androidConf.tmpDirs[variant], 'res/layout')) { include (name : '**/*.xml') }
                        }
    }

    public void invertRFile(Project project, String variant, String invertedRFilePackage) {
        logger.lifecycle("invertRFile ${invertedRFilePackage}")
        File gen = new File(androidConf.tmpDirs[variant], 'gen')

        if (!gen.exists() || gen.list().length == 0) {
            projectHelper.executeCommand(project, androidConf.tmpDirs[variant], ['ant', 'debug'])
        }
        else {
            logger.lifecycle("gen already exists")
        }

        String appPackage = manifestHelper.readPackage(androidConf.tmpDirs[variant])
        appPackage = appPackage.replace(".", File.separator)
        File rFileDir = new File(androidConf.tmpDirs[variant], "gen"+File.separator+appPackage)
        File rFile = new File(rFileDir, "R.java");

        String invertedFileRelativePath = "src"+File.separator+invertedRFilePackage.replace(".", File.separator)
        File  invertedFile = new File(new File(androidConf.tmpDirs[variant], invertedFileRelativePath), "R.java");
        invertedFile.mkdirs();
        invertedFile.delete();
        invertedFile.createNewFile();
        invertRFile(rFileDir, invertedFile, invertedRFilePackage);
    }



    public static void invertRFile(File rFile, File invertedFile, String invertedRFilePackage) {
        BufferedReader reader = new BufferedReader(new FileReader(rFile));
        String line;
        String currentGroup;
        while((line = reader.readLine()) != null) {

            if(line.contains("package ")) {
                invertedFile << invertedRFilePackage << "\n";
            } else if(line.contains("public static final class")) {

                def pattern = /public static final class ([^\r\n]*) /
                line.find(pattern) { match, groupName ->
                    if(currentGroup != null) {
                        // close previous 'static' section
                        invertedFile << "\t\t}\n"
                    }
                    currentGroup = groupName;
                    invertedFile << "\t\tpublic static HashMap<String, String> " + groupName + " = new HashMap<String, String>();\n";
                    invertedFile << "\t\tstatic {\n"
                }
            } else if(line.contains("public static final int")) {
                def pattern = /public static final int ([^\r\n]*)=0x([^\r\n]*);/
                line.find(pattern) { match, name, id ->
                    int intId = Integer.parseInt(id,16);
                    invertedFile << "\t\t" + currentGroup + ".put(\"" + intId + "\",\"" +name+"\");\n";
                }
            }
            else {
                invertedFile << line << "\n";
            }
        }
    }

    File getMainApplicationFile(Project project, String variant) {
        File tmpDir = androidConf.tmpDirs[variant]
        String mainApplicationFileName = manifestHelper.getApplicationName(project.rootDir)
        mainApplicationFileName = mainApplicationFileName.replace('.', '/')
        mainApplicationFileName = mainApplicationFileName + '.java'
        mainApplicationFileName = 'src/' + mainApplicationFileName
        File f
        if (new File(tmpDir,mainApplicationFileName).exists()) {
            f = new File(tmpDir, mainApplicationFileName)
        } else {
            String mainActivityName = manifestHelper.getMainActivityName(project.rootDir)
            mainActivityName = mainActivityName.replace('.', '/')
            mainActivityName = mainActivityName + '.java'
            mainActivityName = 'src/' + mainActivityName
            if (!(new File(tmpDir, mainActivityName)).exists()) {
                f = null
            } else {
                f = new File(tmpDir, mainActivityName)
            }
        }
        return f
    }

    public void preprocessBuildsWithApphance(Project project) {
        project.tasks.each { task ->
            if (task.name.startsWith('buildDebug')) {
                def variant = task.name == 'buildDebug' ? 'Debug' : task.name.substring('buildDebug-'.length())
                task.doFirst {
                    if (!checkIfApphancePresent(project)) {
                        File mainFile = getMainApplicationFile(project, variant)
                        if (mainFile != null) {
                            replaceLogsWithApphance(project, variant)
                            replaceViewsWithApphance(project, variant)
                            addApphanceInit(project, variant, mainFile)
                            copyApphanceJar(project, variant)
                            addApphanceToManifest(project, variant)
                        }
                    }
                }
            }
            if (task.name.startsWith('buildRelease')) {
                def variant = task.name == 'buildRelease' ? 'Release' : task.name.substring('buildRelease-'.length())
                task.doFirst {
                    removeApphanceFromManifest(project, variant)
                    replaceLogsWithAndroid(project, variant)
                }
                task.doLast { restoreManifestBeforeApphanceRemoval(project, variant) }
            }
        }
    }

    private replaceLogsWithAndroid(Project project, String variant) {
        logger.lifecycle("Replacing apphance logs with android for ${variant}")
        project.ant.replace(casesensitive: 'true', token : 'import com.apphance.android.Log;',
                        value: 'import android.util.Log;', summary: true) {
                            fileset(dir: new File(androidConf.tmpDirs[variant], 'src')) { include (name : '**/*.java') }
                        }
    }

    private replaceLogsWithApphance(Project project, String variant) {
        logger.lifecycle("Replacing android logs with apphance for ${variant}")
        project.ant.replace(casesensitive: 'true', token : 'import android.util.Log;',
                        value: 'import com.apphance.android.Log;', summary: true) {
                            fileset(dir: new File(androidConf.tmpDirs[variant], 'src')) { include (name : '**/*.java') }
                        }
    }

    private restoreManifestBeforeApphanceRemoval(Project project, variant) {
        logger.lifecycle("Restoring before apphance was removed from manifest. ")
        manifestHelper.restoreBeforeApphanceRemoval(new File(androidConf.tmpDirs[variant], 'src'))
    }

    private removeApphanceFromManifest(Project project, variant) {
        logger.lifecycle("Remove apphance from manifest")
        manifestHelper.removeApphance(androidConf.tmpDirs[variant])
        File apphanceRemovedManifest = new File(conf.logDirectory,"AndroidManifest-with-apphance-removed.xml")
        logger.lifecycle("Manifest used for this build is stored in ${apphanceRemovedManifest}")
        if (apphanceRemovedManifest.exists()) {
            logger.lifecycle('removed manifest exists')
            apphanceRemovedManifest.delete()
        }
        apphanceRemovedManifest << new File(androidConf.tmpDirs[variant], "AndroidManifest.xml").text
    }

    private addApphanceToManifest(Project project, String variant) {
        logger.lifecycle("Adding apphance to manifest")
        manifestHelper.addApphanceToManifest(androidConf.tmpDirs[variant])
    }

    private def addApphanceInit(Project project, variant, File mainFile) {
        logger.lifecycle("Adding apphance init to file " + mainFile)
        def lineToModification = []
        mainFile.eachLine { line, lineNumber ->
            if (line.contains('super.onCreate')) {
                lineToModification << lineNumber
            }
        }
        File newMainClass = new File("newMainClassFile.java")
        def mode
        if (project[ApphanceProperty.APPHANCE_MODE.propertyName].equals("QA")) {
            mode = "Apphance.Mode.QA"
        } else {
            mode = "Apphance.Mode.SILENT"
        }
        String appKey = project[ApphanceProperty.APPLICATION_KEY.propertyName]
        String startSession = "Apphance.startNewSession(this, \"${appKey}\", ${mode});"

        /*
         if (project[ApphanceProperty.APPHANCE_LOG_EVENTS.propertyName].equals("true")) {
         startSession = startSession + "com.apphance.android.eventlog.EventLog.setInvertedIdMap("+EVENT_LOG_PACKAGE+".R.id)";
         }*/

        String importApphance = 'import com.apphance.android.Apphance;'
        boolean onCreateAdded = false
        newMainClass.withWriter { out ->
            mainFile.eachLine { line ->
                if (line.contains('super.onCreate') && !onCreateAdded) {
                    out.println(line << startSession)
                    onCreateAdded = true
                } else if (line.contains('package')) {
                    out.println(line << importApphance)
                } else {
                    out.println(line)
                }
            }
        }
        mainFile.delete()
        mainFile << newMainClass.getText()
        newMainClass.delete()
    }

    private copyApphanceJar(Project project, variant) {
        def libsDir = new File(androidConf.tmpDirs[variant], 'libs')
        libsDir.mkdirs()
        libsDir.eachFileMatch(".*apphance.*\\.jar") {
            logger.lifecycle("Removing old apphance jar: " + it.name)
            it.delete()
        }
        File libsApphance = new File(androidConf.tmpDirs[variant], 'libs/apphance.jar')
        URL apphanceUrl = this.class.getResource("apphance-android-library_1.5-event-log.jar")
        libsApphance << apphanceUrl.getContent()
    }

    private boolean checkIfApphancePresent(Project project) {
        boolean found = false
        File basedir = project.file('src')
        basedir.eachFileRecurse { file ->
            if (file.name.endsWith('.java')) {
                file.eachLine {
                    if (it.contains("Apphance.startNewSession")) {
                        found = true
                    }
                }
            }
        }
        if (!found) {
            project.file('.').eachFileMatch(".*apphance.*\\.jar") { found = true }
        }
        return found
    }

    static public final String DESCRIPTION =
    """This is the plugin that links Ameba with Apphance service.

The plugin provides integration with Apphance service. It performs the
following tasks: adding Apphance on-the-fly while building the application
(for all Debug builds), removing Apphance on-the-fly while building the application
(for all Release builds), submitting the application to apphance at release time.
"""
}
