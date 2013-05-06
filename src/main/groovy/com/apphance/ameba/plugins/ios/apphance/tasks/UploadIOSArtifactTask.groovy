package com.apphance.ameba.plugins.ios.apphance.tasks

import com.apphance.ameba.executor.IOSExecutor
import com.apphance.ameba.plugins.apphance.ApphanceNetworkHelper
import com.apphance.ameba.plugins.apphance.ApphanceProperty
import com.apphance.ameba.plugins.ios.buildplugin.IOSSingleVariantBuilder
import com.apphance.ameba.plugins.ios.release.IOSReleaseConfigurationOLD
import com.apphance.ameba.plugins.projectconfiguration.ProjectConfiguration
import com.apphance.ameba.plugins.release.ProjectReleaseConfiguration
import com.apphance.ameba.util.Preconditions
import groovy.json.JsonSlurper
import org.apache.http.util.EntityUtils
import org.gradle.api.GradleException
import org.gradle.api.Project

import static com.apphance.ameba.PropertyCategory.getProjectConfiguration
import static com.apphance.ameba.plugins.ios.release.IOSReleaseConfigurationRetriever.getIosReleaseConfiguration
import static com.apphance.ameba.plugins.release.ProjectReleaseCategory.getProjectReleaseConfiguration
import static org.gradle.api.logging.Logging.getLogger

@Mixin(Preconditions)
class UploadIOSArtifactTask {

    private l = getLogger(getClass())

    private Project project
    private IOSExecutor iosExecutor
    private ProjectConfiguration conf
    private ProjectReleaseConfiguration releaseConf
    private IOSReleaseConfigurationOLD iOSReleaseConf
    private String target
    private String configuration

    UploadIOSArtifactTask(Project project, IOSExecutor iosExecutor, Expando details) {
        this.project = project
        this.iosExecutor = iosExecutor
        this.target = details.target
        this.configuration = details.configuration
        this.conf = getProjectConfiguration(project)
        this.releaseConf = getProjectReleaseConfiguration(project)
        this.iOSReleaseConf = getIosReleaseConfiguration(project)
    }

    void uploadIOSArtifact() {

        def builder = new IOSSingleVariantBuilder(project, iosExecutor)
        builder.buildSingleBuilderInfo(target, configuration, 'iphoneos', project)

        //TODO gradle.properties
        String user = project['apphanceUserName']
        String pass = project['apphancePassword']
        //TODO gradle.properties
        String key = project[ApphanceProperty.APPLICATION_KEY.propertyName]

        def networkHelper = null

        try {
            networkHelper = new ApphanceNetworkHelper(user, pass)

            def response = networkHelper.updateArtifactQuery(key, conf.versionString, conf.versionCode, false, ['ipa', 'dsym', 'image_montage'])
            l.lifecycle("Upload version query response: ${response.statusLine}")

            validate(response.entity != null, { throw new GradleException("Error while uploading version query, empty response received") })

            def responseJSON = new JsonSlurper().parseText(response.entity.content.text)

            response = networkHelper.uploadResource(iOSReleaseConf.ipaFiles[e.id].location, responseJSON.update_urls.ipa, 'ipa')
            l.lifecycle("Upload ipa response: ${response.statusLine}")
            EntityUtils.consume(response.entity)

            response = networkHelper.uploadResource(releaseConf.imageMontageFile.location, responseJSON.update_urls.image_montage, 'image_montage')
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
