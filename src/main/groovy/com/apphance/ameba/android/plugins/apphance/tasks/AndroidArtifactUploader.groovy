package com.apphance.ameba.android.plugins.apphance.tasks

import com.apphance.ameba.ProjectConfiguration
import com.apphance.ameba.PropertyCategory
import com.apphance.ameba.android.AndroidProjectConfiguration
import com.apphance.ameba.android.AndroidProjectConfigurationRetriever
import com.apphance.ameba.android.AndroidSingleVariantApkBuilder
import com.apphance.ameba.android.plugins.test.ApphanceNetworkHelper
import com.apphance.ameba.apphance.ApphanceProperty
import com.apphance.ameba.executor.command.CommandExecutor
import com.apphance.ameba.plugins.release.ProjectReleaseCategory
import com.apphance.ameba.util.Preconditions
import groovy.json.JsonSlurper
import org.apache.http.util.EntityUtils
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.logging.Logging

@Mixin(Preconditions)
//TODO to be tested and refactored
class AndroidArtifactUploader {


    def l = Logging.getLogger(getClass())

    private Project project
    private CommandExecutor executor
    private ProjectConfiguration conf
    private AndroidProjectConfiguration androidConf

    AndroidArtifactUploader(Project project, CommandExecutor executor) {
        this.project = project
        this.executor = executor
        this.conf = PropertyCategory.getProjectConfiguration(project)
        this.androidConf = AndroidProjectConfigurationRetriever.getAndroidProjectConfiguration(project)
    }

    void uploadArtifact(String variant) {
        def builder = new AndroidSingleVariantApkBuilder(project, androidConf, executor)
        def builderInfo = builder.buildApkArtifactBuilderInfo(variant, 'Debug')
        def releaseConf = ProjectReleaseCategory.getProjectReleaseConfiguration(project)

        //TODO gradle.properties + validation
        String user = project['apphanceUserName']
        String pass = project['apphancePassword']
        //TODO gradle.properties + validation

        String key = project[ApphanceProperty.APPLICATION_KEY.propertyName]
        ApphanceNetworkHelper networkHelper = null

        try {
            networkHelper = new ApphanceNetworkHelper(user, pass)

            def response = networkHelper.updateArtifactQuery(key, conf.versionString, conf.versionCode, false, ['apk', 'image_montage'])
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
}
