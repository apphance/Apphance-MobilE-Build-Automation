package com.apphance.ameba.android

import java.util.Properties

import org.gradle.api.Project


class AndroidEnvironment {

    Properties androidProperties

    private static final PROPERTIES_FILES = [
        'local',
        'build',
        'default',
        'project'
    ]

    AndroidEnvironment(Project project) {
        def aP = new Properties()
        PROPERTIES_FILES.each {
            File propFile = new File(project.rootDir, "${it}.properties")
            if (propFile.exists()) {
                aP.load(new FileInputStream(propFile))
            }
        }
        this.androidProperties = aP
    }


    String getAndroidProperty(String name) {
        return androidProperties.get(name)
    }

    boolean isLibrary() {
        return(getAndroidProperty('android.library') == 'true')
    }
}
