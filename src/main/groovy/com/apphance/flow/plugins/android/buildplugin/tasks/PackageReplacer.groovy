package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper

import static org.gradle.api.logging.Logging.getLogger

class PackageReplacer {

    def logger = getLogger(this.class)
    def manifestHelper = new AndroidManifestHelper()
    def buildXMLHelper = new AndroidBuildXmlHelper()
    def reader = new PropertyReader()
    def ant = new AntBuilder()

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
        ant.replace(casesensitive: 'true', token: "${oldPackage}", value: "${newPackage}", summary: true) {
            fileset(dir: dir.absolutePath + '/src') { include(name: '**/*.java') }
            fileset(dir: dir.absolutePath + '/res') { include(name: '**/*.xml') }
            fileset(dir: dir.absolutePath) { include(name: 'AndroidManifest.xml') }
        }
    }
}