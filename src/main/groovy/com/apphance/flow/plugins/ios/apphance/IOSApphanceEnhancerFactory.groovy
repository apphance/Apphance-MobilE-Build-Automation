package com.apphance.flow.plugins.ios.apphance

import com.apphance.flow.configuration.ios.variants.IOSVariant

interface IOSApphanceEnhancerFactory {

    IOSApphanceEnhancer create(IOSVariant variant)
}