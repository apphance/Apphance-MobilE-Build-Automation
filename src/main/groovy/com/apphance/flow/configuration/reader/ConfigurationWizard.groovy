package com.apphance.flow.configuration.reader

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.configuration.properties.AbstractProperty
import groovy.transform.PackageScope
import org.gradle.api.GradleException

import static java.lang.System.out
import static org.apache.commons.lang.StringUtils.isBlank
import static org.gradle.api.logging.Logging.getLogger

class ConfigurationWizard {

    def logger = getLogger(this.class)
    def static YELLOW = '\033[93m'
    def static END = '\033[0m'
    def static GREEN = '\033[92m'
    def static BLUE = '\033[94m'

    def reader = new BufferedReader(new InputStreamReader(System.in))

    boolean interactiveMode = true

    static String color(String color, String str) {
        "${color}${str}${END}"
    }

    public static Closure<String> yellow = this.&color.curry(YELLOW)
    public static Closure<String> green = this.&color.curry(GREEN)
    public static Closure<String> blue = this.&color.curry(BLUE)

    def resolveConfigurations(Collection<? extends AbstractConfiguration> configurations) {
        configurations.each { AbstractConfiguration conf ->
            println "\nConfiguring $conf.configurationName"
            if (!conf.enabled && interactiveMode) {
                enablePlugin(conf)
            }
            if (conf.enabled) {
                readValues(conf)
            }
        }

        logger.info('All configurations resolved')
        configurations.each { logger.info(it.toString()) }
    }

    @PackageScope
    void enablePlugin(AbstractConfiguration conf) {
        if (conf.canBeEnabled()) {
            print "Enable plugin ${green(conf.configurationName)}? [y/n] "
            out.flush()
            conf.enabled = reader.readLine()?.equalsIgnoreCase('y')
        } else {
            print conf.explainDisabled()
            out.flush()
        }
    }

    @PackageScope
    void readValues(AbstractConfiguration conf) {
        conf.propertyFields.each {
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
            def input = getInput()
            if (validateInput(input, ap)) {
                setPropertyValue(ap, input)
                if (!interactiveMode) println ''
                break
            } else {
                if (!interactiveMode) {
                    throw new GradleException("Cannot set value of property ${ap.name} in non-interacivte mode. No sensible default value")
                }
                println yellow(ap.failedValidationMessage)
                out.flush()
            }
        }
    }

    @PackageScope
    String getInput() {
        interactiveMode ? reader.readLine() : ''
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
        logger.info("Property '${ap.name}' value set to: ${ap.value}")
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
