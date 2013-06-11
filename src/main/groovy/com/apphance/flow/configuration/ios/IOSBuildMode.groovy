package com.apphance.flow.configuration.ios

enum IOSBuildMode {

    SIMULATOR, DEVICE

    String lowerCase() {
        name().toLowerCase()
    }

    String capitalize() {
        lowerCase().capitalize()
    }
}
