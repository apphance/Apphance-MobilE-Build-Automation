package com.apphance.flow.plugins

import spock.lang.Specification

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_ANALYSIS
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_APPHANCE_SERVICE
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_BUILD
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_SETUP
import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class FlowTasksGroupsSpec extends Specification {

    def 'flow task description'() {
        expect:
        group.toString() == desc

        where:
        group                 | desc
        FLOW_BUILD            | 'Apphance Flow build'
        FLOW_TEST             | 'Apphance Flow test'
        FLOW_RELEASE          | 'Apphance Flow relese'
        FLOW_SETUP            | 'Apphance Flow setup'
        FLOW_ANALYSIS         | 'Apphance Flow analysis'
        FLOW_APPHANCE_SERVICE | 'Apphance Service'
    }
}
