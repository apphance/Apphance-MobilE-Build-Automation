package com.apphance.ameba.configuration.properties


class URLProperty extends AbstractProperty<URL> {

    @Override
    void setValue(String value) {
        if (value)
            this.@value = value.toURL()
    }

    @Override
    Closure<Boolean> getValidator() {
        if (!super.validator) {
            return super.validator
        }
        return {
            try {
                it.toURL()
            } catch (e) {
                return false
            }
            return true
        }
    }
}
