package com.apphance.flow.configuration.properties

class FileProperty extends AbstractProperty<File> {

    private Closure<Boolean> internalValidator

    @Override
    void setValue(String value) {
        value = value?.trim()
        if (value)
            this.@value = new File(value)
    }

    @Override
    void setValidator(Closure<Boolean> validator) {
        internalValidator = validator
    }

    @Override
    Closure<Boolean> getValidator() {
        internalValidator ?: { it ? new File(it as String).exists() : false }
    }
}