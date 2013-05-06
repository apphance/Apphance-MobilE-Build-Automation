package com.apphance.ameba.configuration

interface Configuration {

    boolean isEnabled()

    void setEnabled(boolean enabled)

    List<String> verify()
}