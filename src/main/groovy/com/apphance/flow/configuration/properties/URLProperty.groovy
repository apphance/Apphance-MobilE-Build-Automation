package com.apphance.flow.configuration.properties


class URLProperty extends AbstractProperty<URL> {

    @Override
    void setValue(String val) {
        val = val?.trim()
        if (val)
            this.@value = val.toURL()
    }
}
