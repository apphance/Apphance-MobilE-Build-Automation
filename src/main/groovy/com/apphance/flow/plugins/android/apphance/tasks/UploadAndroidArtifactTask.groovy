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

        String user = androidApphanceConfiguration.user.getNotEmptyValue()
        String pass = androidApphanceConfiguration.pass.getNotEmptyValue()
        String key = variant.apphanceAppKey.getNotEmptyValue()

        ApphanceNetworkHelper networkHelper = null

        try {
            networkHelper = new ApphanceNetworkHelper(user, pass)

            logger.lifecycle "Updating arfifact. Version string: $conf.versionString, version code: $conf.versionCode"
            def response = networkHelper.updateArtifactQuery(key, conf.versionString, conf.versionCode, false, ['apk', 'image_montage'])
            logger.lifecycle "Upload version query response: ${response.statusLine}"
            throwIfConditionTrue(!response.entity, "Error while uploading version query, empty response received")

            String content = response.entity.content.text
            logger.info "Full response: $content"
            def resp = new JsonSlurper().parseText(content)

            logger.lifecycle "Updating arfifact  $builderInfo.originalFile.absolutePath"
            response = networkHelper.uploadResource(builderInfo.originalFile, resp.update_urls.apk, 'apk')
            logger.lifecycle "Upload apk response: ${response.statusLine}"
            EntityUtils.consume(response.entity)
        } catch (e) {
            def msg = "Error while uploading artifact to apphance: ${e.message}"
            logger.error(msg)
            throw new GradleException(msg, e)
        } finally {
            networkHelper?.close()
        }
    }
}
