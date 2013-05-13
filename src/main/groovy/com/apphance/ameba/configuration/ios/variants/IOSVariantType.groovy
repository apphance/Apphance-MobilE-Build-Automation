package com.apphance.ameba.configuration.ios.variants

enum IOSVariantType {
    SCHEME, TC

    static List<String> names() {
        values()*.name()
    }
}