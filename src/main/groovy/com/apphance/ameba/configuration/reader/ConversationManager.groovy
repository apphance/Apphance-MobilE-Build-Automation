package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty

import static java.lang.System.out
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
            print "Enable plugin ${conf.configurationName}? [y/n] "
            out.flush()
            if (reader.readLine()?.equalsIgnoreCase('y')) {
                conf.enabled = true
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
                if (validateInput(input, ap)) {
                    break
                }
                println "Invalid input"
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
        ap.value ?: ap?.defaultValue() ?: ''
    }

    @groovy.transform.PackageScope
    String possibleValuesString(AbstractProperty ap) {
        ap.possibleValues ? ", possible: ${ap.possibleValues()}" : ''
    }

    @groovy.transform.PackageScope
    boolean validateInput(String input, AbstractProperty ap) {
        input = input?.trim()
        if (input?.empty) {
            ap.value || ap.defaultValue() || !ap.required()
        } else {
            (ap.possibleValues && input in ap.possibleValues()) || (ap.validator && ap.validator(input))
        }
    }

    @groovy.transform.PackageScope
    void setPropertyValue(AbstractProperty ap, String input) {
        if (input?.empty) {
            if (!ap.value) {
                ap.value = ap?.defaultValue() ?: ''
            }
        } else if (ap.possibleValues && input in ap.possibleValues() || (ap.validator && ap.validator(input))) {
            ap.value = input
        }
        log.info("value was set to ${ap.value}")
    }

    private Reader buildReader() {
        new BufferedReader(new InputStreamReader(System.in))
    }
}
