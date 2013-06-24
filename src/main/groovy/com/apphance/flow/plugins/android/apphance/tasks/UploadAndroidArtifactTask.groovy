package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.AndroidReleaseConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.executor.AntExecutor
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import com.apphance.flow.util.Preconditions
import groovy.json.JsonSlurper
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_APPHANCE_SERVICE

@Mixin(Preconditions)
//TODO to be tested and refactored
class UploadAndroidArtifactTask extends DefaultTask {

    String description = 'Uploads apk & image_montage to Apphance server'
    String group = FLOW_APPHANCE_SERVICE

    @Inject ApphanceConfiguration androidApphanceConfiguration
    @Inject AndroidConfiguration conf
    @Inject AndroidReleaseConfiguration releaseConf
    @Inject AndroidArtifactProvider artifactBuilder
    @Inject AntExecutor executor

    AndroidVariantConfiguration variant

    @TaskAction
    public void uploadArtifact() {
        def builderInfo = artifactBuilder.builderInfo(variant)

        String user = androidApphanceConfiguration.user.value
        String pass = androidApphanceConfiguration.pass.value
        String key = variant.apphanceAppKey.value

        validateUser(user)
        validatePass(pass)

        ApphanceNetworkHelper networkHelper = null

        try {
            networkHelper = new ApphanceNetworkHelper(user, pass)

            def response = networkHelper.updateArtifactQuery(key, conf.versionString, conf.versionCode, false, ['apk', 'image_montage'])
            logger.debug("Upload version query response: ${response.statusLine}")
            throwIfConditionTrue(!response.entity, "Error while uploading version query, empty response received")

            def resp = new JsonSlurper().parseText(response.entity.content.text)

            response = networkHelper.uploadResource(builderInfo.originalFile, resp.update_urls.apk, 'apk')
            logger.debug("Upload apk response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

            response = networkHelper.uploadResource(releaseConf.imageMontageFile.location, resp.update_urls.image_montage, 'image_montage')
            logger.debug("Upload image_montage response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

        } catch (e) {
            def msg = "Error while uploading artifact to apphance: ${e.message}"
            logger.error(msg)
            throw new GradleException(msg)
        } finally {
            networkHelper?.close()
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
