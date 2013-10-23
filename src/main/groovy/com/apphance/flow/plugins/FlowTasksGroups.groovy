package com.apphance.flow.plugins

enum FlowTasksGroups {

    FLOW_BUILD('Apphance Flow build'),
    FLOW_TEST('Apphance Flow test'),
    FLOW_RELEASE('Apphance Flow relese'),
    FLOW_SETUP('Apphance Flow setup'),
    FLOW_ANALYSIS('Apphance Flow analysis'),
    FLOW_APPHANCE_SERVICE('Apphance Service');

    String name

    FlowTasksGroups(String name) {
        this.name = name
    }

    @Override
    public String toString() {
        name
    }
}
