package com.apphance.ameba.unit;

import static org.junit.Assert.*;
import groovy.text.SimpleTemplateEngine

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;
import org.junit.Test;

import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.android.plugins.buildplugin.AndroidProjectProperty;
import com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryProperty;
import com.apphance.ameba.android.plugins.test.AndroidTestProperty;
import com.apphance.ameba.ios.plugins.buildplugin.IOSProjectProperty;
import com.apphance.ameba.ios.plugins.fonemonkey.IOSFoneMonkeyProperty;
import com.apphance.ameba.ios.plugins.framework.IOSFrameworkProperty;
import com.apphance.ameba.ios.plugins.kif.IOSKifProperty;
import com.apphance.ameba.plugins.projectconfiguration.BaseProperty;
import com.apphance.ameba.plugins.release.ProjectReleaseProperty;
import com.apphance.ameba.vcs.plugins.git.GitProperty;
import com.apphance.ameba.vcs.plugins.mercurial.MercurialProperty;

class BuildPluginDocumentation {

    String COMMON_TASKS  = "Common tasks"
    String VCS_TASKS = "VCS tasks"
    String IOS_TASKS = "iOS tasks"
    String ANDROID_TASKS = "Android tasks"

    DocumentationInfo documentationInfo = new DocumentationInfo()
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


    private boolean shouldTaskBeAdded(PluginGroup group, String taskPath) {
        if (taskPath in vcsPath && group.name == VCS_TASKS) {
            return true
        }
        if (taskPath in excludedPath) {
            return false
        }
        return !documentationInfo.tasks.containsKey(taskPath)
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

    private void addDocumentationInfo(String groupName, String pluginName, property = null) {
        def projectDir = new File("testProjects/documentation/${pluginName}")
        def templateDir = new File("templates/android")
        Project project = getProject(projectDir)
        GradleProject gradleProject =  getProjectModel(projectDir)
        PluginGroup group = getPluginGroup(groupName)
        Plugin plugin = new Plugin()
        plugin.clazz = getPluginClass(pluginName)
        plugin.name = pluginName
        plugin.description = plugin.clazz.getField('DESCRIPTION').get(null)
        plugin.example = getExample(templateDir, projectDir)
        group.plugins.put(pluginName, plugin)
        group.pluginNames << pluginName
        gradleProject.tasks.each { task ->
            if (shouldTaskBeAdded(group, task.path)) {
                documentationInfo.tasks.put(task.path, task)
                plugin.tasks.put(task.path, task)
            }
        }
        if (property != null) {
            plugin.props = PropertyCategory.listProperties(project, property, true)
        }
    }

    private PluginGroup getPluginGroup(String groupName) {
        if (documentationInfo.groups.containsKey(groupName) ) {
            return documentationInfo.groups[groupName]
        } else {
            def pluginGroup = new PluginGroup()
            documentationInfo.groups[groupName]  = pluginGroup
            pluginGroup.name = groupName
            documentationInfo.groupNames << groupName
            return pluginGroup
        }
    }

    private void generateDocumentation() {
        URL pluginsReferenceTemplate = this.class.getResource("plugins_reference.html")
        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        String version
        Properties p = new Properties()
        new File('gradle.properties').withInputStream { p.load(it) }
        def binding = [
                    documentationInfo : documentationInfo,
                    version: p.get('version'),
                ]
        def result = engine.createTemplate(pluginsReferenceTemplate).make(binding)
        new File("tmp/plugins_reference.html").write(result.toString(), "utf-8")
    }

    @Test
    public void testBuildDocumentation() throws Exception {
        addDocumentationInfo(COMMON_TASKS,'ameba-project-configuration', BaseProperty.class)
        addDocumentationInfo(VCS_TASKS, 'ameba-git', GitProperty.class)
        addDocumentationInfo(VCS_TASKS, 'ameba-mercurial', MercurialProperty.class)
        addDocumentationInfo(ANDROID_TASKS, 'ameba-android-build', AndroidProjectProperty.class)
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-build', IOSProjectProperty.class)
        addDocumentationInfo(COMMON_TASKS, 'ameba-project-release', ProjectReleaseProperty.class)
        addDocumentationInfo(ANDROID_TASKS, 'ameba-android-analysis')
        addDocumentationInfo(ANDROID_TASKS, 'ameba-android-apphance')
        addDocumentationInfo(ANDROID_TASKS, 'ameba-android-jarlibrary', AndroidJarLibraryProperty.class)
        addDocumentationInfo(ANDROID_TASKS, 'ameba-android-release')
        addDocumentationInfo(ANDROID_TASKS, 'ameba-android-test', AndroidTestProperty.class)
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-cedar')
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-fonemonkey',IOSFoneMonkeyProperty.class)
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-framework', IOSFrameworkProperty.class)
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-kif', IOSKifProperty.class)
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-ocunit')
        addDocumentationInfo(IOS_TASKS, 'ameba-ios-release')
        generateDocumentation()
        println documentationInfo
    }
}
