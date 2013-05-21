package com.apphance.ameba.configuration.properties

import com.apphance.ameba.configuration.ios.IOSBuildMode

import static org.apache.commons.lang.StringUtils.isNotBlank

class IOSBuildModeProperty extends AbstractProperty<IOSBuildMode> {

    @Override
    void setValue(String value) {
        if (isNotBlank(value))
            this.@value = IOSBuildMode.valueOf(value.trim())
    }
}
