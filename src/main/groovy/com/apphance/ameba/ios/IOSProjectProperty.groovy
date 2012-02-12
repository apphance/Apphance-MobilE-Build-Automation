package com.apphance.ameba.ios
public enum IOSProjectProperty {
    PLIST_FILE(false, 'ios.plist.file', 'Path to plist file of the project'),
    EXCLUDED_BUILDS(false, 'ios.excluded.builds', 'List of excluded builds. These are coma-separated regular expressions (matched against target-configuration)',''),
    IOS_FAMILIES(false, 'ios.families', 'List of iOS families used (iPhone/iPad)', 'iPhone,iPad'),
    DISTRIBUTION_DIR(false, 'ios.distribution.resources.dir', 'Path to distribution resources directory. In this directory mobile provision file should be placed.'),
    MAIN_TARGET(true, 'ios.mainTarget', 'Main target for releaseable build'),
    MAIN_CONFIGURATION(true, 'ios.mainConfiguration', 'Main configuration for releaseable build'),
    IOS_SDK(true, 'ios.sdk', 'SDK used to build iOS targets (-sdk option of xcodebuild)','iphoneos'),
    IOS_SIMULATOR_SDK(true, 'ios.simulator.sdk','SDK used to build simulator targets (-sdk option of xcodebuild)', 'iphonesimulator'),

    public static final DESCRIPTION = 'iOS properties'
    final boolean optional
    final String propertyName
    final String description

    IOSProjectProperty(boolean optional, String propertyName, String description) {
        this.optional = optional
        this.propertyName = propertyName
        this.description = description
    }
}
