package com.apphance.flow.configuration.android

import com.apphance.flow.TestUtils
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

import javax.imageio.ImageIO

import static com.apphance.flow.configuration.android.AndroidReleaseConfiguration.ICON_ORDER
import static com.apphance.flow.configuration.android.AndroidReleaseConfiguration.getDRAWABLE_DIR_PATTERN

@Mixin([FlowUtils, TestUtils])
class AndroidReleaseConfigurationSpec extends Specification {

    def 'DRAWABLE_DIR_PATTERN'() {
        expect:
        (input ==~ DRAWABLE_DIR_PATTERN) == result

        where:
        input            | result
        'drawable-ldpi'  | true
        'drawable-mdpi'  | true
        'drawable-hdpi'  | true
        'drawable-xhdpi' | true
        'drawable'       | true
        'drawable-'      | false
        'drawable-abc'   | false
        'abc'            | false
    }

    def 'test icon path sort'() {
        given:
        def hdpi = '/Users/user/work/project/res/drawable-hdpi/icon.png'
        def relativeHdpi = 'res/drawable-hdpi/icon.png'
        def ldpi = '/Users/user/work/project/res/drawable-ldpi/icon.png'
        def xhdpi = '/Users/user/work/project/res/drawable-xhdpi/icon.png'
        def mdpi = '/Users/user/work/project/res/drawable-mdpi/icon.png'
        def drawable = '/Users/user/work/project/res/drawable/icon.png'
        def image = '/Users/user/work/project/res/drawable/image.png'
        def relativeImage = 'project/res/drawable/image.png'

        def paths = [hdpi, relativeHdpi, ldpi, xhdpi, mdpi, drawable, image, relativeImage]

        expect:
        paths.sort(ICON_ORDER) == [drawable, image, relativeImage, ldpi, mdpi, hdpi, relativeHdpi, xhdpi]

    }

    def 'test default icon'() {
        given:
        def configuration = new AndroidReleaseConfiguration()

        when:
        def icon = configuration.androidIcon

        then:
        icon.exists()
        icon.size() > 100
        ImageIO.read(icon)
        icon.name == 'defaultIcon.png'
    }
}
