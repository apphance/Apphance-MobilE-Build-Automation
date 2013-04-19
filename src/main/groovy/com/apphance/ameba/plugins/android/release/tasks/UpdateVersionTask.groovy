package com.apphance.ameba.plugins.android.release.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.plugins.android.AndroidManifestHelper
import com.google.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_RELEASE
import static com.google.common.base.Preconditions.checkNotNull
import static org.gradle.api.logging.Logging.getLogger

class UpdateVersionTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'updateVersion'
    String group = AMEBA_RELEASE
    String description = """Updates version stored in manifest file of the project.
           Numeric version is set from 'version.code' property, String version is set from 'version.string' property"""

    @Inject
    private AndroidManifestHelper manifestHelper

    @Inject
    private AndroidConfiguration androidConfiguration

    @TaskAction
    public void updateVersion() {
        def versionString = androidConfiguration.versionString
        def versionCode = androidConfiguration.versionCode

        checkNotNull(versionString)
        checkNotNull(versionCode)

        manifestHelper.updateVersion(project.rootDir, versionString, versionCode)

        l.debug("New version code: $versionCode")
        l.debug("Updated version string to: $versionString")
    }
}
