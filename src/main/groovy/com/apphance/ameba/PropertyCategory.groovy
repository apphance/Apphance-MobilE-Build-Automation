package com.apphance.ameba

import java.io.BufferedReader
import java.util.ArrayList
import java.util.List

import org.gradle.api.GradleException
import org.gradle.api.Project

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin

class PropertyCategory {


    private static Map<String, String> getDefaultConventionValues(Project project) {
        if (project.convention.plugins.amebaPropertyDefaults != null) {
            def defaultConventionValues = Eval.me(project.convention.plugins.amebaPropertyDefaults.defaults)
            return defaultConventionValues
        } else {
            return [:]
        }
    }

    public static final String PROJECT_CONFIGURATION_KEY = 'project.configuration'
    public static List<String> listProperties(Project project, Class<Enum> properties, boolean useComments, Properties extraProperties = null) {
        String description = properties.getField('DESCRIPTION').get(null)
        List<String> s = []
        s << "###########################################################"
        s << "# ${description}"
        s << "###########################################################"
        properties.each {
            def defaultValue = getDefaultForProperty(project, it)
            String comment = '# ' + it.description
            String propString = it.propertyName + '='
            if (defaultValue != null) {
                comment = comment + " [optional] default: <${defaultValue}>"
            }
            if (project.hasProperty(it.propertyName)) {
                String propertyValue = project[it.propertyName]
                propString = propString + propertyValue
            } else if (properties != null && extraProperties != null) {
                String propertyValue = extraProperties.get(it.propertyName)
                propString = propString + propertyValue
            }
            if (useComments == true) {
                s << comment
            }
            s << propString
        }
        return s
    }

    private static String getDefaultForProperty(Project project, property) {
        def defaultMap = getDefaultConventionValues(project)
        def defaultValue = property.defaultValue
        if (defaultMap[property.propertyName] != null) {
            defaultValue = defaultMap[property.propertyName]
        }
        return defaultValue
    }


    public static String listPropertiesAsString(Project project, Class<Enum> properties, boolean useComments) {
        StringBuffer sb = new StringBuffer()
        listProperties(project, properties, useComments).each { sb << it + '\n' }
        return sb.toString()
    }

    public static Object readProperty(Project project, String propertyName, Object defaultValue = null) {
        if (project.hasProperty(propertyName)) {
            return project[propertyName]
        } else {
            return defaultValue
        }
    }

    public static Object readProperty(Project project, Enum property) {
        return readProperty(project, property.propertyName, getDefaultForProperty(project, property))
    }

    public static String getProjectPropertyFromUser(Project project, Enum property,
    ArrayList options, BufferedReader br) {
        def defaultValue = getDefaultForProperty(project, property)
        String s = "Enter ${property.propertyName}\n${property.description}"
        if (options != null) {
            s = s + '\nProposed values: ' + options
        }
        if (project.hasProperty(property.propertyName)) {
            // skip setting = we already have some value
        } else if (defaultValue != null) {
            // or choose default if exists
            project[property.propertyName] = defaultValue
        } else if (options != null) {
            // or pre-select first option if present
            if (options.size > 0 && options[0] != null) {
                project[property.propertyName] = options[0]
            }
        }
        System.out.println(s)
        if (project.hasProperty(property.propertyName) && project[property.propertyName] != null) {
            System.out.print("[${project[property.propertyName]}] > ")
            System.out.flush()
        } else {
            System.out.print(" > ")
            System.out.flush()
        }
        String newValue = br.readLine()
        if (newValue == null) {
            throw new GradleException("Entering data has been stopped at reading ${property.propertyName}")
        }
        if (!newValue.isEmpty()) {
            project[property.propertyName] = newValue
        }
    }

    public static String readPropertyOrEnvironmentVariable(Project project, Enum property) {
        return readPropertyOrEnvironmentVariable(project, property.propertyName)
    }

    public static String readPropertyOrEnvironmentVariable(Project project, String propertyName) {
        if (project.hasProperty(propertyName)) {
            return project[propertyName]
        } else if (System.getProperty(propertyName) != null){
            return System.getProperty(propertyName)
        } else {
            def envVariable = propertyName.toUpperCase().replace(".","_")
            def val = System.getenv(envVariable)
            if (val == null) {
                throw new GradleException("The property ${propertyName} was not defined (neither in project nor system) and ${envVariable} environment variable is missing.")
            }
            return val
        }
    }

    public static String isPropertyOrEnvironmentVariableDefined(Project project, Enum property) {
        return isPropertyOrEnvironmentVariableDefined(project, property.propertyName)
    }

    public static boolean isPropertyOrEnvironmentVariableDefined(Project project, String property) {
        if (project.hasProperty(property)) {
            return true
        } else if (System.getProperty(property) != null){
            return true
        } else {
            def envVariable = property.toUpperCase().replace(".","_")
            def val = System.getenv(envVariable)
            return (val != null)
        }
    }
    public static String readExpectedProperty(Project project, Enum property) {
        return readExpectedProperty(project, property.propertyName)
    }

    public static String readExpectedProperty(Project project, String property) {
        if (!project.hasProperty(property)) {
            throw new GradleException("I need ${property} property to be set on project")
        }
        return project[property]
    }

    public static ProjectConfiguration getProjectConfiguration(Project project){
        if (!project.ext.has(PROJECT_CONFIGURATION_KEY)) {
            project.ext[PROJECT_CONFIGURATION_KEY] = new ProjectConfiguration()
        }
        return project.ext[PROJECT_CONFIGURATION_KEY]
    }

    public static ProjectConfiguration retrieveBasicProjectData(Project project) {
        use (PropertyCategory) {
            ProjectConfiguration conf = getProjectConfiguration(project)
            conf.projectName = project.readProperty(ProjectConfigurationPlugin.PROJECT_NAME_PROPERTY)
            conf.tmpDirectory = project.file('tmp')
            conf.logDirectory = project.file('log')
            conf.buildDirectory = project.file('build')
            return conf
        }
    }
}
