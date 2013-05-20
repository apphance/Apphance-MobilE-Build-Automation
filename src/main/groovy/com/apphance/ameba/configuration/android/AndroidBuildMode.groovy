package com.apphance.ameba.configuration.android

enum AndroidBuildMode {
    RELEASE, DEBUG

    String lowerCase() {
        name().toLowerCase()
    }

    String capitalize() {
        lowerCase().capitalize()
    }
}