package com.apphance.ameba.plugins.release.tasks

import com.apphance.ameba.executor.command.CommandExecutor
import org.gradle.api.Project
import spock.lang.Specification

class ImageMontageTaskSpec extends Specification {

    Project project = Mock()
    CommandExecutor commandExecutor = Mock()

    def imageMontageTask = new ImageMontageTask()

    def "test"() {
        expect:
        def testDir = new File('src/test/resources/com/apphance/ameba/plugins/release/tasks/montageFiles')
        testDir.exists()

        when:
        def files = imageMontageTask.getFilesToMontage(testDir)

        then:
        files.size() == 4
    }
}
