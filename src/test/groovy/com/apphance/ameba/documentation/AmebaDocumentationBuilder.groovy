package com.apphance.ameba.documentation

import groovy.text.SimpleTemplateEngine
import groovy.xml.MarkupBuilder

import java.io.File;
import java.util.List;

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.model.GradleProject;

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty
import com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryProperty
import com.apphance.ameba.android.plugins.test.AndroidTestProperty
import com.apphance.ameba.ios.plugins.buildplugin.IOSProjectProperty
import com.apphance.ameba.ios.plugins.fonemonkey.IOSFoneMonkeyProperty
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkProperty
import com.apphance.ameba.ios.plugins.kif.IOSKifProperty
import com.apphance.ameba.plugins.projectconfiguration.BaseProperty
import com.apphance.ameba.plugins.release.ProjectReleaseProperty
import com.apphance.ameba.vcs.plugins.git.GitProperty
import com.apphance.ameba.vcs.plugins.mercurial.MercurialProperty

class AmebaDocumentationBuilder {

	String COMMON_TASKS  = "Common tasks"
	String VCS_TASKS = "VCS tasks"
	String IOS_TASKS = "iOS tasks"
	String ANDROID_TASKS = "Android tasks"

	AmebaDocumentation amebaDocumentation = new AmebaDocumentation()
	def excludedPath = [
		':assemble',
		':build',
		':buildNeeded',
		':classes',
		':compileJava',
		':compileTestJava',
		':jar',
		':processResources',
		':processTestResources',
		':test',
		':testClasses',
		':build-GradleXCode-BasicConfiguration',
		':build--',
	]

	def vcsPath = [
		':cleanVCS',
		':saveReleaseInfoInVCS'
	]

	private GradleProject getProjectModel(File projectDir) {
		ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(projectDir).connect()
		return connection.getModel(GradleProject.class)
	}

	Class getPluginClass(String pluginName) {
		Properties p = new Properties()
		p.load(this.class.getResourceAsStream("/META-INF/gradle-plugins/${pluginName}.properties"))
		return Class.forName(p.get('implementation-class'))
	}


	private boolean shouldTaskBeAdded(PluginGroupDocumentation group, String taskPath) {
		if (taskPath in vcsPath && group.name == VCS_TASKS) {
			return true
		}
		if (taskPath in excludedPath) {
			return false
		}
		return !amebaDocumentation.tasks.containsKey(taskPath)
	}

	private Project getProject(File projectDir) {
		ProjectBuilder projectBuilder = ProjectBuilder.builder()
		projectBuilder.withProjectDir(projectDir)
		return projectBuilder.build()
	}

	private List<String> getExample(File templateDir, File projectDir) {
		def list = []
		boolean enabled = true
		new File(templateDir,'build.gradle').text.split ('\n').each {
			if (it.trim().startsWith('apply')) {
				enabled = false
			}
			if (enabled && ! it.trim().empty) {
				if (it.empty) {
					list << '<br>'
				} else {
					list << it.replaceAll(' ', '&nbsp;')
				}
			}
		}
		new File(projectDir,'build.gradle').text.split ('\n').each {
			if (it.trim().startsWith('apply')) {
				enabled = true
			}
			if (enabled && ! it.trim().empty) {
				if (it.empty) {
					list << '<br>'
				} else {
					list << it.replaceAll(' ', '&nbsp;')
				}
			}
		}
		return list
	}

	private void addAmebaDocumentation(String groupName, String pluginName, property = null) {
		def projectDir = new File("testProjects/documentation/${pluginName}")
		def templateDir = new File("templates/android")
		Project project = getProject(projectDir)
		GradleProject gradleProject =  getProjectModel(projectDir)
		PluginGroupDocumentation group = getPluginGroupDocumentation(groupName)
		PluginDocumentation plugin = new PluginDocumentation()
		plugin.clazz = getPluginClass(pluginName)
		plugin.name = pluginName
		plugin.description = plugin.clazz.getField('DESCRIPTION').get(null)
		plugin.example = getExample(templateDir, projectDir)
		group.plugins.put(pluginName, plugin)
		group.pluginNames << pluginName
		gradleProject.tasks.each { task ->
			if (shouldTaskBeAdded(group, task.path)) {
				amebaDocumentation.tasks.put(task.path, task)
				plugin.tasks.put(task.path, task)
			}
		}
		if (property != null) {
			MarkupBuilder mb = new MarkupBuilder()
			plugin.props = PropertyCategory.listProperties(project, property, true).collect {mb.escapeXmlValue(it, false)}
		}
	}

	private PluginGroupDocumentation getPluginGroupDocumentation(String groupName) {
		if (amebaDocumentation.groups.containsKey(groupName) ) {
			return amebaDocumentation.groups[groupName]
		} else {
			def PluginGroupDocumentation = new PluginGroupDocumentation()
			amebaDocumentation.groups[groupName]  = PluginGroupDocumentation
			PluginGroupDocumentation.name = groupName
			amebaDocumentation.groupNames << groupName
			return PluginGroupDocumentation
		}
	}

	private void generateDocumentation() {
		URL pluginsReferenceTemplate = this.class.getResource("plugins_reference.html")
		SimpleTemplateEngine engine = new SimpleTemplateEngine()
		String version
		Properties p = new Properties()
		new File('gradle.properties').withInputStream { p.load(it) }
		def binding = [
					amebaDocumentation : amebaDocumentation,
					version: p.get('version'),
				]
		def result = engine.createTemplate(pluginsReferenceTemplate).make(binding)
		new File("tmp/plugins_reference.html").write(result.toString(), "utf-8")
	}

	public void buildDocumentation() throws Exception {
		addAmebaDocumentation(COMMON_TASKS,'ameba-project-configuration', BaseProperty.class)
		addAmebaDocumentation(VCS_TASKS, 'ameba-git', GitProperty.class)
		addAmebaDocumentation(VCS_TASKS, 'ameba-mercurial', MercurialProperty.class)
		addAmebaDocumentation(ANDROID_TASKS, 'ameba-android-build', AndroidProjectProperty.class)
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-build', IOSProjectProperty.class)
		addAmebaDocumentation(COMMON_TASKS, 'ameba-project-release', ProjectReleaseProperty.class)
		addAmebaDocumentation(ANDROID_TASKS, 'ameba-android-analysis')
		addAmebaDocumentation(ANDROID_TASKS, 'ameba-android-apphance')
		addAmebaDocumentation(ANDROID_TASKS, 'ameba-android-jarlibrary', AndroidJarLibraryProperty.class)
		addAmebaDocumentation(ANDROID_TASKS, 'ameba-android-release')
		addAmebaDocumentation(ANDROID_TASKS, 'ameba-android-test', AndroidTestProperty.class)
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-cedar')
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-fonemonkey',IOSFoneMonkeyProperty.class)
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-framework', IOSFrameworkProperty.class)
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-kif', IOSKifProperty.class)
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-ocunit')
		addAmebaDocumentation(IOS_TASKS, 'ameba-ios-release')
		generateDocumentation()
		println amebaDocumentation
	}


	public static void main(String[] args) {
		new AmebaDocumentationBuilder().buildDocumentation()
	}

}
