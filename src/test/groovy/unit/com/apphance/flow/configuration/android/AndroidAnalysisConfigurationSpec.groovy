package com.apphance.flow.configuration.android

import com.apphance.flow.TestUtils
import com.apphance.flow.detection.project.ProjectTypeDetector
import com.apphance.flow.util.FlowUtils
import com.apphance.flow.validation.PropertyValidator
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS
import static org.apache.commons.lang.StringUtils.isNotEmpty

@Mixin([TestUtils, FlowUtils])
class AndroidAnalysisConfigurationSpec extends Specification {

    def static noFile = new File('./nonExistingFile.ext')
    def static message = { List<String> props -> props.collect { "Incorrect value of 'android.analysis.$it' property" } }

    def 'configuration is enabled based on project type and internal field'() {
        given:
        def ptd = GroovyStub(ProjectTypeDetector)

        when:
        ptd.detectProjectType(_) >> type
        def ac = new AndroidConfiguration()
        ac.projectTypeDetector = ptd
        ac.project = GroovyStub(Project) {
            getRootDir() >> GroovyStub(File)
        }
        def aac = new AndroidAnalysisConfiguration()
        aac.enabled = internalField
        aac.conf = ac

        then:
        aac.isEnabled() == enabled

        where:
        enabled | type    | internalField
        false   | IOS     | true
        false   | IOS     | false
        true    | ANDROID | true
        false   | ANDROID | false
    }

    def 'configuration is verified properly'() {
        given:
        def aac = new AndroidAnalysisConfiguration()
        aac.propValidator = new PropertyValidator()

        when:
        aac.findbugsExclude.value = findbugs
        aac.pmdRules.value = pmd
        aac.checkstyleConfigFile.value = checkstyle
        def errors = []
        aac.validate(errors)

        then:
        errors.findAll { isNotEmpty(it) }.sort() == expectedErrors.sort()

        where:
        checkstyle | pmd    | findbugs | expectedErrors
        null       | null   | null     | []
        tempFile   | null   | tempFile | []
        noFile     | null   | tempFile | message(['checkstyle.config'])
        noFile     | noFile | noFile   | message(['checkstyle.config', 'pmd.rules', 'findbugs.exclude'])

    }
}
