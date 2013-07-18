package com.apphance.flow.executor

import spock.lang.Specification

import static com.apphance.flow.executor.ExecutableCommand.*

class ExecutableCommandSpec extends Specification {

    def 'executable command constructed correctly'() {
        expect:
        WIN_EXECUTABLE_ANDROID.cmd == ['cmd', '/c', 'android.bat']
        WIN_EXECUTABLE_ANT.cmd == ['cmd', '/c', 'ant.bat']
        WIN_EXECUTABLE_ADB.cmd == ['cmd', '/c', 'adb.exe']
        WIN_EXECUTABLE_EMULATOR.cmd == ['cmd', '/c', 'emulator.exe']

        and:
        STD_EXECUTABLE_ANDROID.cmd == ['android']
        STD_EXECUTABLE_ANT.cmd == ['ant']
        STD_EXECUTABLE_ADB.cmd == ['adb']
        STD_EXECUTABLE_EMULATOR.cmd == ['emulator']
    }
}
