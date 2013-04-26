package com.apphance.ameba.configuration.properties

class BooleanProperty extends AbstractProperty<Boolean> {

    @Override
    void setValue(String value) {
        if (value?.trim()) {
            this.@value = value.trim().toBoolean()
        }
    }
}
