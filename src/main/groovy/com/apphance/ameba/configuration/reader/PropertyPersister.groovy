package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration

interface PropertyPersister {

    def abstract get(String name)
    def abstract save(Collection<AbstractConfiguration> configurations)
}
