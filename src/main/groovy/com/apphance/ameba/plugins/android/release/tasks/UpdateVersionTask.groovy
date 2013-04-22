package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidReleaseConfiguration
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

    private Pattern WHITESPACE = Pattern.compile('\\s+')

    @Inject
    private AndroidManifestHelper manifestHelper

    @Inject
    private AndroidReleaseConfiguration releaseConf

    @TaskAction
    public void updateVersion() {
        def releaseString = releaseConf.releaseString
        def releaseCode = releaseConf.releaseCode

        validateReleaseString(releaseString)
        validateReleaseCode(releaseCode)

        manifestHelper.updateVersion(project.rootDir, releaseString, releaseCode)

        l.debug("New version code: $releaseCode")
        l.debug("Updated version string to: $releaseString")
    }

    @groovy.transform.PackageScope
    void validateReleaseString(String releaseString) {
        releaseString = releaseString?.trim()
        if (!releaseString || releaseString?.empty || WHITESPACE.matcher(releaseString ?: '').find()) {
            throw new GradleException("""|Property 'release.string' has invalid value!
                                         |Set it either by 'release.string' system property or
                                         |'RELEASE_STRING environment variable!
                                         |This property must not contain white space characters!""")
        }
    }

    @groovy.transform.PackageScope
    void validateReleaseCode(String releaseCode) {
        releaseCode = releaseCode?.trim()
        if (releaseCode?.empty || !releaseCode?.matches('[0-9]+')) {
            throw new GradleException("""|Property 'release.code' has invalid value!
                                         |Set it either by 'release.code' system property or
                                         |'RELEASE_CODE environment variable!
                                         |This property must have numeric value!""")
        }
    }
}
