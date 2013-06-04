package com.apphance.ameba.configuration.reader

import com.apphance.ameba.configuration.AbstractConfiguration
import com.apphance.ameba.configuration.properties.AbstractProperty
import groovy.transform.PackageScope

import static java.lang.System.out
import static org.apache.commons.lang.StringUtils.isBlank
import static org.gradle.api.logging.Logging.getLogger

class ConfigurationWizard {

    private log = getLogger(getClass())
    def static YELLOW = '\033[93m'
    def static END = '\033[0m'
    def static GREEN = '\033[92m'
    def static BLUE = '\033[94m'

    def reader = new BufferedReader(new InputStreamReader(System.in))

    static String color(String color, String str) {
        "${color}${str}${END}"
    }

    public static Closure<String> yellow = this.&color.curry(YELLOW)
    public static Closure<String> green = this.&color.curry(GREEN)
    public static Closure<String> blue = this.&color.curry(BLUE)

    static String removeColor(String str) {
        str.replaceAll(/\033\[[0-9;]*m/, '')
    }

    def resolveConfigurations(Collection<? extends AbstractConfiguration> configurations) {
        configurations.each { AbstractConfiguration c ->
            if (!c.enabled) {
                enablePlugin(c)
            }
            if (c.enabled) {
                readValues(c)
            }
        }

        log.info('All configurations resolved')
        configurations.each { log.info(it.toString()) }
    }

    @PackageScope
    void enablePlugin(AbstractConfiguration conf) {
        if (conf.canBeEnabled()) {
            print "Enable plugin ${green(conf.configurationName)}? [y/n] "
            out.flush()
            conf.enabled = reader.readLine()?.equalsIgnoreCase('y')
        } else {
            print conf.message
            out.flush()
        }
    }

    @PackageScope
    void readValues(AbstractConfiguration conf) {
        conf.amebaProperties.each {
            if (it.interactive()) readProperty(it)
        }
        if (!conf.subConfigurations?.empty) {
            resolveConfigurations(conf.subConfigurations)
        }
    }

    @PackageScope
    void readProperty(AbstractProperty ap) {
        while (true) {
            print prompt(ap)
            out.flush()
            def input = reader.readLine()
            if (validateInput(input, ap)) {
                setPropertyValue(ap, input)
                break
            } else {
                println yellow(ap.failedValidationMessage)
                out.flush()
            }
        }
    }

    @PackageScope
    boolean validateInput(String input, AbstractProperty ap) {
        if (isBlank(input)) {
            ap.effectiveDefaultValue() || !ap.required()
        } else {
            ap.validator(input)
        }
    }

    @PackageScope
    void setPropertyValue(AbstractProperty ap, String input) {
        ap.value = isBlank(input) ? ap.effectiveDefaultValue() : input
        log.info("Property '${ap.name}' value set to: ${ap.value}")
    }

    @PackageScope
    String prompt(AbstractProperty ap) {
        ap.message + promptPossible(ap) + '\n' + promptDefault(ap) + ': '
    }

    @PackageScope
    String promptDefault(AbstractProperty ap) {
        ap.effectiveDefaultValue() ? "default: '${green(ap.effectiveDefaultValue())}'" : ''
    }

    @PackageScope
    String promptPossible(AbstractProperty ap) {
        ap.possibleValues() ? ", possible: ${blue(ap.possibleValues().toString())}" : ''
    }
}
