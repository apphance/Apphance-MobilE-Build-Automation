package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty

import static java.lang.System.out
import static org.apache.commons.lang.StringUtils.isBlank
import static org.gradle.api.logging.Logging.getLogger

class ConversationManager {

    private log = getLogger(getClass())

    def reader = buildReader()

    def resolveConfigurations(Collection<? extends AbstractConfiguration> configurations) {
        configurations.each { AbstractConfiguration c ->
            enablePlugin(c)
            readValues(c)
        }

        log.info('All configurations resolved')
        configurations.each { log.info(it.toString()) }
    }

    @groovy.transform.PackageScope
    void enablePlugin(AbstractConfiguration conf) {
        if (!conf.enabled) {
            if (conf.canBeEnabled()) {
                print "Enable plugin ${conf.configurationName}? [y/n] "
                out.flush()
                if (reader.readLine()?.equalsIgnoreCase('y')) {
                    conf.enabled = true
                }
            } else {
                print conf.message
                out.flush()
            }
        }
    }

    @groovy.transform.PackageScope
    void readValues(AbstractConfiguration c) {
        if (c.enabled) {
            readProperties(c.amebaProperties)
            if (!c.subConfigurations?.empty) {
                resolveConfigurations(c.subConfigurations)
            }
        }
    }

    private void readProperties(List<AbstractProperty> properties) {
        properties.each {
            readProperty(it)
        }
    }

    private void readProperty(AbstractProperty ap) {
        if (ap.interactive()) {
            String input

            while (true) {
                print prompt(ap)
                out.flush()
                input = reader.readLine()
                if (validateInput(input, ap))
                    break
            }

            setPropertyValue(ap, input)
        }
    }

    @groovy.transform.PackageScope
    String prompt(AbstractProperty ap) {
        "${ap.message}, default: '${defaultValueString(ap)}'${possibleValuesString(ap)}: "
    }

    @groovy.transform.PackageScope
    String defaultValueString(AbstractProperty ap) {
        ap.value ?: ap?.defaultValue() ?: ap.possibleValues() ? ap.possibleValues().get(0) : ''
    }

    @groovy.transform.PackageScope
    String possibleValuesString(AbstractProperty ap) {
        ap.possibleValues() ? ", possible: ${ap.possibleValues()}" : ''
    }

    @groovy.transform.PackageScope
    boolean validateInput(String input, AbstractProperty ap) {
        isBlank(input) && (defaultValueString(ap) || !ap.required()) || input in ap.possibleValues() || ap.validator(input)
    }

    @groovy.transform.PackageScope
    void setPropertyValue(AbstractProperty ap, String input) {
        ap.value = input?.empty ? ap.value ?: ap?.defaultValue() ?: null : input
        log.info("Property '${ap.name}' value set to: ${ap.value}")
    }

    private Reader buildReader() {
        new BufferedReader(new InputStreamReader(System.in))
    }
}
