package com.apphance.flow.plugins.android.jarlibrary.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidJarLibraryConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

@Mixin(AndroidJarLibraryMixin)
class JarLibraryTask extends DefaultTask {

    static String NAME = 'jarLibrary'
    String group = FLOW_BUILD
    String description = 'Prepares jar library with embedded resources'

    @Inject AndroidConfiguration androidConf
    @Inject AndroidJarLibraryConfiguration jarLibConf

    @TaskAction
    void jarLibrary() {
        androidConf.tmpDir.mkdirs()
        def manifestFile = new File(androidConf.tmpDir, 'MANIFEST.MF')
        ant.manifest(file: manifestFile) {
            attribute(name: 'Specification-Title', value: androidConf.projectName.value)
            attribute(name: 'Specification-Vendor', value: androidConf.projectName.value)
            attribute(name: 'Implementation-Title', value: androidConf.versionString)
            attribute(name: 'Implementation-Version', value: androidConf.versionCode)
            attribute(name: 'Implementation-Vendor', value: androidConf.projectName.value)
            attribute(name: 'Implementation-Vendor-Id', value: androidConf.projectName.value)
        }
        def manifestPropertiesFile = new File(androidConf.tmpDir, 'manifest.properties')
        def properties = new Properties()
        properties.setProperty("implementation.title", androidConf.versionString)
        properties.setProperty("implementation.version", androidConf.versionCode)
        properties.store(manifestPropertiesFile.newOutputStream(), "Automatically generated with Ameba")
        File resDir = new File(androidConf.tmpDir, "${jarLibraryPrefix()}-res")
        ant.delete(dir: resDir)
        resDir.mkdirs()
        ant.copy(todir: resDir) {
            fileset(dir: project.file('res'))
        }
        File destFile = project.file(getJarLibraryFilePath(androidConf.projectName.value, androidConf.versionString))
        File classesDir = project.file("bin/classes")
        destFile.delete()
        ant.jar(destfile: destFile, manifest: manifestFile, manifestencoding: 'utf-8') {
            fileset(dir: classesDir) {
                include(name: '**/*.class')
                exclude(name: '**/test/*.class')
                exclude(name: 'R*.class')
            }
            fileset(dir: androidConf.tmpDir) {
                include(name: 'manifest.properties')
                include(name: "${resDir.name}/**")
                exclude(name: '**/test*.*')
                exclude(name: "${resDir.name}/raw/config.properties")
            }
        }
    }

    private String jarLibraryPrefix() {
        jarLibConf.resourcePrefix.value ?: androidConf.projectName.value
    }
}
