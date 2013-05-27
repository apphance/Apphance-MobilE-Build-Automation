package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_BUILD
import static org.gradle.api.logging.Logging.getLogger

class ReplacePackageTask extends DefaultTask {

    static String NAME = 'replacePackage'
    String description = """Replaces manifest's package with a new one. Requires oldPackage and newPackage
           parameters. Optionally it takes newLabel or newName parameters if application's label/name is to be replaced"""
    String group = AMEBA_BUILD

    private l = getLogger(getClass())

    @Inject AndroidConfiguration conf
    @Inject AndroidManifestHelper manifestHelper
    @Inject AndroidBuildXmlHelper buildXMLHelper
    @Inject PropertyReader reader

    @TaskAction
    void replacePackage() {
        String oldPackage = reader.systemProperty('oldPackage')
        l.lifecycle("Old package $oldPackage")
        String newPackage = reader.systemProperty('newPackage')
        l.lifecycle("New package $newPackage")
        String newLabel = reader.systemProperty('newLabel')
        l.lifecycle("New label $newLabel")
        String newName = reader.systemProperty('newLabel')
        l.lifecycle("New name $newName")
        manifestHelper.replacePackage(conf.rootDir, oldPackage, newPackage, newLabel)
        l.lifecycle("Replaced the package from ${oldPackage} to ${newPackage}")
        if (newLabel != null) {
            l.lifecycle("Also replaced label with ${newLabel}")
        }
        if (newName != null) {
            l.lifecycle("Replacing name with ${newName}")
            buildXMLHelper.replaceProjectName(project.rootDir, newName)
        }
        File sourceFolder = project.file("src/" + oldPackage.replaceAll('\\.', '/'))
        File targetFolder = project.file("src/" + newPackage.replaceAll('\\.', '/'))
        l.lifecycle("Moving ${sourceFolder} to ${targetFolder}")
        ant.move(file: sourceFolder, tofile: targetFolder, failonerror: false)
        l.lifecycle("Replacing remaining references in AndroidManifest ")
        ant.replace(casesensitive: 'true', token: "${oldPackage}",
                value: "${newPackage}", summary: true) {
            fileset(dir: 'src') { include(name: '**/*.java') }
            fileset(dir: 'res') { include(name: '**/*.xml') }
            fileset(dir: '.') { include(name: 'AndroidManifest.xml') }
        }
    }
}
