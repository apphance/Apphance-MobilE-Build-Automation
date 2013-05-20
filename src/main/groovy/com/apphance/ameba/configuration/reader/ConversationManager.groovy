package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty

import static java.lang.System.out
import static org.apache.commons.lang.StringUtils.isBlank
import static org.gradle.api.logging.Logging.getLogger

class ConversationManager {

    private log = getLogger(getClass())

    def reader = new BufferedReader(new InputStreamReader(System.in))

    def resolveConfigurations(Collection<? extends AbstractConfiguration> configurations) {
        configurations.each { AbstractConfiguration c ->
            c.enabled ? readValues(c) : enablePlugin(c)
        }

        log.info('All configurations resolved')
        configurations.each { log.info(it.toString()) }
    }

    @groovy.transform.PackageScope
    void enablePlugin(AbstractConfiguration conf) {
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

    @groovy.transform.PackageScope
    void readValues(AbstractConfiguration conf) {
        conf.amebaProperties.findAll { it.interactive() }.each {
            readProperty(it)
        }
        if (!conf.subConfigurations?.empty) {
            resolveConfigurations(conf.subConfigurations)
        }
    }

    private void readProperty(AbstractProperty ap) {
        while (true) {
            print prompt(ap)
            out.flush()
            def input = reader.readLine()
            if (validateInput(input, ap)) {
                setPropertyValue(ap, input)
                break
            }
        }
    }

    @groovy.transform.PackageScope
    boolean validateInput(String input, AbstractProperty ap) {
        if (isBlank(input)) {
            effectiveDefaultValue(ap) || !ap.required()
        } else {
            ap.validator(input)
        }
    }

    @groovy.transform.PackageScope
    void setPropertyValue(AbstractProperty ap, String input) {
        ap.value = isBlank(input) ? effectiveDefaultValue(ap) : input

        log.info("Property '${ap.name}' value set to: ${ap.value}")
    }

    @groovy.transform.PackageScope
    String effectiveDefaultValue(AbstractProperty ap) {
        ap.value ?: ap?.defaultValue() ?: ap.possibleValues() ? ap.possibleValues().get(0) : ''
    }

    @groovy.transform.PackageScope
    String prompt(AbstractProperty ap) {
        "${ap.message}, default: '${effectiveDefaultValue(ap)}'${possibleValuesString(ap)}: "
    }

    @groovy.transform.PackageScope
    String possibleValuesString(AbstractProperty ap) {
        ap.possibleValues() ? ", possible: ${ap.possibleValues()}" : ''
    }
}
