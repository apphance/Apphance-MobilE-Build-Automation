package com.apphance.ameba.configuration

interface Configuration {

    boolean isEnabled()
    void setEnabled(boolean enabled)

    int getOrder()

    List<AmebaProperty> getAmebaProperties()

    String getPluginName()



}