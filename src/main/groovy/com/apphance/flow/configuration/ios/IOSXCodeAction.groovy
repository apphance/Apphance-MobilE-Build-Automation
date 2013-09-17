package com.apphance.flow.configuration.ios

enum IOSXCodeAction {

    BUILD_ACTION('BuildAction'),
    LAUNCH_ACTION('LaunchAction'),
    ARCHIVE_ACTION('ArchiveAction'),
    TEST_ACTION('TestAction'),

    private String xmlNodeName

    IOSXCodeAction(String xmlNodeName) {
        this.xmlNodeName = xmlNodeName
    }

    String getXmlNodeName() {
        return xmlNodeName
    }
}
