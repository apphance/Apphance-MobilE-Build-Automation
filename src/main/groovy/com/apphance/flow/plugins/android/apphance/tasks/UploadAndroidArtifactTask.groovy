package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import com.apphance.flow.util.Preconditions
import com.google.inject.Inject
import groovy.json.JsonSlurper
import groovy.transform.PackageScope
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_APPHANCE_SERVICE
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotEmpty

@Mixin(Preconditions)
class UploadAndroidArtifactTask extends DefaultTask {

    String description = 'Uploads apk to Apphance server'
    String group = FLOW_APPHANCE_SERVICE

    @Inject AndroidConfiguration conf
    @Inject ApphanceConfiguration apphanceConf
    @Inject AndroidArtifactProvider artifactBuilder
    @Inject PropertyReader reader

    AndroidVariantConfiguration variant

    def action = new UploadAndroidArtifactTaskAction()
    def bundle = getBundle('validation')

    @TaskAction
    public void uploadArtifact() {
        def builderInfo = artifactBuilder.builderInfo(variant)

        String user = apphanceConf.user.value ?: reader.systemProperty('apphance.user') ?: reader.envVariable('APPHANCE_USER')
        String pass = apphanceConf.pass.value ?: reader.systemProperty('apphance.pass') ?: reader.envVariable('APPHANCE_PASS')
        String key = variant.apphanceAppKey.value

        validate(isNotEmpty(user), { throw new GradleException(bundle.getString('exception.apphance.empty.user')) })
        validate(isNotEmpty(pass), { throw new GradleException(bundle.getString('exception.apphance.empty.pass')) })
        validate(isNotEmpty(key), { throw new GradleException(format(bundle.getString('exception.apphance.empty.key'), variant.name)) })

        logger.lifecycle "Uploading arfifact: ${builderInfo.originalFile} with version ${conf.versionString} (${conf.versionCode})"
        action.upload(new ApphanceNetworkHelper(user, pass), builderInfo.originalFile, key, conf.versionString, conf.versionCode)
    }
}

@PackageScope
class UploadAndroidArtifactTaskAction {

    @PackageScope
    void upload(ApphanceNetworkHelper apphanceNetworkHelper, File originalFile, String key, String versionString, String versionCode) {
        apphanceNetworkHelper.safeCall { ApphanceNetworkHelper networkHelper ->
            String updateResponseJson = networkHelper.updateArtifactJson(key, versionString, versionCode, false, ['apk'])
            def resp = new JsonSlurper().parseText(updateResponseJson)
            networkHelper.uploadResourceJson(originalFile, resp.update_urls.apk, 'apk')
        }
    }
}
