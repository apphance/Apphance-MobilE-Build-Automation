package com.apphance.ameba.configuration

interface PropertyPersister {

    def abstract get(String name)
    def abstract save(Collection<Configuration> configurations)
}
