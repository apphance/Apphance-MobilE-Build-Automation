package com.apphance.ameba.plugins.ios.apphance.tasks

import com.apphance.ameba.configuration.apphance.ApphanceConfiguration
import com.apphance.ameba.configuration.ios.IOSReleaseConfiguration
import com.apphance.ameba.configuration.ios.variants.AbstractIOSVariant
import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.apphance.ApphanceNetworkHelper
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
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
class UploadIOSArtifactTask extends DefaultTask {

    private l = getLogger(getClass())

    String description = 'Uploads ipa, dsym & image_montage to Apphance server'
    String group = AMEBA_APPHANCE_SERVICE

    @Inject IOSExecutor iosExecutor
    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSReleaseConfiguration iOSReleaseConf

    //TODO inject network helper

    AbstractIOSVariant variant

    @TaskAction
    void uploadIOSArtifact() {

        def builder = new IOSSingleVariantBuilder(iosExecutor: iosExecutor)
        builder.buildSingleBuilderInfo(variant.target, variant.configuration, 'iphoneos', project)

        String user = apphanceConf.user.value
        String pass = apphanceConf.pass.value
        String key = variant.apphanceAppKey.value

        def networkHelper = null

        try {
            networkHelper = new ApphanceNetworkHelper(user, pass)

            def response = networkHelper.updateArtifactQuery(key, variant.versionString, variant.versionCode, false, ['ipa', 'dsym', 'image_montage'])
            l.lifecycle("Upload version query response: ${response.statusLine}")

            validate(response.entity != null, { throw new GradleException("Error while uploading version query, empty response received") })

            def responseJSON = new JsonSlurper().parseText(response.entity.content.text)

            response = networkHelper.uploadResource(iOSReleaseConf.ipaFiles[e.id].location, responseJSON.update_urls.ipa, 'ipa')
            l.lifecycle("Upload ipa response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

            response = networkHelper.uploadResource(iOSReleaseConf.imageMontageFile.location, responseJSON.update_urls.image_montage, 'image_montage')
            l.lifecycle("Upload image_montage response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

            //TODO turn on after DI is implemented
//                iOSReleaseConf.ahSYMDirs[e.id].location.list([accept: { d, n -> def f = new File(d, n); n.endsWith("ahsym") && f.isFile() && f.exists() }] as FilenameFilter).each { ahSYM ->
//                    response = networkHelper.uploadResource(new File(iOSReleaseConf.ahSYMDirs[e.id].location, ahSYM), responseJSON.update_urls.dsym, 'dsym')
//                    l.lifecycle("Upload ahsym ($ahSYM) response: ${response.statusLine}")
//                    EntityUtils.consume(response.entity)
//                }

        } catch (err) {
            def msg = "Error while uploading artifact to apphance: ${err.message}"
            l.error(msg)
            throw new GradleException(msg)
        } finally {
            networkHelper?.closeConnection()
        }
    }
}
