package com.apphance.ameba.configuration.ios

enum IOSBuildMode {

    SIMULATOR, DEVICE

    String lowerCase() {
        name().toLowerCase()
    }

    String capitalize() {
        lowerCase().capitalize()
    }
}
