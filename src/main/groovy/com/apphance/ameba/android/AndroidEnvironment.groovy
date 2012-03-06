package com.apphance.ameba.android

import java.util.List;
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
            File propFile = project.file( "${it}.properties")
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

    private List extractTargets(String text) {
        List targets = []
        text.split('\n').each {
            if (it.startsWith('id:') ) {
                def matcher =  (it =~ /id:.*"(.*)"/)
                if (matcher.matches()) {
                    targets << matcher[0][1]
                }
            }
        }
        return targets
    }
}
