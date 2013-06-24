package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import com.apphance.flow.util.Preconditions
import com.google.inject.Inject
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.transform.PackageScope
import org.apache.http.util.EntityUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_APPHANCE_SERVICE
import static java.text.MessageFormat.format
import static java.util.ResourceBundle.getBundle
import static org.apache.commons.lang.StringUtils.isNotEmpty
import static org.gradle.api.logging.Logging.getLogger

@Mixin(Preconditions)
class IOSApphanceUploadTask extends DefaultTask {

    private logger = getLogger(getClass())

    String description = 'Uploads ipa, dsym & image_montage to Apphance server'
    String group = FLOW_APPHANCE_SERVICE

    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject PropertyReader reader

    AbstractIOSVariant variant
    @Lazy
    @PackageScope
    ApphanceNetworkHelper networkHelper = {
        new ApphanceNetworkHelper(apphanceUser, apphancePass)
    }()

    def bundle = getBundle('validation')

    @TaskAction
    void upload() {

        validate(isNotEmpty(apphanceUser), { throw new GradleException(bundle.getString('exception.apphance.empty.user')) })
        validate(isNotEmpty(apphancePass), { throw new GradleException(bundle.getString('exception.apphance.empty.pass')) })
        validate(isNotEmpty(apphanceKey), { throw new GradleException(format(bundle.getString('exception.apphance.empty.key'), variant.name)) })

        try {

            def response = networkHelper.updateArtifactQuery(apphanceKey, variant.versionString, variant.versionCode, false, ['ipa', 'dsym', 'image_montage'])
            logger.info("Upload version query response: $response.statusLine")

            validate(response.entity != null, { throw new GradleException('Error while uploading version query, empty response received') })

            def responseJSON = new JsonSlurper().parseText(response.entity.content.text) as Map

            response = networkHelper.uploadResource(releaseConf.ipaFiles[variant.name].location, responseJSON.update_urls.ipa, 'ipa')
            logger.info("Upload ipa response: $response.statusLine")
            EntityUtils.consume(response.entity)

            response = networkHelper.uploadResource(releaseConf.imageMontageFile.location, responseJSON.update_urls.image_montage, 'image_montage')
            logger.info("Upload image_montage response: $response.statusLine")
            EntityUtils.consume(response.entity)

            def ahSymDir = releaseConf.ahSYMDirs[variant.name].location
            def ahSymFiles = ahSymDir.listFiles([accept: { d, n -> def f = new File(d, n); n.endsWith('ahsym') && f.isFile() && f.exists() }] as FilenameFilter)
            ahSymFiles.each { ahSYM ->
                response = networkHelper.uploadResource(ahSYM, responseJSON.update_urls.dsym, 'dsym')
                logger.info("Upload ahsym ($ahSYM) response: $response.statusLine")
                EntityUtils.consume(response.entity)
            }

        } catch (e) {
            def msg = "Error while uploading iOS artifacts to apphance: $e.message"
            logger.error(msg)
            throw new GradleException(msg)
        } finally {
            networkHelper?.close()
        }
    }

    @Lazy
    @PackageScope
    String apphanceUser = {
        apphanceConf.user.value ?: reader.systemProperty('apphance.user') ?: reader.envVariable('APPHANCE_USER')
    }()

    @Lazy
    @PackageScope
    String apphancePass = {
        apphanceConf.pass.value ?: reader.systemProperty('apphance.pass') ?: reader.envVariable('APPHANCE_PASS')
    }()

    @Lazy
    @PackageScope
    String apphanceKey = {
        variant.apphanceAppKey.value
    }()
}
