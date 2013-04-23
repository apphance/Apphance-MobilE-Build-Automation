package com.apphance.ameba.plugins.android.test.tasks

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class CleanAVDTaskTest extends Specification {

    def p = ProjectBuilder.builder().build()
    def cleanAVDTask = p.task(CleanAVDTask.NAME, type: CleanAVDTask) as CleanAVDTask

    // TODO do it for all tasks
    def "cleanAVDTask has actions"() {
        expect: cleanAVDTask.actions.size() > 0
    }
}
