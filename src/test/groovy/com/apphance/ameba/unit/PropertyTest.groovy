package com.apphance.ameba.unit;

import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test



import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.plugins.projectconfiguration.BaseProperty;
import com.apphance.ameba.plugins.release.ProjectReleaseProperty

class PropertyTest {
    @Test
    void testBasePropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['project.icon.file'] = "Icon.png"
            project['project.url.base'] = "http://example.com"
            project['project.directory.name'] = "subproject"
            project['project.language'] = "pl"
            project['project.country'] = "PL"
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
            project['project.icon.file'] = "Icon.png"
            project['project.url.base'] = "http://example.com"
            project['project.directory.name'] = "subproject"
            project['project.language'] = "pl"
            project['project.country'] = "PL"
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
            project['release.mail.from'] = "test@apphance.com"
            project['release.mail.to'] = "no-reply@apphance.com"
            project['release.mail.flags'] = "qrCode,imageMontage"
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
            project['release.mail.from'] = "test@apphance.com"
            project['release.mail.to'] = "no-reply@apphance.com"
            project['release.mail.flags'] = "qrCode,imageMontage"
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
