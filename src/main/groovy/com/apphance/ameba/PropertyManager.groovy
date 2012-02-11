package com.apphance.ameba

import org.gradle.api.Project

class PropertyManager {

    public static List<String> listProperties(Project project, def properties, boolean useComments) {
        String description = properties.getField('DESCRIPTION').get(null)
        List<String> s = []
        s << "###########################################################"
        s << "# ${description}"
        s << "###########################################################"
        properties.each {
            String comment = '# ' + it.description
            String propString = it.propertyName + '='
            comment = comment + (it.optional ? ' [optional]':  ' [required]')
            if (project.hasProperty(it.propertyName)) {
                propString = propString + project[it.propertyName]
            }
            if (useComments == true) {
                s << comment
            }
            s << propString
        }
        return s
    }

    public static String listPropertiesAsString(Project project, def properties, boolean useComments) {
        StringBuffer sb = new StringBuffer()
        listProperties(project, properties, useComments).each { sb << it + '\n' }
        return sb.toString()
    }
}
