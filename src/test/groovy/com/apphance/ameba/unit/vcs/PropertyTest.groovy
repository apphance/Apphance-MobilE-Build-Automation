package com.apphance.ameba.unit.vcs;

import static org.junit.Assert.*;

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test



import com.apphance.ameba.PropertyCategory;
import com.apphance.ameba.vcs.plugins.git.GitProperty;
import com.apphance.ameba.vcs.plugins.mercurial.MercurialProperty

class PropertyVCSTest {
    @Test
    void testGitPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['git.branch'] = "master"
            String s = project.listPropertiesAsString(GitProperty.class, false)
            assertEquals('''###########################################################
# Git properties
###########################################################
git.branch=master
''',s)
        }
    }
    @Test
    void testGitPropertyWithComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['git.branch'] = "master"
            String s = project.listPropertiesAsString(GitProperty.class, true)
            assertEquals('''###########################################################
# Git properties
###########################################################
# Branch which is used for git operations [optional] default: <master>
git.branch=master
''',s)
        }
    }

    @Test
    void testMercurialPropertyNoComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['hg.commit.user'] = "Test Apphance <test@apphance.com>"
            String s = project.listPropertiesAsString(MercurialProperty.class, false)
            assertEquals('''###########################################################
# Mercurial properties
###########################################################
hg.commit.user=Test Apphance <test@apphance.com>
''',s)
        }
    }
    @Test
    void testMercurialPropertyWithComments () {
        use (PropertyCategory) {
            ProjectBuilder projectBuilder = ProjectBuilder.builder()
            Project project = projectBuilder.build()
            project['hg.commit.user'] = "Test Apphance <test@apphance.com>"
            String s = project.listPropertiesAsString(MercurialProperty.class, true)
            assertEquals('''###########################################################
# Mercurial properties
###########################################################
# Commit user - usually in form of "Name <e-mail>"
hg.commit.user=Test Apphance <test@apphance.com>
''',s)
        }
    }
}
