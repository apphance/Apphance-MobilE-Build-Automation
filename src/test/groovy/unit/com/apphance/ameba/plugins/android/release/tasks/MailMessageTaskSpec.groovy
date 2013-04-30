package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantsConfiguration
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyReader
import com.apphance.ameba.plugins.release.AmebaArtifact
import org.gradle.api.GradleException
import spock.lang.Specification

import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class MailMessageTaskSpec extends Specification {

    def p = builder().build()
    def mmt = p.task(MailMessageTask.NAME, type: MailMessageTask) as MailMessageTask

    def 'release notes are validated correctly when empty'() {
        when:
        mmt.validateReleaseNotes(releaseNotes)

        then:
        def e = thrown(GradleException)
        e.message =~ 'Release notes are empty'

        where:
        releaseNotes << [[], null]
    }

    def 'release notes are validated correctly when set'() {
        when:
        mmt.validateReleaseNotes(releaseNotes)

        then:
        noExceptionThrown()

        where:
        releaseNotes << [['1', '2', '3'], ['', '2']]
    }

    def 'prepares mail message'() {
        given:
        def otaDir = createTempDir()
        def apkDir = createTempDir()

        and:
        def p = builder().withProjectDir(new File('testProjects/android/android-basic')).build()

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
                name: 'Mail message file',
                url: new URL(projectUrl, 'message_file.html'),
                location: new File(new File(new File(otaDir, projectName), fullVersionString), 'message_file.html'))
        arc.apkFiles = [(mainVariant): new AmebaArtifact(location: apkDir, url: projectUrl, name: "${mainVariant}.apk")]
        arc.reader = reader

        and:
        def avc = GroovyMock(AndroidVariantsConfiguration)
        avc.mainVariant >> mainVariant

        and:
        def ac = GroovyMock(AndroidConfiguration)
        ac.projectName >> new StringProperty(value: projectName)
        ac.fullVersionString >> fullVersionString

        and:
        def task = p.task(MailMessageTask.NAME, type: MailMessageTask) as MailMessageTask
        task.conf = ac
        task.releaseConf = arc
        task.variantsConf = avc

        when:
        task.mailMessage()

        then:
        arc.releaseMailSubject == 'Android TestAndroidProject 1.0.1_42 is ready to install'

        cleanup:
        otaDir.deleteDir()
        apkDir.deleteDir()
        System.properties.remove('release.notes')
    }
}
