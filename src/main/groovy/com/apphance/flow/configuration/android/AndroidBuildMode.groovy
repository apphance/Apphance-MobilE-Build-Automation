package com.apphance.flow.configuration.android

enum AndroidBuildMode {
    RELEASE, DEBUG

    String lowerCase() {
        name().toLowerCase()
    }

    String capitalize() {
        lowerCase().capitalize()
    }
}