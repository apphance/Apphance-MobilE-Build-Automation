package com.apphance.ameba.android

import org.gradle.api.Project

/**
 * Stores android build environment properties.
 */
class AndroidEnvironment {

    Properties androidProperties

    private static final PROPERTIES_FILES = [
            'local',
            'build',
            'default',
            'project'
    ]

    AndroidEnvironment(Project project) {
        this.androidProperties = loadAndroidProperties(project)
    }

    Properties loadAndroidProperties(project) {
        def props = new Properties()
        PROPERTIES_FILES.each {
            File propFile = project.file("${it}.properties")
            if (propFile.exists()) {
                props.load(new FileInputStream(propFile))
            }
        }
        props
    }

    String getAndroidProperty(String name) {
        androidProperties.get(name)
    }

    boolean isLibrary() {
        (getAndroidProperty('android.library') == 'true')
    }
}
