package com.apphance.flow.configuration.android.variants

import com.apphance.flow.configuration.ProjectConfiguration
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

@Mixin(FlowUtils)
class AndroidVariantConfigurationSpec extends Specification {

    def 'test isLibrary'() {
        given:
        def androidVariantConf = new AndroidVariantConfiguration('variantName')
        def flowTmp = temporaryDir
        def projectProps = new File(flowTmp, 'VariantName/project.properties')
        projectProps.parentFile.mkdirs()
        projectProps << input
        println projectProps.absolutePath
        println projectProps.text

        androidVariantConf.conf = GroovyStub(ProjectConfiguration) {
            getTmpDir() >> flowTmp
        }

        expect:
        androidVariantConf.isLibrary() == isLib

        where:
        input                   | isLib
        ''                      | false
        'android.library=false' | false
        'android.library=true'  | true
    }
}
