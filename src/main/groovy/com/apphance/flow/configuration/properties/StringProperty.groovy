package com.apphance.flow.configuration.properties

class StringProperty extends AbstractProperty<String> {

    @Override
    void setValue(String value) {
        if (value?.trim())
            this.@value = value
    }
}
