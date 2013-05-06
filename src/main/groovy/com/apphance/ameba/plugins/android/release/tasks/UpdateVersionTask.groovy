package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static org.gradle.api.logging.Logging.getLogger

class UpdateVersionTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'updateVersion'
    String group = AMEBA_RELEASE
    String description = """Updates version stored in manifest file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""

    static Pattern WHITESPACE_PATTERN = Pattern.compile('\\s+')

    @Inject
    AndroidConfiguration conf
    @Inject
    AndroidManifestHelper manifestHelper

    @TaskAction
    public void updateVersion() {
        def versionString = conf.externalVersionString
        def versionCode = conf.externalVersionCode

        validateVersionString(versionString)
        validateVersionCode(versionCode)

        manifestHelper.updateVersion(project.rootDir, versionString, versionCode)

        l.debug("New version code: $versionCode")
        l.debug("Updated version string to: $versionString")
    }

    @groovy.transform.PackageScope
    void validateVersionString(String versionString) {
        versionString = versionString?.trim()
        if (!versionString || versionString?.empty || WHITESPACE_PATTERN.matcher(versionString ?: '').find()) {
            throw new GradleException("""|Property 'version.string' has invalid value!
                                         |Set it either by 'release.string' system property or 'VERSION_STRING' environment variable!
                                         |This property must not contain white space characters!""".stripMargin())
        }
    }

    @groovy.transform.PackageScope
    void validateVersionCode(String versionCode) {
        versionCode = versionCode?.trim()
        if (versionCode?.empty || !versionCode?.matches('[0-9]+')) {
            throw new GradleException("""|Property 'version.code' has invalid value!
                                         |Set it either by 'version.code' system property or 'VERSION_CODE' environment variable!
                                         |This property must have numeric value!""".stripMargin())
        }
    }
}
