package com.apphance.flow.plugins.android.release.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.properties.URLProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.release.FlowArtifact
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir
import static java.io.File.separator
import static java.lang.System.getProperties
import static org.gradle.testfixtures.ProjectBuilder.builder

class PrepareMailMessageTaskIntegrationSpec extends Specification {

    def 'prepares mail message'() {
        given:
        def apkDir = createTempDir()
        def rootDir = createTempDir()

        and:
        def p = builder().build()

        and:
        def projectName = 'TestAndroidProject'
        def projectUrl = "http://ota.polidea.pl/$projectName".toURL()
        def fullVersionString = '1.0.1_42'
        def mainVariant = 'MainVariant'
        System.setProperty('release.notes', 'release\nnotes')

        and:
        def reader = new PropertyReader()

        and:
        def ac = GroovyMock(AndroidConfiguration)
        ac.projectName >> new StringProperty(value: projectName)
        ac.fullVersionString >> fullVersionString
        ac.versionString >> '1.0.1'
        ac.versionCode >> '42'
        ac.rootDir >> rootDir

        and:
        def arc = new AndroidReleaseConfiguration()
        arc.projectURL = new URLProperty(value: projectUrl.toString())
        arc.apkFiles = [(mainVariant): new FlowArtifact(url: projectUrl, location: apkDir)]
        arc.fileIndexFile = new FlowArtifact(url: new URL(projectUrl, 'fileIndexFile.html'))
        arc.otaIndexFile = new FlowArtifact(url: new URL(projectUrl, 'otaIndexFile.html'))
        arc.reader = reader
        arc.conf = ac

        and:
        def avc = GroovyMock(AndroidVariantsConfiguration)
        avc.mainVariant >> mainVariant

        and:
        def task = p.task(PrepareMailMessageTask.NAME, type: PrepareMailMessageTask) as PrepareMailMessageTask
        task.conf = ac
        task.releaseConf = arc
        task.variantsConf = avc

        when:
        task.prepareMailMessage()

        then:
        arc.releaseMailSubject == 'Android TestAndroidProject 1.0.1_42 is ready to install'
        def mailMsgDir = new File(rootDir, "flow-ota${separator}/${projectName}${separator}${fullVersionString}")
        def mailMsgFile = new File(mailMsgDir, 'message_file.html')
        mailMsgDir.exists()
        mailMsgDir.list().size() == 1
        mailMsgFile.exists()
        mailMsgFile.size() > 0

        and:
        def html = new XmlSlurper().parse(mailMsgFile)
        html.head.title.text() == 'TestAndroidProject - Android'
        html.body.b[0].text() == 'TestAndroidProject'
        html.body.b[1].text() == '1.0.1'
        html.body.p[0].ul.li.a.@href.text() == 'http://ota.polidea.pl/otaIndexFile.html'
        html.body.p[1].ul.li[0].text() == 'release'
        html.body.p[1].ul.li[1].text() == 'notes'
        html.body.p[2].ul.li.a.@href.text() == 'http://ota.polidea.pl/fileIndexFile.html'

        cleanup:
        apkDir.deleteDir()
        rootDir.deleteDir()
        properties.remove('release.notes')
    }
}
