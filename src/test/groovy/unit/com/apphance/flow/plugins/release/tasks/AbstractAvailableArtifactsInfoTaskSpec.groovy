package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.properties.FileProperty
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.plugins.android.release.tasks.AvailableArtifactsInfoTask as AndroidAvailableArtifactsInfoTask
import com.apphance.flow.plugins.ios.release.tasks.AvailableArtifactsInfoTask as IosAvailableArtifactsInfoTask
import spock.lang.Specification

@Mixin(TestUtils)
class AbstractAvailableArtifactsInfoTaskSpec extends Specification {

    def 'test fillMailSubject'() {
        given:
        def infoTask = create TaskType
        infoTask.conf = GroovyStub(ProjectConfiguration) {
            getFullVersionString() >> 'full-version'
            getProjectName() >> new StringProperty(value: 'projName')
        }
        infoTask.releaseConf = GroovySpy(AndroidReleaseConfiguration) {
            getReleaseIcon() >> new FileProperty(value: null)
            getReleaseNotes() >> ['']
        }
        infoTask.initTask()

        expect:
        infoTask.fillMailSubject() == message

        where:
        TaskType                          | message
        AndroidAvailableArtifactsInfoTask | 'Android projName full-version is ready to install'
        IosAvailableArtifactsInfoTask     | "iOS projName full-version is ready to download"
    }
}
