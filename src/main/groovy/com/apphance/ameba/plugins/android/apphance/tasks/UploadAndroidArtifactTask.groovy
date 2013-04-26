package com.apphance.ameba.plugins.android.apphance.tasks

import com.apphance.ameba.configuration.ReleaseConfiguration
import com.apphance.ameba.configuration.android.AndroidApphanceConfiguration
import com.apphance.ameba.configuration.android.AndroidConfiguration
import com.apphance.ameba.configuration.android.AndroidVariantConfiguration
import com.apphance.ameba.executor.AntExecutor
import com.apphance.ameba.plugins.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.plugins.apphance.ApphanceNetworkHelper
import com.apphance.ameba.util.Preconditions
import com.google.inject.Inject
import groovy.json.JsonSlurper
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_APPHANCE_SERVICE
import static org.gradle.api.logging.Logging.getLogger

@Mixin(Preconditions)
//TODO to be tested and refactored
class UploadAndroidArtifactTask extends DefaultTask {

    def l = getLogger(getClass())

    String description = 'Uploads apk & image_montage to Apphance server'
    String group = AMEBA_APPHANCE_SERVICE

    @Inject
    private AndroidApphanceConfiguration androidApphanceConfiguration
    @Inject
    private AndroidConfiguration androidConfiguration
    @Inject
    private ReleaseConfiguration releaseConf
    AndroidVariantConfiguration variant

    @Inject
    AntExecutor executor

    @TaskAction
    public void uploadArtifact() {
        def builder = new AndroidSingleVariantApkBuilder(project, androidConfiguration, executor)
        def builderInfo = builder.buildApkArtifactBuilderInfo(variant)

        String user = androidApphanceConfiguration.user.value
        String pass = androidApphanceConfiguration.pass.value
        String key = variant.apphanceAppKey.value

        validateUser(user)
        validatePass(pass)

        ApphanceNetworkHelper networkHelper = null

        try {
            networkHelper = new ApphanceNetworkHelper(user, pass)

            def response = networkHelper.updateArtifactQuery(key, androidConfiguration.versionString, androidConfiguration.versionCode, false,
                    ['apk', 'image_montage'])
            l.debug("Upload version query response: ${response.statusLine}")
            throwIfCondition(!response.entity, "Error while uploading version query, empty response received")

            def resp = new JsonSlurper().parseText(response.entity.content.text)

            response = networkHelper.uploadResource(builderInfo.originalFile, resp.update_urls.apk, 'apk')
            l.debug("Upload apk response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

            response = networkHelper.uploadResource(releaseConf.imageMontageFile.location, resp.update_urls.image_montage, 'image_montage')
            l.debug("Upload image_montage response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

        } catch (e) {
            def msg = "Error while uploading artifact to apphance: ${e.message}"
            l.error(msg)
            throw new GradleException(msg)
        } finally {
            networkHelper?.closeConnection()
        }
    }

    @groovy.transform.PackageScope
    void validateUser(String user) {
        if (!user || user?.trim()?.empty) {
            throw new GradleException("""|Property 'android.apphance.user' has invalid value!
                                         |This property must be set when invoking task: ${this.name}""")
        }
    }

    @groovy.transform.PackageScope
    void validatePass(String pass) {
        if (!pass || pass?.trim()?.empty) {
            throw new GradleException("""|Property 'android.apphance.pass' has invalid value!
                                         |This property must be set when invoking task: ${this.name}""")
        }
    }
}
