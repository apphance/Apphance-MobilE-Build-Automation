package com.apphance.flow.configuration.properties

import com.apphance.flow.configuration.apphance.ApphanceMode

import static org.apache.commons.lang.StringUtils.isNotBlank

class ApphanceModeProperty extends AbstractProperty<ApphanceMode> {

    @Override
    void setValue(String value) {
        if (isNotBlank(value))
            this.@value = ApphanceMode.valueOf(value.trim())
    }
}
