package com.apphance.ameba.plugins.core

import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class CorePluginTest {

    @Before void removeGetImplementationsOfMethod() {
        // remove if previous tests added it already
        PluginContainer.class.metaClass.getImplementationsOf = null
    }

    @Test void 'core plugin should push method to PluginContainer metaClass'() {
        // given
        Project project = ProjectBuilder.builder().build()

        // when
        project.plugins.apply(CorePlugin.class)
        
        // then
        assertTrue(project.plugins.getImplementationsOf(CorePlugin.class).size() > 0)
    }
}
