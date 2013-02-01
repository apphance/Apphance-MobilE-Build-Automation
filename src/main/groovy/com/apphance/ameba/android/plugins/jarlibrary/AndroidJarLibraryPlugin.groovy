package com.apphance.ameba.android.plugins.jarlibrary

import com.apphance.ameba.AmebaCommonBuildTaskGroups
import com.apphance.ameba.PluginHelper
import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.ProjectHelper
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidManifestHelper
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.plugins.buildplugin.AndroidPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * Helps building the library with resources embedded. It is useful in case we want to generate libraries like
 * *.jar that wants to have the resources embedded.
 */
class AndroidJarLibraryPlugin implements Plugin<Project> {

    static Logger logger = Logging.getLogger(AndroidJarLibraryPlugin.class)

    ProjectHelper projectHelper
    ProjectConfiguration conf
    AndroidManifestHelper manifestHelper
    AndroidProjectConfiguration androidConf
    String jarLibraryPrefix

    public void apply(Project project) {
        PluginHelper.checkAllPluginsAreLoaded(project, this.class, AndroidPlugin.class)
        use(PropertyCategory) {
            this.projectHelper = new ProjectHelper()
            this.conf = project.getProjectConfiguration()
            this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
            manifestHelper = new AndroidManifestHelper()
            jarLibraryPrefix = project.readProperty(AndroidJarLibraryProperty.RES_PREFIX)
            if (jarLibraryPrefix == null) {
                jarLibraryPrefix = this.androidConf.mainProjectName
            }
            prepareJarLibraryTask(project)
            prepareJarLibraryUploadTask(project)
            project.prepareSetup.prepareSetupOperations << new PrepareAndroidJarLibrarySetupOperation()
            project.verifySetup.verifySetupOperations << new VerifyAndroidJarLibrarySetupOperation()
            project.showSetup.showSetupOperations << new ShowAndroidJarLibrarySetupOperation()
        }
    }

    public void prepareJarLibraryTask(Project project) {
        def task = project.task('jarLibrary')
        task.description = "Prepares jar library with embedded resources"
        task.group = AmebaCommonBuildTaskGroups.AMEBA_BUILD
        task << {
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
            File resDir = new File(conf.tmpDirectory, "${jarLibraryPrefix}-res")
            project.ant.delete(dir: resDir)
            resDir.mkdirs()
            project.ant.copy(todir: resDir) {
                fileset(dir: project.file('res'))
            }
            File destFile = project.file(getJarLibraryFilePath())
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
        task.dependsOn(project.readAndroidProjectConfiguration)
    }

    private GString getJarLibraryFilePath() {
        "bin/${androidConf.mainProjectName}_${conf.versionString}.jar"
    }

    public void prepareJarLibraryUploadTask(Project project) {
        project.configurations {
            // this makes uploadJarLibraryConfiguration task visible
            // we need to specify archives for this configuration
            jarLibraryConfiguration
        }

        project.uploadJarLibraryConfiguration.doFirst {

            project.uploadJarLibraryConfiguration {

                repositories {
                    mavenDeployer {
                        pom.version = pom.version == '0' ? conf.versionString : pom.version
                    }
                }
            }

            project.artifacts {
                jarLibraryConfiguration file: project.file(getJarLibraryFilePath()), name: androidConf.mainProjectName
            }
        }

        project.uploadJarLibraryConfiguration.dependsOn project.jarLibrary
    }

    static public final String DESCRIPTION =
        """This is the plugin that makes up Android's inability to prepare standalone .jar libraries.

Currently (android sdk v16 as of this writing) android has no features yet to provide
libraries as standalone .jar files. The feature is being worked on, but temporarily
the jarlibrary plugin provides capability of building such jar library. It embeds
resources of Android project (from res directory) to standard java/jar resource - with specified prefix.
This is not a perfect solution (for example you cannot process layouts this way - only the
images) but it will do for a moment. This is how Apphance service library is prepared.
"""

}
