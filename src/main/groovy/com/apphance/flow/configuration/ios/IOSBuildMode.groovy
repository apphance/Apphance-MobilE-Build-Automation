package com.apphance.flow.configuration.ios

enum IOSBuildMode {

    SIMULATOR, DEVICE, FRAMEWORK

    String lowerCase() {
        name().toLowerCase()
    }

    String capitalize() {
        lowerCase().capitalize()
    }
}
