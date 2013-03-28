package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty

import static java.lang.System.out

class ConversationManager {

    def reader = buildReader()

    def resolveConfigurations(List<Configuration> configurations) {
        configurations.each { Configuration c ->
            enablePlugin(c)
            readValues(c)
        }
    }

    @groovy.transform.PackageScope
    void enablePlugin(Configuration c) {
        if (!c.enabled) {
            print "Enable plugin ${c.configurationName}? [y/n] "
            out.flush()
            if (reader.readLine()?.equalsIgnoreCase('y')) {
                c.enabled = true
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
        "${ap.message}${defaultValueString(ap)}${possibleValuesString(ap)}: "
    }

    @groovy.transform.PackageScope
    String defaultValueString(AbstractProperty ap) {
        (ap.defaultValue && ap.defaultValue()) ? ", default: '${ap.defaultValue()}'" : ''
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
            ap.value = ap.defaultValue()
        } else if (input in ap.possibleValues || (ap.validator && ap.validator(input))) {
            ap.value = input
        }
    }

    private Reader buildReader() {
        new BufferedReader(new InputStreamReader(System.in))
    }
}
