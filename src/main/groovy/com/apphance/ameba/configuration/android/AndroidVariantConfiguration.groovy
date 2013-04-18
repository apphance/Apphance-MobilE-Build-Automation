package com.apphance.ameba.configuration.android

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.apphance.ApphanceMode
import com.apphance.ameba.configuration.properties.ApphanceModeProperty
import com.apphance.ameba.configuration.properties.StringProperty
import com.apphance.ameba.configuration.reader.PropertyPersister
import org.gradle.api.Project

class AndroidVariantConfiguration extends AbstractConfiguration {

    final String name
    private AndroidConfiguration androidConf
    private AndroidApphanceConfiguration androidApphanceConf

    AndroidVariantConfiguration(String name,
                                PropertyPersister persister,
                                AndroidConfiguration androidConf,
                                AndroidApphanceConfiguration androidApphanceConf,
                                Project project) {
        this.propertyPersister = persister
        this.name = name
        this.androidConf = androidConf
        this.androidApphanceConf = androidApphanceConf
        this.project = project

        initFields()
    }

    private void initFields() {
        mode.name = "android.variant.${name}.mode"
        mode.message = "Android variant '$name' mode"
        apphanceAppKey.name = "android.variant.${name}.apphance.appKey"
        apphanceAppKey.message = "Apphance key for '$name'"
        apphanceMode.name = "android.variant.${name}.apphance.mode"
        apphanceMode.message = "Apphance mode for '$name'"
        apphanceLibVersion.name = "android.variant.${name}.apphance.lib"
        apphanceLibVersion.message = "Apphance lib version for '$name'"
    }

    def mode = new StringProperty(
            possibleValues: { AndroidBuildMode.values()*.name() as List<String> }
    )

    def apphanceAppKey = new StringProperty(
            askUser: { androidApphanceConf.enabled }
    )

    def apphanceMode = new ApphanceModeProperty(
            possibleValues: { ApphanceMode.values()*.name() as List<String> },
            askUser: { androidApphanceConf.enabled }
    )

    def apphanceLibVersion = new StringProperty(
            askUser: { androidApphanceConf.enabled }
    )

    @Override
    boolean isEnabled() {
        androidConf.enabled
    }

    @Override
    String getConfigurationName() {
        "Android configuration for variant: ${this.@name}"
    }

    File getTmpDir() {
        def rootDir = androidConf.rootDir
        new File(rootDir.parent, ("tmp-${rootDir.name}-" + name).replaceAll('[\\\\ /]', '_'))
    }

}
