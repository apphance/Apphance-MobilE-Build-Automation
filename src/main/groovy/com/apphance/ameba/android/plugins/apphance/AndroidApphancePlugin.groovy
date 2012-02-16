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
import com.apphance.ameba.apphance.ApphanceProperty;
import com.apphance.ameba.apphance.PrepareApphanceSetup;
import com.apphance.ameba.apphance.ShowApphancePropertiesTask;
import com.apphance.ameba.apphance.VerifyApphanceSetupTask;
import com.apphance.ameba.apphance.ApphanceProperty;

class AndroidApphancePlugin implements Plugin<Project>{

    static Logger logger = Logging.getLogger(AndroidApphancePlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    File findbugsHomeDir
    AndroidManifestHelper manifestHelper

    public void apply(Project project) {
        use (PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            manifestHelper = new AndroidManifestHelper()
            preprocessBuildsWithApphance(project)
            prepareConvertLogsToApphance(project)
            prepareConvertLogsToAndroid(project)
            prepareRemoveApphaceFromManifest(project)
            prepareRestoreManifestBeforeApphance(project)
			project.task('showApphanceProperties', type:ShowApphancePropertiesTask)
			project.task('verifyApphanceSetup', type:VerifyApphanceSetupTask)
			project.task('prepareApphanceSetup', type:PrepareApphanceSetup)
        }
    }

    void prepareConvertLogsToApphance(project) {
        def task = project.task('convertLogsToApphance')
        task.description = "Converts all logs to apphance from android logs"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << { replaceLogsWithApphance(project) }
    }

    void prepareConvertLogsToAndroid(project) {
        def task = project.task('convertLogsToAndroid')
        task.description = "Converts all logs to android from apphance logs"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << { replaceLogsWithAndroid(project) }
    }

    void prepareRemoveApphaceFromManifest(project) {
        def task = project.task('removeApphanceFromManifest')
        task.description = "Remove apphance-only entries from manifest"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << { removeApphanceFromManifest(project) }
    }

    void prepareRestoreManifestBeforeApphance(project) {
        def task = project.task('restoreManifestBeforeApphance')
        task.description = "Restore manifest to before apphance replacement"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
        task << { restoreManifestBeforeApphanceRemoval(project) }
    }

    public void preprocessBuildsWithApphance(Project project) {
        project.tasks.each {  task ->
            if (task.name.startsWith('buildDebug')) {
                task.doFirst { 
					replaceLogsWithApphance(project) 
					addApphanceInit(project)	
					copyApphanceJar(project)
				}
            }
            if (task.name.startsWith('buildRelease')) {
                task.doFirst {
                    removeApphanceFromManifest(project)
                    replaceLogsWithAndroid(project)
                }
                task.doLast { restoreManifestBeforeApphanceRemoval(project) }
            }
        }
    }

    private replaceLogsWithAndroid(Project project) {
        project.ant.replace(casesensitive: 'true', token : 'import com.apphance.android.Log;',
                value: 'import android.util.Log;', summary: true) {
                    fileset(dir: 'src') { include (name : '**/*.java') }
                }
    }

    private replaceLogsWithApphance(Project project) {
        logger.lifecycle("Replacing android logs with apphance")
        project.ant.replace(casesensitive: 'true', token : 'import android.util.Log;',
                value: 'import com.apphance.android.Log;', summary: true) {
                    fileset(dir: 'src') { include (name : '**/*.java') }
                }
    }

    private restoreManifestBeforeApphanceRemoval(Project project) {
        logger.lifecycle("Restoring before apphance was removed from manifest. ")
        manifestHelper.restoreBeforeApphanceRemoval(project.rootDir)
    }

    private removeApphanceFromManifest(Project project) {
        logger.lifecycle("Remove apphance from manifest")
        manifestHelper.removeApphance(project.rootDir)
        File apphanceRemovedManifest = new File(conf.logDirectory,"AndroidManifest-with-apphance-removed.xml")
        logger.lifecycle("Manifest used for this build is stored in ${apphanceRemovedManifest}")
        apphanceRemovedManifest.delete()
        apphanceRemovedManifest << new File(project.rootDir,"AndroidManifest.xml").text
    }
	
	private addApphanceInit(Project project) {
		String applicationName = manifestHelper.getApplicationName(project.rootDir)
		applicationName = applicationName.replace('.', '/')
		applicationName = applicationName + '.java'
		applicationName = 'src/' + applicationName
		File f
		if (new File(applicationName).exists()) {
			f = new File(applicationName)
		} else {
			String mainActivityName = manifestHelper.getMainActivityName(project.rootDir)
			mainActivityName = mainActivityName.replace('.', '/')
			mainActivityName = mainActivityName + '.java'
			mainActivityName = 'src/' + mainActivityName
			if (!(new File(mainActivityName)).exists()) {
				throw new GradleException("Application class and main activity not defined in Android manifest")
			}
			f = new File(mainActivityName)
		}
		
		def lineToModification = []
		f.eachLine { line, lineNumber ->
			if (line.contains('super.onCreate')) {
				lineToModification << lineNumber
			}
		}
		File newMainClass = new File("newMainClassFile.java")
		def mode
		if (project[ApphanceProperty.APPHANCE_MODE.propertyName].equals("QA")) {
			mode = "Apphance.QA"
		} else {
			mode = "Apphance.SILENT"
		}
		String appKey = project[ApphanceProperty.APPLICATION_KEY.propertyName]
		String startSession = "Apphance.startNewSession(this, \"${appKey}\", ${mode});"
		String importApphance = 'import com.apphance.android.Apphance;'
		newMainClass.withWriter { out ->
			f.eachLine { line ->
				if (line.contains('super.onCreate')) {
					out.println(line << startSession)
				} else if (line.contains('package')) {
					out.println(line << importApphance)
				} else {
					out.println(line)
				}
			}
		}
	}
	
	private copyApphanceJar(Project project) {
		File libsApphance = new File(project.rootDir, '/libs/apphance.jar')
		URL apphanceUrl = this.class.getResource("apphance-android-library_1.4.2.1.jar")
		libsApphance << apphanceUrl.getContent()
	}
}
