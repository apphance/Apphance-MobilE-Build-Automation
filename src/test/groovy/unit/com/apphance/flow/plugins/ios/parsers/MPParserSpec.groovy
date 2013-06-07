package com.apphance.flow.plugins.ios.parsers

import spock.lang.Shared
import spock.lang.Specification

class MPParserSpec extends Specification {

    @Shared
    URL provisioningFile
    @Shared
    URL plistFile

    def setupSpec() {
        provisioningFile = this.getClass().getResource("Test.mobileprovision")
        plistFile = this.getClass().getResource("Test.plist")
    }


}
