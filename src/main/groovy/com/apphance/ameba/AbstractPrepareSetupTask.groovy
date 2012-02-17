package com.apphance.ameba

import groovy.io.FileType;

import java.util.List;

import org.gradle.api.DefaultTask;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging

import com.apphance.ameba.AmebaCommonBuildTaskGroups;
import com.apphance.ameba.PropertyCategory;


class AbstractPrepareSetupTask extends DefaultTask {

    private static BufferedReader br = null
    public static BufferedReader getReader() {
        if (br == null) {
            br = new BufferedReader(new InputStreamReader(System.in))
        }
        return br
    }

    public static final String GENERATED_GRADLE_PROPERTIES = 'generated.gradle.properties'
    Logger logger = Logging.getLogger(AbstractPrepareSetupTask.class)
    String propertyDescription
    Class<? extends Enum> clazz

    AbstractPrepareSetupTask(Class<? extends Enum> clazz) {
        use (PropertyCategory) {
            this.clazz = clazz
            this.propertyDescription = clazz.getField('DESCRIPTION').get(null)
            this.group = AmebaCommonBuildTaskGroups.AMEBA_SETUP
            this.description = "Walks you through setup of the ${propertyDescription} of the project."
            //inject myself as dependency for umbrella prepareSetup
            project.prepareSetup.dependsOn(this)
            this.dependsOn(project.readProjectConfiguration)
        }
    }

    void appendProperties() {
        use (PropertyCategory) {
            String propertyString = project.listPropertiesAsString(clazz, false)
            String oldValue = project.readProperty(GENERATED_GRADLE_PROPERTIES, '')
            String newValue = oldValue + propertyString
            project[GENERATED_GRADLE_PROPERTIES] = newValue
        }
    }

    List getFiles(Closure filter) {
        List paths = [
            new File(project.rootDir,'bin').absolutePath,
            new File(project.rootDir,'build').absolutePath,
            new File(project.rootDir,'ota').absolutePath,
            new File(project.rootDir,'tmp').absolutePath,
        ]
        def plistFiles = []
        project.rootDir.traverse([type: FileType.FILES, maxDepth : 7]) {
            def thePath = it.absolutePath
            if (filter(it)) {
                if (!paths.any {path -> thePath.startsWith(path)}) {
                    plistFiles << thePath.substring(project.rootDir.path.length() + 1)
                }
            }
        }
        return plistFiles
    }
}
