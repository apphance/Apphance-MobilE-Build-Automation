package com.apphance.ameba.configuration.android

enum AndroidArchiveType {

    JAR('jar'),
    APK('apk')

    private String extension

    AndroidArchiveType(String extension) {
        this.extension = extension
    }

    String getExtension() {
        return extension
    }
}