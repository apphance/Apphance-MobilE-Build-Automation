package com.apphance.flow.executor

import com.apphance.flow.configuration.android.AndroidConfiguration
import com.apphance.flow.executor.command.CommandExecutor
import spock.lang.Shared
import spock.lang.Specification

import static com.apphance.flow.executor.ExecutableCommand.STD_EXECUTABLE_ANDROID

class AndroidExecutorSpec extends Specification {

    def conf = GroovyMock(AndroidConfiguration)

    def 'test updateProject method'() {
        given:
        def commandExecutor = GroovyMock(CommandExecutor) {
            executeCommand(_) >> targets.split('\n').iterator()
        }
        def androidExecutor = new AndroidExecutor(executor: commandExecutor, conf: conf, executableAndroid: STD_EXECUTABLE_ANDROID)

        when: androidExecutor.updateProject(GroovyMock(File), 'android-8', 'sample-name')
        then: 1 * commandExecutor.executeCommand({ it.commandForExecution.join(' ') == "android update project -p . -s -t android-8 -n sample-name" })
    }

    def 'test list targets'() {
        given:
        def ce = Mock(CommandExecutor)
        ce.executeCommand({ it.commandForExecution.join(' ') == 'android list target' }) >> targets.split('\n').iterator()

        and:
        def ae = new AndroidExecutor(executor: ce, conf: conf, executableAndroid: STD_EXECUTABLE_ANDROID)

        when:
        def output = ae.targets

        then:
        output == ['Google Inc.:Google APIs:3', 'Google Inc.:Google APIs:4', 'android-17', 'android-3', 'android-4']
    }

    def 'id for target'() {
        given:
        def ce = Mock(CommandExecutor) {
            executeCommand(_) >> targets.split('\n').iterator()
        }

        and:
        def ae = new AndroidExecutor(executor: ce, conf: conf, executableAndroid: STD_EXECUTABLE_ANDROID)

        expect:
        idForTarget == ae.idForTarget.call(target)

        where:
        idForTarget | target
        '1'         | 'android-3'
        '3'         | 'android-4'
        '24'        | 'android-17'
        '4'         | 'Google Inc.:Google APIs:4'
    }

    @Shared
    def targets = "Available Android targets:\n" +
            "----------\n" +
            "id: 1 or \"android-3\"\n" +
            "     Name: Android 1.5\n" +
            "     Type: Platform\n" +
            "     API level: 3\n" +
            "     Revision: 4\n" +
            "     Skins: HVGA (default), HVGA-L, HVGA-P, QVGA-L, QVGA-P\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 2 or \"Google Inc.:Google APIs:3\"\n" +
            "     Name: Google APIs\n" +
            "     Type: Add-On\n" +
            "     Vendor: Google Inc.\n" +
            "     Revision: 3\n" +
            "     Description: Android + Google APIs\n" +
            "     Based on Android 1.5 (API level 3)\n" +
            "     Libraries:\n" +
            "      * com.google.android.maps (maps.jar)\n" +
            "          API for Google Maps\n" +
            "     Skins: QVGA-P, HVGA-L, HVGA (default), QVGA-L, HVGA-P\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 3 or \"android-4\"\n" +
            "     Name: Android 1.6\n" +
            "     Type: Platform\n" +
            "     API level: 4\n" +
            "     Revision: 3\n" +
            "     Skins: HVGA, QVGA, WVGA800 (default), WVGA854\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 4 or \"Google Inc.:Google APIs:4\"\n" +
            "     Name: Google APIs\n" +
            "     Type: Add-On\n" +
            "     Vendor: Google Inc.\n" +
            "     Revision: 2\n" +
            "     Description: Android + Google APIs\n" +
            "     Based on Android 1.6 (API level 4)\n" +
            "     Libraries:\n" +
            "      * com.google.android.maps (maps.jar)\n" +
            "          API for Google Maps\n" +
            "     Skins: WVGA854, HVGA, WVGA800 (default), QVGA\n" +
            "     ABIs : armeabi\n" +
            "----------\n" +
            "id: 24 or \"android-17\"\n" +
            "     Name: Android 4.2\n" +
            "     Type: Platform\n" +
            "     API level: 17\n" +
            "     Revision: 1\n" +
            "     Skins: HVGA, QVGA, WQVGA400, WQVGA432, WSVGA, WVGA800 (default), WVGA854, WXGA720, WXGA800, WXGA800-7in\n" +
            "     ABIs : armeabi-v7a, mips"
}
