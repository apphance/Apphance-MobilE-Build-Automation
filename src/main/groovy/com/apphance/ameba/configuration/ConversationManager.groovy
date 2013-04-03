package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty
import com.google.inject.Inject

import static java.lang.System.out
import static org.gradle.api.logging.Logging.getLogger

class ConversationManager {

    private log = getLogger(getClass())

    def reader = buildReader()

    @Inject
    PropertyPersister propertyPersister

    def resolveConfigurations(List<Configuration> configurations) {
        configurations.each { Configuration c ->
            c.init()
            enablePlugin(c)
            readValues(c)
        }

        log.info('All configurations resolved')
        configurations.each { log.info(it.toString()) }
    }

    @groovy.transform.PackageScope
    void enablePlugin(Configuration conf) {
        if (!conf.enabled) {
            print "Enable plugin ${conf.configurationName}? [y/n] "
            out.flush()
            if (reader.readLine()?.equalsIgnoreCase('y')) {
                conf.enabled = true
            }
        }
    }

    @groovy.transform.PackageScope
    void readValues(Configuration c) {
        if (c.enabled) {
            c.amebaProperties.each { AbstractProperty ap ->
                String input
                while (true) {
                    print prompt(ap)
                    out.flush()
                    input = reader.readLine()
                    if (validateInput(ap, input))
                        break
                }
                setPropertyValue(ap, input)
            }
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
        ap.possibleValues ? ", possible: ${ap.possibleValues}" : ''
    }

    @groovy.transform.PackageScope
    boolean validateInput(AbstractProperty ap, String input) {
        input = input?.trim()
        input?.empty || (input in ap.possibleValues) || (ap.validator && ap.validator(input))
    }

    @groovy.transform.PackageScope
    void setPropertyValue(AbstractProperty ap, String input) {
        if (input?.empty) {
            ap.value = defaultValueString(ap)
        } else if (input in ap.possibleValues || (ap.validator && ap.validator(input))) {
            ap.value = input
        }
    }

    private Reader buildReader() {
        new BufferedReader(new InputStreamReader(System.in))
    }
}
