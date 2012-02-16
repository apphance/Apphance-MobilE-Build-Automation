package com.apphance.ameba.ios.plugins.buildplugin
public enum IOSProjectProperty {
    PLIST_FILE('ios.plist.file', 'Path to plist file of the project',''),
    EXCLUDED_BUILDS('ios.excluded.builds', 'List of excluded builds. These are coma-separated regular expressions (matched against target-configuration)',''),
    IOS_FAMILIES('ios.families', 'List of iOS families used (iPhone/iPad)', 'iPhone,iPad'),
    DISTRIBUTION_DIR('ios.distribution.resources.dir', 'Path to distribution resources directory. In this directory mobile provision file should be placed.'),
    MAIN_TARGET('ios.mainTarget', 'Main target for releaseable build'),
    MAIN_CONFIGURATION('ios.mainConfiguration', 'Main configuration for releaseable build'),
    IOS_SDK('ios.sdk', 'SDK used to build iOS targets (-sdk option of xcodebuild)','iphoneos'),
    IOS_SIMULATOR_SDK('ios.simulator.sdk','SDK used to build simulator targets (-sdk option of xcodebuild)', 'iphonesimulator'),

    public static final DESCRIPTION = 'iOS properties'
    final String propertyName
    final String description
    final String defaultValue

    IOSProjectProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}
