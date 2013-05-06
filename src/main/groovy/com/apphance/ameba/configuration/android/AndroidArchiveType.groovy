package com.apphance.ameba.configuration.android

enum AndroidArchiveType {
    JAR, APK

    String lowerCase() {
        name().toLowerCase()
    }
}
