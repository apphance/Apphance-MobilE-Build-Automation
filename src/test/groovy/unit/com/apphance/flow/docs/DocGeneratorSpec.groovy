package com.apphance.flow.docs

import spock.lang.Specification

class DocGeneratorSpec extends Specification {

    def 'test get flow reference'() {
        given:
        def reference = new DocGenerator()

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
                'CopySourcesTask', 'DeviceVariantTask', 'FrameworkVariantTask', 'IOSApphanceUploadTask', 'IOSTestTask', 'ImageMontageTask',
                'LintTask', 'PrepareSetupTask', 'RunRobolectricTestsTask', 'SendMailMessageTask', 'SimulatorVariantTask', 'SingleVariantTask',
                'UnlockKeyChainTask', 'UpdateProjectTask', 'UpdateVersionTask', 'UpdateVersionTask', 'UploadAndroidArtifactTask']
    }
}
