package com.apphance.ameba.unit;

import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test



import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.plugins.projectconfiguration.BaseProperty;
import com.apphance.ameba.plugins.release.ProjectReleaseProperty

class IOSPropertyTest {
    @Test
    void testBasePropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[BaseProperty.PROJECT_ICON_FILE.propertyName] = "Icon.png"
            project[BaseProperty.PROJECT_URL.propertyName] = "http://example.com"
            project[BaseProperty.PROJECT_DIRECTORY.propertyName] = "subproject"
            project[BaseProperty.PROJECT_LANGUAGE.propertyName] = "pl"
            project[BaseProperty.PROJECT_COUNTRY.propertyName] = "PL"
            String s = project.listPropertiesAsString(BaseProperty.class, false)
            assertEquals('''###########################################################
# Base properties
###########################################################
project.icon.file=Icon.png
project.url.base=http://example.com
project.directory.name=subproject
project.language=pl
project.country=PL
''',s)
        }
    }

    @Test
    void testBasePropertyWithComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[BaseProperty.PROJECT_ICON_FILE.propertyName] = "Icon.png"
            project[BaseProperty.PROJECT_URL.propertyName] = "http://example.com"
            project[BaseProperty.PROJECT_DIRECTORY.propertyName] = "subproject"
            project[BaseProperty.PROJECT_LANGUAGE.propertyName] = "pl"
            project[BaseProperty.PROJECT_COUNTRY.propertyName] = "PL"
            String s = project.listPropertiesAsString(BaseProperty.class, true)
            println s
            assertEquals('''###########################################################
# Base properties
###########################################################
# Path to project's icon file
project.icon.file=Icon.png
# Base project URL where the artifacts will be available when released (for example http://example.com/)
project.url.base=http://example.com
# Name of subdirectory (at base url) where the artifacts will be placed (for example "testproject" leads to http://example.com/testproject)
project.directory.name=subproject
# Language of the project [optional] default: <en>
project.language=pl
# Project country [optional] default: <US>
project.country=PL
''',s)
        }
    }

    @Test
    void testReleasePropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project[ProjectReleaseProperty.RELEASE_MAIL_FROM.propertyName] = "test@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_TO.propertyName] = "no-reply@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_FLAGS.propertyName] = "qrCode,imageMontage"
            String s = project.listPropertiesAsString(ProjectReleaseProperty.class, false)
            assertEquals('''###########################################################
# Release properties
###########################################################
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
            project[ProjectReleaseProperty.RELEASE_MAIL_FROM.propertyName] = "test@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_TO.propertyName] = "no-reply@apphance.com"
            project[ProjectReleaseProperty.RELEASE_MAIL_FLAGS.propertyName] = "qrCode,imageMontage"
            String s = project.listPropertiesAsString(ProjectReleaseProperty.class, true)
            assertEquals('''###########################################################
# Release properties
###########################################################
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
