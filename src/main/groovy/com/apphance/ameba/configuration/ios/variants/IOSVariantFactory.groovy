package com.apphance.ameba.configuration.ios.variants

interface IOSVariantFactory {

    IOSSchemeVariant createSchemeVariant(String name)

    IOSTCVariant createTCVariant(String name)
}