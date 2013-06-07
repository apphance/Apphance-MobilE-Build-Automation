package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD

class ReplacePackageTask extends DefaultTask {

    static String NAME = 'replacePackage'
    String description = """Replaces manifest's package with a new one. Requires oldPackage and newPackage
           parameters. Optionally it takes newLabel or newName parameters if application's label/name is to be replaced"""
    String group = FLOW_BUILD

    @Inject AndroidConfiguration conf
    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidBuildXmlHelper buildXMLHelper
    @Inject PropertyReader reader

    @TaskAction
    void replacePackage() {
        String oldPackage = reader.systemProperty('oldPackage')
        logger.lifecycle("Old package $oldPackage")
        String newPackage = reader.systemProperty('newPackage')
        logger.lifecycle("New package $newPackage")
        String newLabel = reader.systemProperty('newLabel')
        logger.lifecycle("New label $newLabel")
        String newName = reader.systemProperty('newLabel')
        logger.lifecycle("New name $newName")
        manifestHelper.replacePackage(conf.rootDir, oldPackage, newPackage, newLabel)
        logger.lifecycle("Replaced the package from ${oldPackage} to ${newPackage}")
        if (newLabel != null) {
            logger.lifecycle("Also replaced label with ${newLabel}")
        }
        if (newName != null) {
            logger.lifecycle("Replacing name with ${newName}")
            buildXMLHelper.replaceProjectName(conf.rootDir, newName)
        }
        File sourceFolder = project.file("src/" + oldPackage.replaceAll('\\.', '/'))
        File targetFolder = project.file("src/" + newPackage.replaceAll('\\.', '/'))
        logger.lifecycle("Moving ${sourceFolder} to ${targetFolder}")
        ant.move(file: sourceFolder, tofile: targetFolder, failonerror: false)
        logger.lifecycle("Replacing remaining references in AndroidManifest ")
        ant.replace(casesensitive: 'true', token: "${oldPackage}",
                value: "${newPackage}", summary: true) {
            fileset(dir: 'src') { include(name: '**/*.java') }
            fileset(dir: 'res') { include(name: '**/*.xml') }
            fileset(dir: '.') { include(name: 'AndroidManifest.xml') }
        }
    }
}
