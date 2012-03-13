package com.apphance.ameba.android.plugins.apphance

import java.io.File

import org.gradle.api.GradleException
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
import com.apphance.ameba.apphance.PrepareApphanceSetupOperation;
import com.apphance.ameba.apphance.ShowApphancePropertiesOperation;
import com.apphance.ameba.apphance.VerifyApphanceSetupOperation;

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
        project.tasks.each {  task ->
            if (task.name.startsWith('buildDebug')) {
                def variant = task.name == 'buildDebug' ? 'Debug' : task.name.substring('buildDebug-'.length())
                task.doFirst {
                    if (!checkIfApphancePresent(project)) {
						logger.lifecycle("Apphance not found in project")
                        File mainFile = getMainApplicationFile(project, variant)
                        if (mainFile != null) {
                            replaceLogsWithApphance(project, variant)
                            addApphanceInit(project, variant, mainFile)
                            copyApphanceJar(project, variant)
                            addApphanceToManifest(project, variant)
                        }
                    } else {
						logger.lifecycle("Apphance found in project")
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
        String importApphance = 'import com.apphance.android.Apphance;'
        newMainClass.withWriter { out ->
            mainFile.eachLine { line ->
                if (line.contains('super.onCreate')) {
                    out.println(line << startSession)
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
        URL apphanceUrl = this.class.getResource("apphance-android-library_1.5.jar")
        libsApphance.withWriter{ out ->
			out << apphanceUrl.getContent()
        }
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
