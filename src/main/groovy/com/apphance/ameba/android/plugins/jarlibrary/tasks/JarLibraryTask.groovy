package com.apphance.ameba.android.plugins.jarlibrary.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.plugins.jarlibrary.AndroidJarLibraryProperty
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.PropertyCategory.readProperty
import static com.apphance.ameba.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

//TODO to be tested & refactored
@Mixin(AndroidJarLibraryMixin)
class JarLibraryTask {

    private Project project
    private ProjectConfiguration conf
    private AndroidProjectConfiguration androidConf


    JarLibraryTask(Project project) {
        this.project = project
        this.conf = getProjectConfiguration(project)
        this.androidConf = getAndroidProjectConfiguration(project)
    }

    public void jarLibrary() {
        conf.tmpDirectory.mkdirs()
        def manifestFile = new File(conf.tmpDirectory, 'MANIFEST.MF')
        project.ant.manifest(file: manifestFile) {
            attribute(name: 'Specification-Title', value: androidConf.mainProjectName)
            attribute(name: 'Specification-Vendor', value: androidConf.mainProjectName)
            attribute(name: 'Implementation-Title', value: conf.versionString)
            attribute(name: 'Implementation-Version', value: conf.versionCode)
            attribute(name: 'Implementation-Vendor', value: androidConf.mainProjectName)
            attribute(name: 'Implementation-Vendor-Id', value: androidConf.mainProjectName)
        }
        def manifestPropertiesFile = new File(conf.tmpDirectory, 'manifest.properties')
        def properties = new Properties()
        properties.setProperty("implementation.title", conf.versionString)
        properties.setProperty("implementation.version", Long.toString(conf.versionCode))
        properties.store(manifestPropertiesFile.newOutputStream(), "Automatically generated with Ameba")
        File resDir = new File(conf.tmpDirectory, "${jarLibraryPrefix()}-res")
        project.ant.delete(dir: resDir)
        resDir.mkdirs()
        project.ant.copy(todir: resDir) {
            fileset(dir: project.file('res'))
        }
        File destFile = project.file(getJarLibraryFilePath(androidConf.mainProjectName, conf.versionString))
        File classesDir = project.file("bin/classes")
        destFile.delete()
        project.ant.jar(destfile: destFile, manifest: manifestFile, manifestencoding: 'utf-8') {
            fileset(dir: classesDir) {
                include(name: '**/*.class')
                exclude(name: '**/test/*.class')
                exclude(name: 'R*.class')
            }
            fileset(dir: conf.tmpDirectory) {
                include(name: 'manifest.properties')
                include(name: "${resDir.name}/**")
                exclude(name: '**/test*.*')
                exclude(name: "${resDir.name}/raw/config.properties")
            }
        }
    }

    private String jarLibraryPrefix() {
        readProperty(project, AndroidJarLibraryProperty.RES_PREFIX) ?: androidConf.mainProjectName
    }
}
