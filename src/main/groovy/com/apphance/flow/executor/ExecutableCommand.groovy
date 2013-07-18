package com.apphance.flow.executor

enum ExecutableCommand {

    WIN_EXECUTABLE_ANDROID(['cmd', '/c', 'android.bat']),
    WIN_EXECUTABLE_ANT(['cmd', '/c', 'ant.bat']),
    WIN_EXECUTABLE_ADB(['cmd', '/c', 'adb.exe']),
    WIN_EXECUTABLE_EMULATOR(['cmd', '/c', 'emulator.exe']),

    STD_EXECUTABLE_ANDROID(['android']),
    STD_EXECUTABLE_ANT(['ant']),
    STD_EXECUTABLE_ADB(['adb']),
    STD_EXECUTABLE_EMULATOR(['emulator']),

    private List<String> cmd

    ExecutableCommand(List<String> cmd) {
        this.cmd = cmd
    }

    List<String> getCmd() {
        return cmd
    }
}