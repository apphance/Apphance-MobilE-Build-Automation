package com.apphance.flow.plugins.ios.buildplugin.tasks

import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class FrameworkVariantTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('frameworkTask', type: FrameworkVariantTask) as FrameworkVariantTask
}
