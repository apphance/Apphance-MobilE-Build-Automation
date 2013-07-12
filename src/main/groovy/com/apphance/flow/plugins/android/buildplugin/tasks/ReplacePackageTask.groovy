package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
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
        String newPackage = reader.systemProperty('newPackage')
        String newLabel = reader.systemProperty('newLabel')
        String newName = reader.systemProperty('newName')

        replace(conf.rootDir, oldPackage, newPackage, newLabel, newName)
    }

    void replace(File dir, String oldPackage, String newPackage, String newLabel, String newName) {
        logger.lifecycle("Old package $oldPackage. New package $newPackage. New label $newLabel. New name $newName")

        manifestHelper.replacePackage(dir, oldPackage, newPackage, newLabel, newName)
        logger.lifecycle("Replaced the package from ${oldPackage} to ${newPackage}")

        if (newName != null) {
            logger.lifecycle("Replacing name with ${newName} in directory: $dir.absolutePath")
            buildXMLHelper.replaceProjectName(dir, newName)
        }

        File sourceFolder = new File(dir, "src/" + oldPackage.replaceAll('\\.', '/'))
        File targetFolder = new File(dir, "src/" + newPackage.replaceAll('\\.', '/'))

        logger.lifecycle("Moving ${sourceFolder} to ${targetFolder}")
        ant.move(file: sourceFolder, tofile: targetFolder, failonerror: false)

        logger.lifecycle("Replacing remaining references in AndroidManifest ")

        ant.replace(casesensitive: 'true', token: "${oldPackage}",
                value: "${newPackage}", summary: true) {
            fileset(dir: dir.absolutePath + '/src') { include(name: '**/*.java') }
            fileset(dir: dir.absolutePath + '/res') { include(name: '**/*.xml') }
            fileset(dir: dir.absolutePath) { include(name: 'AndroidManifest.xml') }
        }
    }
}