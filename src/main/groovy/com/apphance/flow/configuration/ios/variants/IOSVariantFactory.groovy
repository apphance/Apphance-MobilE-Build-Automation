package com.apphance.flow.configuration.ios.variants

interface IOSVariantFactory {

    IOSSchemeVariant createSchemeVariant(String name)

    IOSTCVariant createTCVariant(String name)
}