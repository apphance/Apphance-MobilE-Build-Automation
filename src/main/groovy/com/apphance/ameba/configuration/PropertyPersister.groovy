package com.apphance.ameba.configuration

interface PropertyPersister {

    def abstract get(String name)
    def abstract save(List<Configuration> configurations)
}
