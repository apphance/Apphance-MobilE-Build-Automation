package com.apphance.ameba.configuration.properties

import com.apphance.ameba.configuration.apphance.ApphanceMode

class ApphanceModeProperty extends AbstractProperty<ApphanceMode> {

    @Override
    void setValue(String value) {
        if (value)
            this.@value = ApphanceMode.valueOf(value)
    }
}
