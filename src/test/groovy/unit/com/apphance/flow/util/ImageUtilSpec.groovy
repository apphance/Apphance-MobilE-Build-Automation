package com.apphance.flow.util

import spock.lang.Specification
import spock.lang.Unroll

import static com.apphance.flow.util.ImageUtil.getImageFrom

class ImageUtilSpec extends Specification {

    def 'test read invalid png'() {
        given:
        def ihdrBadLength = new File('src/test/resources/com/apphance/flow/plugins/release/tasks/bad_length_ihdr.png')

        expect:
        getImageFrom(ihdrBadLength) == null
    }

    @Unroll
    def '#file conversion'() {
        given:
        def dir = 'src/test/resources/com/apphance/flow/plugins/release/tasks/montageFiles/montageFilesSubdir/'
        def source = new File(dir + '1.' + file)

        expect:
        source.exists()
        getImageFrom(source) != null

        where:
        file << ['jpg', 'jpeg', 'gif', 'png', 'raw', 'bmp']
    }
}
