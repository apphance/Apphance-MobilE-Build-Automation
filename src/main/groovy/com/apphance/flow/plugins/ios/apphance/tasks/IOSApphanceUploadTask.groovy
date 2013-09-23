package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.AbstractIOSVariant
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import com.apphance.flow.util.Preconditions
import com.google.inject.Inject
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

    String description = 'Uploads ipa, dsym to Apphance server'
    String group = FLOW_APPHANCE_SERVICE

    @Inject ApphanceConfiguration apphanceConf
    @Inject IOSReleaseConfiguration releaseConf
    @Inject PropertyReader reader

    AbstractIOSVariant variant
    @Lazy(soft = true)
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
            def response = updateArtifactQuery()

            uploadIpa(response.update_urls.ipa)
            updateAhsym(response.update_urls.dsym)

        } catch (e) {
            def msg = "Error while uploading iOS artifacts to apphance. $e.message"
            logger.error(msg)
            throw new GradleException(msg)
        } finally {
            networkHelper?.close()
        }
    }

    private Map updateArtifactQuery() {
        def response = networkHelper.updateArtifactQuery(apphanceKey, variant.versionString, variant.versionCode, false, ['ipa', 'dsym'])
        logger.info("Upload version query response: $response.statusLine")

        validate(response.entity != null, { throw new GradleException('Error while uploading version query, empty response received') })

        new JsonSlurper().parseText(response.entity.content.text) as Map
    }

    private void uploadIpa(String url) {
        def response = networkHelper.uploadResource(releaseConf.ipaFiles[variant.name].location, url, 'ipa')
        validate(response.statusLine.statusCode == 200,
                {
                    throw new GradleException(format(bundle.getString('exception.apphance.ios.upload.ipa'),
                            releaseConf.ipaFiles[variant.name].location.absolutePath, response.entity.content as String))
                })
        logger.info("Upload ipa response: $response.statusLine")
        EntityUtils.consume(response.entity)
    }

    private void updateAhsym(String url) {
        def ahSymDir = releaseConf.ahSYMDirs[variant.name].location
        def ahSymFiles = ahSymDir.listFiles(
                [accept: { d, n -> def f = new File(d, n); n.endsWith('ahsym') && f.isFile() && f.exists() }] as FilenameFilter)

        ahSymFiles.each { ahSYM ->
            def response = networkHelper.uploadResource(ahSYM, url, 'dsym')
            logger.info("Upload ahsym ($ahSYM) response: $response.statusLine")
            validate(response.statusLine.statusCode == 200,
                    {
                        throw new GradleException(format(bundle.getString('exception.apphance.ios.upload.ahsym'),
                                ahSYM.absolutePath, response.entity.content as String))
                    })
            EntityUtils.consume(response.entity)
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
