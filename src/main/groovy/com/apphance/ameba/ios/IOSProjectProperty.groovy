package com.apphance.ameba.ios
public enum IOSProjectProperty {
    PLIST_FILE(false, 'ios.plist.file', 'Path to plist file'),
    EXCLUDED_BUILDS(false, 'ios.excluded.builds', 'List of excluded builds'),
    IOS_FAMILIES(false, 'ios.families', 'List of iOS families'),
    DISTRIBUTION_DIR(false, 'ios.distribution.resources.dir', 'Path to distribution resources directory'),
    MAIN_TARGET(true, 'ios.mainTarget', 'Main target for release build'),
    MAIN_CONFIGURATION(true, 'ios.mainConfiguration', 'Main configuration for release build'),
    IOS_SDK(true, 'ios.sdk', 'List of iOS SDKs'),
    IOS_SIMULATOR_SDK(true, 'ios.simulator.sdk', 'List of iOS simulator SDKs'),

    final boolean optional
    final String propertyName
    final String description

    IOSProjectProperty(boolean optional, String propertyName, String description) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
    }
}
