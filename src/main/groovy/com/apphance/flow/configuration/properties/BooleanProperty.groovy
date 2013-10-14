package com.apphance.flow.configuration.properties

import static com.google.common.base.Preconditions.checkArgument
import static org.apache.commons.lang.StringUtils.isNotEmpty

class BooleanProperty extends AbstractProperty<Boolean> {

    @Override
    void setValue(String value) {
        value = value?.trim()
        if (isNotEmpty(value)) {
            checkArgument(value in POSSIBLE_BOOLEAN, "Invalid boolean value ($value) of property $name!")
            this.@value = value.toBoolean()
        }
    }

    public final static List<String> POSSIBLE_BOOLEAN = ['true', 'false']
}
