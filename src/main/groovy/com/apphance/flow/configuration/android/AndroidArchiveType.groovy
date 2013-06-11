package com.apphance.flow.configuration.android

enum AndroidArchiveType {
    JAR, APK

    String lowerCase() {
        name().toLowerCase()
    }
}
