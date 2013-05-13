package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.variants.AndroidVariantsConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.release.AmebaArtifact
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir
import static java.io.File.separator
import static org.gradle.testfixtures.ProjectBuilder.builder

class MailMessageTaskIntegrationSpec extends Specification {

    def 'prepares mail message'() {
        given:
        def otaDir = createTempDir()
        def apkDir = createTempDir()

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
        def arc = new AndroidReleaseConfiguration()
        arc.mailMessageFile = new AmebaArtifact(
                url: new URL(projectUrl, 'message_file.html'),
                location: new File("$otaDir$separator$projectName$separator$fullVersionString", 'message_file.html'))
        arc.apkFiles = [(mainVariant): new AmebaArtifact(url: projectUrl, location: apkDir)]
        arc.fileIndexFile = new AmebaArtifact(url: new URL(projectUrl, 'fileIndexFile.html'))
        arc.otaIndexFile = new AmebaArtifact(url: new URL(projectUrl, 'otaIndexFile.html'))
        arc.reader = reader

        and:
        def avc = GroovyMock(AndroidVariantsConfiguration)
        avc.mainVariant >> mainVariant

        and:
        def ac = GroovyMock(AndroidConfiguration)
        ac.projectName >> new StringProperty(value: projectName)
        ac.fullVersionString >> fullVersionString
        ac.versionString >> '42'

        and:
        def task = p.task(PrepareMailMessageTask.NAME, type: PrepareMailMessageTask) as PrepareMailMessageTask
        task.conf = ac
        task.releaseConf = arc
        task.variantsConf = avc

        when:
        task.mailMessage()

        then:
        arc.releaseMailSubject == 'Android TestAndroidProject 1.0.1_42 is ready to install'
        def mailMsgDir = new File(otaDir, "${projectName}${separator}${fullVersionString}")
        def mailMsgFile = new File(mailMsgDir, 'message_file.html')
        mailMsgDir.exists()
        mailMsgDir.list().size() == 1
        mailMsgFile.exists()
        mailMsgFile.size() > 0

        and:
        def html = new XmlSlurper().parse(mailMsgFile)
        html.head.title.text() == 'TestAndroidProject - Android'
        html.body.b[0].text() == 'TestAndroidProject'
        html.body.b[1].text() == '42'
        html.body.p[0].ul.li.a.@href.text() == 'http://ota.polidea.pl/otaIndexFile.html'
        html.body.p[1].ul.li[0].text() == 'release'
        html.body.p[1].ul.li[1].text() == 'notes'
        html.body.p[2].ul.li.a.@href.text() == 'http://ota.polidea.pl/fileIndexFile.html'

        cleanup:
        otaDir.deleteDir()
        apkDir.deleteDir()
        System.properties.remove('release.notes')
    }
}
