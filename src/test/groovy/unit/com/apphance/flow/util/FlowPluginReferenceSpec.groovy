package com.apphance.flow.util

import spock.lang.Specification

@Mixin(FlowUtils)
class FlowPluginReferenceSpec extends Specification {

    def 'test get flow reference'() {
        given:
        def reference = new FlowPluginReference()

        expect:
        reference.configurations
        reference.plugins
        reference.tasks

        reference.configurations*.name().sort() == ['AbstractIOSVariant', 'AbstractVariant', 'AndroidAnalysisConfiguration', 'AndroidConfiguration',
                'AndroidReleaseConfiguration', 'AndroidTestConfiguration', 'AndroidVariantConfiguration', 'AndroidVariantsConfiguration',
                'ApphanceConfiguration', 'IOSConfiguration', 'IOSReleaseConfiguration', 'IOSSchemeVariant', 'IOSTestConfiguration', 'IOSVariantsConfiguration',
                'IOSWorkspaceVariant', 'ProjectConfiguration', 'ReleaseConfiguration']

        reference.plugins*.name().sort() == ['AndroidAnalysisPlugin', 'AndroidApphancePlugin', 'AndroidPlugin', 'AndroidReleasePlugin', 'AndroidTestPlugin',
                'FlowPlugin', 'IOSApphancePlugin', 'IOSPlugin', 'IOSReleasePlugin', 'IOSTestPlugin', 'ProjectPlugin', 'ReleasePlugin']

        reference.tasks*.name().sort() == ['AbstractAvailableArtifactsInfoTask', 'AbstractBuildVariantTask', 'AbstractUpdateVersionTask',
                'AvailableArtifactsInfoTask', 'AvailableArtifactsInfoTask', 'BuildSourcesZipTask', 'CPDTask', 'CleanFlowTask', 'CopyMobileProvisionTask',
                'CopySourcesTask', 'CopySourcesTask', 'DeviceVariantTask', 'FrameworkVariantTask', 'IOSApphanceUploadTask', 'IOSTestTask', 'ImageMontageTask',
                'LintTask', 'PrepareSetupTask', 'RunRobolectricTestsTask', 'SendMailMessageTask', 'SimulatorVariantTask', 'SingleVariantTask',
                'UnlockKeyChainTask', 'UpdateProjectTask', 'UpdateVersionTask', 'UpdateVersionTask', 'UploadAndroidArtifactTask', 'VerifySetupTask']
    }
}
