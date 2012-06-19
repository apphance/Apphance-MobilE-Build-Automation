package com.apphance.ameba.plugins.core

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.PluginContainer
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertEquals

class ExtendedPluginContainerTest {

    PluginContainerExtender containerExtender = new PluginContainerExtender()

    Project project

    @Before void removeGetImplementationsOfMethod() {
        // remove if previous tests added it already
        PluginContainer.class.metaClass.getImplementationsOf = null

        containerExtender.extendPluginContainer()

        project = new ProjectBuilder().build()

        project.plugins.apply(AppliedPluginImplementation.class)
    }

    @Test void 'extended PluginContainer should validate single applied plugin'() {
        assertEquals(
                [new AppliedPluginImplementation()],
                project.plugins.getImplementationsOf(AppliedPluginImplementation.class).asList()
        )
    }

    @Test void 'extended PluginContainer should validate applied plugin by interface'() {
        assertEquals(
                [new AppliedPluginImplementation()],
                project.plugins.getImplementationsOf(AppliedPluginInterface.class).asList()
        )
    }

    @Test void 'extended PluginContainer should validate applied plugin by superclass'() {
        assertEquals(
                [new AppliedPluginImplementation()],
                project.plugins.getImplementationsOf(EmptyAppliedPlugin.class).asList()
        )
    }

    @Test void 'extended PluginContainer should invalidate not applied plugin'() {
        assertEquals(
                [],
                project.plugins.getImplementationsOf(UnappliedPlugin.class).asList()
        )
    }

    static interface UnappliedPlugin extends Plugin {}

    static interface AppliedPluginInterface extends Plugin<Project> {}

    static class EmptyAppliedPlugin implements AppliedPluginInterface {void apply(Project project) {}}

    static class AppliedPluginImplementation extends EmptyAppliedPlugin {
        @Override
        int hashCode() {
            return 0
        }

        @Override
        boolean equals(Object obj) {
            return obj.class == this.class
        }
    }
}
