package com.apphance.ameba

import groovy.io.FileType;

import java.util.List;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.PropertyCategory;


/**
 * Abstract class that is superclass for all 'prepare setup' class.
 * It provides framework for setting values of properties for various plugins.
 *
 */
abstract class AbstractPrepareSetupOperation {

    private static BufferedReader br = null
    public static BufferedReader getReader() {
        if (br == null) {
            br = new BufferedReader(new InputStreamReader(System.in))
        }
        return br
    }


    public static final String GENERATED_GRADLE_PROPERTIES = 'generated.gradle.properties'
    Logger logger = Logging.getLogger(AbstractPrepareSetupOperation.class)
    String propertyDescription
    Class<? extends Enum> clazz
    Project project

    abstract void prepareSetup()

    AbstractPrepareSetupOperation(Class<? extends Enum> clazz) {
        this.clazz = clazz
        this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
    }

    void appendProperties() {
        use (PropertyCategory) {
            String propertyString = project.listPropertiesAsString(clazz, false)
            String oldValue = project.readProperty(GENERATED_GRADLE_PROPERTIES, '')
            String newValue = oldValue + propertyString
            project[GENERATED_GRADLE_PROPERTIES] = newValue
        }
    }

}
