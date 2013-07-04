package com.apphance.flow.plugins.ios.apphance.pbx

import com.apphance.flow.configuration.ios.variants.IOSVariant

interface IOSApphancePbxEnhancerFactory {

    IOSApphancePbxEnhancer create(IOSVariant variant)
}