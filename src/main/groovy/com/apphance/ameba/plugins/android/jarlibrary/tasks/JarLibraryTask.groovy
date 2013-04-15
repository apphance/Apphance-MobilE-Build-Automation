package com.apphance.ameba.plugins.android.jarlibrary.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidJarLibraryConfiguration
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD

//TODO to be tested & refactored
@Mixin(AndroidJarLibraryMixin)
class JarLibraryTask extends DefaultTask {

    static String NAME = 'jarLibrary'
    String group = AMEBA_BUILD
    String description = 'Prepares jar library with embedded resources'

    @Inject
    private AndroidConfiguration androidConf
    @Inject
    private AndroidJarLibraryConfiguration jarLibConf

    @TaskAction
    void jarLibrary() {
        androidConf.tmpDir.value.mkdirs()
        def manifestFile = new File(androidConf.tmpDir.value, 'MANIFEST.MF')
        project.ant.manifest(file: manifestFile) {
            attribute(name: 'Specification-Title', value: androidConf.projectName.value)
            attribute(name: 'Specification-Vendor', value: androidConf.projectName.value)
            attribute(name: 'Implementation-Title', value: androidConf.versionString.value)
            attribute(name: 'Implementation-Version', value: androidConf.versionCode.value)
            attribute(name: 'Implementation-Vendor', value: androidConf.projectName.value)
            attribute(name: 'Implementation-Vendor-Id', value: androidConf.projectName.value)
        }
        def manifestPropertiesFile = new File(androidConf.tmpDir.value, 'manifest.properties')
        def properties = new Properties()
        properties.setProperty("implementation.title", androidConf.versionString.value)
        properties.setProperty("implementation.version", androidConf.versionCode.value.toString())
        properties.store(manifestPropertiesFile.newOutputStream(), "Automatically generated with Ameba")
        File resDir = new File(androidConf.tmpDir.value, "${jarLibraryPrefix()}-res")
        project.ant.delete(dir: resDir)
        resDir.mkdirs()
        project.ant.copy(todir: resDir) {
            fileset(dir: project.file('res'))
        }
        File destFile = project.file(getJarLibraryFilePath(androidConf.projectName.value, androidConf.versionString.value))
        File classesDir = project.file("bin/classes")
        destFile.delete()
        project.ant.jar(destfile: destFile, manifest: manifestFile, manifestencoding: 'utf-8') {
            fileset(dir: classesDir) {
                include(name: '**/*.class')
                exclude(name: '**/test/*.class')
                exclude(name: 'R*.class')
            }
            fileset(dir: androidConf.tmpDir.value) {
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
