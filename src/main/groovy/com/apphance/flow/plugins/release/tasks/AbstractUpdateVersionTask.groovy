package com.apphance.flow.plugins.release.tasks

import com.apphance.flow.configuration.ProjectConfiguration
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject
import java.util.regex.Pattern

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_RELEASE
import static org.apache.commons.lang.StringUtils.isEmpty

abstract class AbstractUpdateVersionTask extends DefaultTask {

    static final String NAME = 'updateVersion'
    String group = FLOW_RELEASE
    String description = """Updates version stored in manifest file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""

    public final static Pattern WHITESPACE_PATTERN = Pattern.compile('\\s+')

    @Inject ProjectConfiguration conf

    @TaskAction
    void updateVersion() {
        def versionString = conf.extVersionString
        def versionCode = conf.extVersionCode

        validateVersionString(versionString)
        validateVersionCode(versionCode)

        updateDescriptor(versionCode, versionString)

        logger.lifecycle("New version string: $versionString")
        logger.lifecycle("New version code: $versionCode")
    }

    @PackageScope
    abstract void updateDescriptor(String versionCode, String versionString)

    @PackageScope
    void validateVersionString(String versionString) {
        if (isEmpty(versionString) || WHITESPACE_PATTERN.matcher(versionString).find()) {
            throw new GradleException("""|Property 'version.string' has invalid value!
                                         |Set it either by 'version.string' system property or 'VERSION_STRING' environment variable!
                                         |This property must not contain white space characters!""".stripMargin())
        }
    }

    @PackageScope
    void validateVersionCode(String versionCode) {
        if (isEmpty(versionCode) || !versionCode.matches('[0-9]+')) {
            throw new GradleException("""|Property 'version.code' has invalid value!
                                         |Set it either by 'version.code' system property or 'VERSION_CODE' environment variable!
                                         |This property must have numeric value!""".stripMargin())
        }
    }
}
