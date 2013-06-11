package com.apphance.flow.configuration.properties

import com.apphance.flow.configuration.ios.IOSBuildMode

import static org.apache.commons.lang.StringUtils.isNotBlank

class IOSBuildModeProperty extends AbstractProperty<IOSBuildMode> {

    @Override
    void setValue(String value) {
        if (isNotBlank(value))
            this.@value = IOSBuildMode.valueOf(value.trim())
    }
}
