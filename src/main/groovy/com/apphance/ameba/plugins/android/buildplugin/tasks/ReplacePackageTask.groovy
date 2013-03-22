package com.apphance.ameba.plugins.android.buildplugin.tasks

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.android.AndroidBuildXmlHelper
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import org.gradle.api.Project

import static org.gradle.api.logging.Logging.getLogger

//TODO refactor/test
class ReplacePackageTask {

    private l = getLogger(getClass())

    private Project project
    private AndroidManifestHelper manifestHelper = new AndroidManifestHelper()

    ReplacePackageTask(Project project) {
        this.project = project
    }

    void replacePackage() {
        use(PropertyCategory) {
            String oldPackage = project.readExpectedProperty('oldPackage')
            l.lifecycle("Old package $oldPackage")
            String newPackage = project.readExpectedProperty('newPackage')
            l.lifecycle("New package $newPackage")
            String newLabel = project.readProperty('newLabel')
            l.lifecycle("New label $newLabel")
            String newName = project.readProperty('newLabel')
            l.lifecycle("New name $newName")
            manifestHelper.replacePackage(project.getRootDir(), oldPackage, newPackage, newLabel)
            l.lifecycle("Replaced the package from ${oldPackage} to ${newPackage}")
            if (newLabel != null) {
                l.lifecycle("Also replaced label with ${newLabel}")
            }
            if (newName != null) {
                l.lifecycle("Replacing name with ${newName}")
                AndroidBuildXmlHelper buildXMLHelper = new AndroidBuildXmlHelper()
                buildXMLHelper.replaceProjectName(project.rootDir, newName)
            }
            File sourceFolder = project.file("src/" + oldPackage.replaceAll('\\.', '/'))
            File targetFolder = project.file("src/" + newPackage.replaceAll('\\.', '/'))
            l.lifecycle("Moving ${sourceFolder} to ${targetFolder}")
            project.ant.move(file: sourceFolder, tofile: targetFolder, failonerror: false)
            l.lifecycle("Replacing remaining references in AndroidManifest ")
            project.ant.replace(casesensitive: 'true', token: "${oldPackage}",
                    value: "${newPackage}", summary: true) {
                fileset(dir: 'src') { include(name: '**/*.java') }
                fileset(dir: 'res') { include(name: '**/*.xml') }
                fileset(dir: '.') { include(name: 'AndroidManifest.xml') }
            }
        }
    }
}
