package com.apphance.ameba.plugins.release


import java.util.Properties;

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
    IOSProjectConfiguration iosConf

    VerifyReleaseSetupOperation() {
        super(ProjectReleaseProperty.class)
    }


    void verifySetup() {
        def projectProperties = readProperties()
        iosConf = IOSXCodeOutputParser.getIosProjectConfiguration(project)

        ProjectReleaseProperty.each {
            if (it.defaultValue == null && it != ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE) {
                checkProperty(projectProperties, it)
            }
        }
        checkReleaseMailFlags()
        checkIconFile(projectProperties)
        checkUrl(projectProperties)
        checkLanguage(projectProperties)
        checkCountry(projectProperties)
        checkEmail(ProjectReleaseProperty.RELEASE_MAIL_FROM)
        checkEmail(ProjectReleaseProperty.RELEASE_MAIL_TO)
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

    void checkIconFile(Properties projectProperties) {
        use (PropertyCategory) {
            String iconPath = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE)
            if (iconPath != null && !iconPath.empty) {
                File iconFile = project.file(iconPath)
                if (!iconFile.exists() || !iconFile.isFile()) {
                    throw new GradleException("""The icon file property ${ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE.propertyName}: ${iconFile}) does not exist
        or is not a file. Please run 'gradle prepareSetup' to correct it.""")
                }
            }
        }
    }

    private checkUrl(Properties projectProperties) {
        use (PropertyCategory) {
            String urlString = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_URL)
            try {
                URL url = new URL(urlString)
            } catch( MalformedURLException e) {
                throw new GradleException("The ${ProjectReleaseProperty.RELEASE_PROJECT_URL.propertyName}:${urlString} property is not a valid URL: ${e}")
            }
        }
    }

        private checkLanguage(Properties projectProperties) {
        use (PropertyCategory) {
            String language = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE)
            if (language.length() != 2 || language.toLowerCase() != language) {
                throw new GradleException("The ${ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE.propertyName}: ${language} property is not a valid language: should be 2 letter lowercase")
            }
        }
    }

    private checkCountry(Properties projectProperties) {
        use (PropertyCategory) {
            String country = project.readProperty(ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY)
            if (country.length() != 2 || country.toUpperCase() != country) {
                throw new GradleException("The ${ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY.propertyName}: ${country} property is not a valid country: should be 2 letter UPPERCASE")
            }
        }
    }

}
