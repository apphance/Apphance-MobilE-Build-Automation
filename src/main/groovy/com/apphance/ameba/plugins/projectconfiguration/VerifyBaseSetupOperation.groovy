package com.apphance.ameba.plugins.projectconfiguration

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import com.apphance.ameba.AbstractVerifySetupOperation
import com.apphance.ameba.PropertyCategory

class VerifyBaseSetupOperation extends  AbstractVerifySetupOperation {
    Logger logger = Logging.getLogger(VerifyBaseSetupOperation.class)

    VerifyBaseSetupOperation() {
        super(BaseProperty.class)
    }

    void verifySetup() {
        def projectProperties = readProperties()
        BaseProperty.each { checkProperty(projectProperties, it) }
        checkIconFile(projectProperties)
        checkUrl(projectProperties)
        checkLanguage(projectProperties)
        checkCountry(projectProperties)
        allPropertiesOK()
    }

    private checkLanguage(Properties projectProperties) {
        use (PropertyCategory) {
            String language = project.readProperty(BaseProperty.PROJECT_LANGUAGE)
            if (language.length() != 2 || language.toLowerCase() != language) {
                throw new GradleException("The ${BaseProperty.PROJECT_LANGUAGE.propertyName}: ${language} property is not a valid language: should be 2 letter lowercase")
            }
        }
    }

    private checkCountry(Properties projectProperties) {
        use (PropertyCategory) {
            String country = project.readProperty(BaseProperty.PROJECT_COUNTRY)
            if (country.length() != 2 || country.toUpperCase() != country) {
                throw new GradleException("The ${BaseProperty.PROJECT_COUNTRY.propertyName}: ${country} property is not a valid country: should be 2 letter UPPERCASE")
            }
        }
    }


    private checkUrl(Properties projectProperties) {
        use (PropertyCategory) {
            String urlString = project.readProperty(BaseProperty.PROJECT_URL)
            try {
                URL url = new URL(urlString)
            } catch( MalformedURLException e) {
                throw new GradleException("The ${BaseProperty.PROJECT_URL.propertyName}:${urlString} property is not a valid URL: ${e}")
            }
        }
    }

    void checkIconFile(Properties projectProperties) {
        use (PropertyCategory) {
            String iconPath = project.readProperty(BaseProperty.PROJECT_ICON_FILE)
            if (iconPath != null && !iconPath.empty) {
                File iconFile = new File(project.rootDir,iconPath)
                if (!iconFile.exists() || !iconFile.isFile()) {
                    throw new GradleException("""The icon file property ${BaseProperty.PROJECT_ICON_FILE.propertyName}: ${iconFile}) does not exist
        or is not a file. Please run 'gradle prepareSetup' to correct it.""")
                }
            }
        }
    }
}
