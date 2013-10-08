package com.apphance.flow.configuration.ios

enum XCAction {

    BUILD_ACTION('BuildAction'),
    LAUNCH_ACTION('LaunchAction'),
    ARCHIVE_ACTION('ArchiveAction'),
    TEST_ACTION('TestAction'),

    private String xmlNodeName

    XCAction(String xmlNodeName) {
        this.xmlNodeName = xmlNodeName
    }

    String getXmlNodeName() {
        return xmlNodeName
    }
}
