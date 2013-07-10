package com.apphance.flow.configuration.ios.variants

public enum IOSXCodeAction {

    LAUNCH_ACTION('LaunchAction'),
    ARCHIVE_ACTION('ArchiveAction')

    private String xmlNodeName

    IOSXCodeAction(String xmlNodeName) {
        this.xmlNodeName = xmlNodeName
    }

    String getXmlNodeName() {
        return xmlNodeName
    }
}
