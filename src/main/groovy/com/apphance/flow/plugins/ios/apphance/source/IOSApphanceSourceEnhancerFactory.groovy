package com.apphance.flow.plugins.ios.apphance.source

import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.plugins.ios.apphance.pbx.IOSApphancePbxEnhancer

interface IOSApphanceSourceEnhancerFactory {
    IOSApphanceSourceEnhancer create(IOSVariant variant, IOSApphancePbxEnhancer apphancePbxEnhancer)
}