package com.apphance.ameba.plugins.release


import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.ios.IOSProjectConfiguration;
import com.apphance.ameba.ios.IOSXCodeOutputParser;


class VerifyReleaseSetupOperation extends AbstractVerifySetupOperation {

    public static final def ALL_EMAIL_FLAGS = [
        'installableSimulator',
        'qrCode',
        'imageMontage'
    ]
    IOSXCodeOutputParser iosXCodeOutputParser
    IOSProjectConfiguration iosConf

    VerifyReleaseSetupOperation() {
        super(ProjectReleaseProperty.class)
    }

    
    void verifySetup() {
        def projectProperties = readProperties()
        iosXCodeOutputParser = new IOSXCodeOutputParser()
        iosConf = iosXCodeOutputParser.getIosProjectConfiguration(project)

        ProjectReleaseProperty.each {
            if (!it.defaultValue != null) {
                checkProperty(projectProperties, it)
            }
        }
        checkEmail(ProjectReleaseProperty.RELEASE_MAIL_FROM)
        checkEmail(ProjectReleaseProperty.RELEASE_MAIL_TO)
        checkReleaseMailFlags()
        allPropertiesOK()
    }

    void checkReleaseMailFlags() {
        use (PropertyCategory) {
            String flags = project.readProperty(ProjectReleaseProperty.RELEASE_MAIL_FLAGS)
            if (flags != null) {
                flags.split(',').each {
                    if (!(it in ALL_EMAIL_FLAGS)) {
                        throw new GradleException("The flag in ${ProjectReleaseProperty.RELEASE_MAIL_FLAGS.propertyName}: ${it} is not one of  ${ALL_EMAIL_FLAGS}")
                    }
                }
            }
        }
    }

    void checkEmail(property) {
        use (PropertyCategory) {
            String email = project.readProperty(property)
            if (!(email ==~ /.* *<{0,1}[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[A-Za-z]{2,4}>{0,1}/)) {
                throw new GradleException("The email in ${property.propertyName}: ${email} is not valid")
            }
        }
    }
}
