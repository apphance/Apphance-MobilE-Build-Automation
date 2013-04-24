package com.apphance.ameba.configuration.properties

class LongProperty extends AbstractProperty<Long> {

    @Override
    void setValue(String value) {
        this.@value = value?.isNumber() ? value.toLong() : null
    }
}
