package com.apphance.ameba.unit;

import static org.junit.Assert.*

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.AfterClass;
import org.junit.Test

import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.plugins.release.ProjectReleaseProperty

class ReleasePropertyTest {

    @Test
    void testReleasePropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE.propertyName] = "Icon.png"
            project[ProjectReleaseProperty.RELEASE_PROJECT_URL.propertyName] = "http://example.com/subproject"
            project[ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE.propertyName] = "pl"
            project[ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY.propertyName] = "PL"
            project[ProjectReleaseProperty.RELEASE_MAIL_FROM.propertyName] = "test@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_TO.propertyName] = "no-reply@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_FLAGS.propertyName] = "qrCode,imageMontage"
            String s = project.listPropertiesAsString(ProjectReleaseProperty.class, false)
            assertEquals('''###########################################################
# Release properties
###########################################################
release.project.icon.file=Icon.png
release.project.url=http://example.com/subproject
release.project.language=pl
release.project.country=PL
release.mail.from=test@apphance.com
release.mail.to=no-reply@apphance.com
release.mail.flags=qrCode,imageMontage
''',s)
        }
    }

    @Test
    void testReleasePropertyWithComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[ProjectReleaseProperty.RELEASE_PROJECT_ICON_FILE.propertyName] = "Icon.png"
            project[ProjectReleaseProperty.RELEASE_PROJECT_URL.propertyName] = "http://example.com/subproject"
            project[ProjectReleaseProperty.RELEASE_PROJECT_LANGUAGE.propertyName] = "pl"
            project[ProjectReleaseProperty.RELEASE_PROJECT_COUNTRY.propertyName] = "PL"
            project[ProjectReleaseProperty.RELEASE_MAIL_FROM.propertyName] = "test@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_TO.propertyName] = "no-reply@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_FLAGS.propertyName] = "qrCode,imageMontage"
            String s = project.listPropertiesAsString(ProjectReleaseProperty.class, true)
            assertEquals('''###########################################################
# Release properties
###########################################################
# Path to project's icon file
release.project.icon.file=Icon.png
# Base project URL where the artifacts will be placed. This should be folder URL where last element (after last /) is used as subdirectory of ota dir when artifacts are created locally.
release.project.url=http://example.com/subproject
# Language of the project [optional] default: <en>
release.project.language=pl
# Project country [optional] default: <US>
release.project.country=PL
# Sender email address
release.mail.from=test@apphance.com
# Recipient of release email
release.mail.to=no-reply@apphance.com
# Flags for release email [optional] default: <qrCode,imageMontage>
release.mail.flags=qrCode,imageMontage
''',s)
        }
    }
}
