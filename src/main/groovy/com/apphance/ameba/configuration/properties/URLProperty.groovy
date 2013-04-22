package com.apphance.ameba.configuration.properties


class URLProperty extends AbstractProperty<URL> {

    private Closure<Boolean> internalValidator
    private String internalValue

    @Override
    void setValue(String value) {
        value = value?.trim()
        if (value)
            this.internalValue = value
    }

    @Override
    URL getValue() {
        internalValue ? internalValue.toURL() : null
    }

    boolean isSet() {
        internalValue?.trim() as boolean
    }

    @Override
    void setValidator(Closure<Boolean> validator) {
        internalValidator = validator
    }

    @Override
    Closure<Boolean> getValidator() {
        internalValidator ?:
            {
                try {
                    it.toURL()
                } catch (e) {
                    return false
                }
                return true
            }
    }
}
