package com.apphance.flow.configuration.ios.variants

enum IOSVariantType {
    SCHEME, TC

    static List<String> names() {
        values()*.name()
    }
}