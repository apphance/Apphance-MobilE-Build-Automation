package com.apphance.ameba.configuration.android

public enum AndroidBuildMode {
    RELEASE, DEBUG

    String lowerCase() {
        name().toLowerCase()
    }

    String capitalize() {
        name().toLowerCase().capitalize()
    }
}