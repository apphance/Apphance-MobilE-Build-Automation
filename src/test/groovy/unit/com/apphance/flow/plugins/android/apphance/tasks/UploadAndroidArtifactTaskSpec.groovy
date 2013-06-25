package com.apphance.flow.plugins.android.apphance.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.plugins.android.builder.AndroidArtifactProvider
import com.apphance.flow.plugins.android.builder.AndroidBuilderInfo
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import spock.lang.Specification

@Mixin(TestUtils)
class UploadAndroidArtifactTaskSpec extends Specification {

    def 'upload method invoked with appropriate arguments'() {
        given:
        def task = create UploadAndroidArtifactTask
        def origFile = tempFile

        def builderInfo = GroovyStub(AndroidBuilderInfo)
        builderInfo.originalFile >> origFile
        task.artifactBuilder = GroovyMock(AndroidArtifactProvider)
        task.artifactBuilder.builderInfo(_) >> builderInfo

        task.androidApphanceConfiguration = new ApphanceConfiguration()
        task.androidApphanceConfiguration.user.value = 'user'
        task.androidApphanceConfiguration.pass.value = 'pass'
        task.conf = GroovyStub(AndroidConfiguration)
        task.conf.getVersionString() >> '1.1.1'
        task.conf.getVersionCode() >> '20'

        task.variant = new AndroidVariantConfiguration('variant')
        task.variant.apphanceAppKey.value = '123'
        task.action = Mock(UploadAndroidArtifactTaskAction)

        when:
        task.uploadArtifact()

        then:
        1 * task.action.upload(_ as ApphanceNetworkHelper, origFile, '123', '1.1.1', '20')
    }

    def 'upload method calls helper'() {
        given:
        ApphanceNetworkHelper networkHelper = Spy(ApphanceNetworkHelper, constructorArgs: ['user', 'pass'])

        def taskAction = new UploadAndroidArtifactTaskAction()
        def file = tempFile

        when:
        taskAction.upload(networkHelper, file, '123', '1.2.3', '13')

        then:
        1 * networkHelper.updateArtifactJson('123', '1.2.3', '13', false, ['apk']) >> '{"status": "OK", "update_urls": {"apk": "https://apphance-app.appspot' +
                '.com/_ah/upload/XYZ/", "image_montage": "https://apphance-app.appspot.com/_ah/upload/ABC/"}}' >> null
        1 * networkHelper.uploadResourceJson(file, "https://apphance-app.appspot.com/_ah/upload/XYZ/", 'apk') >> null
    }
}
