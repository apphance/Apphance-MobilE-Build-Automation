package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.plugins.android.parsers.AndroidBuildXmlHelper
import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import org.gradle.api.AntBuilder
import spock.lang.Specification

@Mixin(TestUtils)
class PackageReplacerSpec extends Specification {

    def 'test replace method in action class'() {
        given:
        def rootDir = temporaryDir
        def action = new PackageReplacer()
        def manifestHelper = GroovyMock(AndroidManifestHelper)
        def buildXMLHelper = GroovyMock(AndroidBuildXmlHelper)
        def antBuilder = GroovyMock(AntBuilder)
        action.ant = antBuilder
        action.manifestHelper = manifestHelper
        action.buildXMLHelper = buildXMLHelper
        File sourceFolder = new File(rootDir, "src/" + 'com.oldPkg'.replaceAll('\\.', '/'))
        File targetFolder = new File(rootDir, "src/" + 'com.newPkg'.replaceAll('\\.', '/'))

        when:
        action.replace(rootDir, 'com.oldPkg', 'com.newPkg', 'newLbl', newName)

        then:
        1 * manifestHelper.replacePackage(rootDir, 'com.oldPkg', 'com.newPkg', 'newLbl', newName)
        (newName ? 1 : 0) * buildXMLHelper.replaceProjectName(rootDir, newName)
        1 * antBuilder.move([file: sourceFolder, tofile: targetFolder, failonerror: false]) >> null
        1 * antBuilder.replace([casesensitive: 'true', token: "com.oldPkg", value: "com.newPkg", summary: true], _ as Closure) >> null

        where:
        newName << ['newNm', null]
    }
}
