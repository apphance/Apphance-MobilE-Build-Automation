package com.apphance.ameba


import org.gradle.api.Project;
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.logging.StyledTextOutput;
import org.gradle.logging.StyledTextOutput.Style;
import org.gradle.logging.StyledTextOutputFactory;

abstract class AbstractShowSetupOperation {
    Logger logger = Logging.getLogger(AbstractShowSetupOperation.class)
    final String propertyDescription
    final Class<? extends Enum> clazz
    Project project

    public AbstractShowSetupOperation(Class<? extends Enum> clazz) {
        this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
        this.clazz = clazz
    }

    public void showSetup() {
        use (PropertyCategory) {
            StyledTextOutput o = project.showSetup.services.get(StyledTextOutputFactory).create(this.class)
            List props = project.listProperties(clazz, true)
            props.each {
                if (it.startsWith('#')) {
                    o.withStyle(Style.Info).println(it)
                } else {
                    o.withStyle(Style.Identifier).println(it)
                }
            }
        }
    }
}
