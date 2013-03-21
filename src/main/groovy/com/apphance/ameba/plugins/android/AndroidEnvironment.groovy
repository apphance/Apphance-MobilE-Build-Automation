package com.apphance.ameba.plugins.android

import org.gradle.api.Project

/**
 * Stores android build environment properties.
 */
class AndroidEnvironment {

    private static final PROPERTIES_FILES = [
            'local',
            'build',
            'default',
            'project'
    ]

    private Properties androidProperties

    AndroidEnvironment(Project project) {
        this.androidProperties = loadAndroidProperties(project)
    }

    private Properties loadAndroidProperties(project) {
        def props = new Properties()
        PROPERTIES_FILES.each {
            File propFile = project.file("${it}.properties")
            if (propFile.exists()) {
                props.load(new FileInputStream(propFile))
            }
        }
        props
    }

    boolean isLibrary() {
        getAndroidProperty('android.library') == 'true'
    }

    String getAndroidProperty(String name) {
        androidProperties.get(name)
    }
}
