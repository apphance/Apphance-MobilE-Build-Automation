package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import groovy.json.JsonSlurper
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_APPHANCE_SERVICE

class UploadAndroidArtifactTask extends DefaultTask {

    String description = 'Uploads apk to Apphance server'
    String group = FLOW_APPHANCE_SERVICE

    @Inject AndroidConfiguration conf
    @Inject ApphanceConfiguration apphanceConf
    @Inject AndroidArtifactProvider artifactBuilder

    AndroidVariantConfiguration variant

    def action = new UploadAndroidArtifactTaskAction()

    @TaskAction
    public void uploadArtifact() {
        def builderInfo = artifactBuilder.builderInfo(variant)

        String user = apphanceConf.user.getNotEmptyValue()
        String pass = apphanceConf.pass.getNotEmptyValue()
        String key = variant.apphanceAppKey.getNotEmptyValue()

        logger.lifecycle "Uploading arfifact: ${builderInfo.originalFile} with version ${conf.versionString} (${conf.versionCode})"
        action.upload(new ApphanceNetworkHelper(user, pass), builderInfo.originalFile, key, conf.versionString, conf.versionCode)
    }
}

@PackageScope
class UploadAndroidArtifactTaskAction {

    @PackageScope
    void upload(ApphanceNetworkHelper apphanceNetworkHelper, File originalFile, String key, String versionString, String versionCode) {
        apphanceNetworkHelper.call { ApphanceNetworkHelper networkHelper ->
            String updateResponseJson = networkHelper.updateArtifactJson(key, versionString, versionCode, false, ['apk'])
            def resp = new JsonSlurper().parseText(updateResponseJson)
            networkHelper.uploadResourceJson(originalFile, resp.update_urls.apk, 'apk')
        }
    }
}
