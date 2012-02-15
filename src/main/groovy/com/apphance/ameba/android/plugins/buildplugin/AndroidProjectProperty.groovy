package com.apphance.ameba.android.plugins.buildplugin;

public enum AndroidProjectProperty {

    MAIN_VARIANT('android.mainVariant',"Main variant used when releasing the aplication"),
    EXCLUDED_BUILDS('android.excluded.builds', "Regular expressions separated with comas - if variant name matches any of these, it is excluded from configuration",''),
    MIN_SDK_TARGET('android.minSdk.target', "Minimum target against which source code analysis is done - the project will fail Java compilation in case classes from higher target are used",'')
    public static final String DESCRIPTION = 'Android properties'
    final String propertyName
    final String description
    final String defaultValue

    AndroidProjectProperty(String propertyName, String description, String defaultValue = null) {
        this.propertyName = propertyName
        this.description = description
        this.defaultValue = defaultValue
    }
}