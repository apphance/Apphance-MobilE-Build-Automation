package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.android.AndroidProjectConfiguration
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.android.AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration

//TODO refactor/test
class CopySourcesTask {

    private ProjectConfiguration conf
    private AndroidProjectConfiguration androidConf
    private File directory
    private AntBuilder ant

    CopySourcesTask(Project project) {
        this.conf = getProjectConfiguration(project)
        this.androidConf = getAndroidProjectConfiguration(project)
        this.directory = project.rootDir
        this.ant = project.ant
    }

    void copySources() {
        androidConf.variants.each { variant ->
            ant.sync(toDir: androidConf.tmpDirs[variant], overwrite: true, failonerror: false, verbose: false) {
                fileset(dir: "${directory}/") {
                    exclude(name: androidConf.tmpDirs[variant].absolutePath + '/**/*')
                    conf.sourceExcludes.each {
                        if (!it.equals('**/local.properties') && !it.equals('**/gen/**')) {
                            exclude(name: it)
                        }
                    }
                }
            }
        }
    }
}
