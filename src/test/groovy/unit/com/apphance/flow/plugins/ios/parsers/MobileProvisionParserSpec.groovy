package com.apphance.flow.plugins.ios.parsers

import com.apphance.flow.executor.IOSExecutor
import spock.lang.Specification

class MobileProvisionParserSpec extends Specification {

    def input = new File('testProjects/ios/GradleXCode/release/distribution_resources/Ameba_Test_Project.mobileprovision.xml')
    def executor
    def parser

    def setup() {
        executor = GroovyMock(IOSExecutor)
        executor.mobileprovisionToXml(_) >> input.text.split('\n')

        parser = new MobileProvisionParser()
        parser.executor = executor
    }

    def 'bundle id is read correctly'() {
        expect:
        parser.bundleId(Mock(File)) == 'com.apphance.flow'
    }

    def 'udids are read correctly'() {
        expect:
        parser.udids(Mock(File)) == [
                'c66f6b79a8473d858a73908ee08c41a0a9400a64',
                '4860904159146be176cfea2f4ddd3beffb38806d',
                'e202fda7d6ab419d847d9fbf6d834ac7f2b7964f',
                'e5de7d3f10023ea395f454cdacaac294997617f0',
                'a397a1b037b6420e74add4f774de4db33410ed38',
                '98abe71145419bce57254616cdddeefca0d70e86',
                'cba04d41c62bcb845193a6942b13d7876e7849e0',
                '26049eaba5c9af32ac3ac0b1475c69cb971db512',
                'e099358191aef77651995c44e9c01e0a9c138782',
                'ae0aadfb8d7f60cfcc40f4b63da96120f1cb2465',
                '074b4502dcf5af019a5b1f7e3eb304129314d5d7',
                'bea8af0efd1674d7efe2a82ec9e66aab74eee6ea',
                '6d9665fdc0a916e184ad346846fe97d67ab64088',
                '139fbb263b2d9d78417b90c71dbee6aa97f04a0f',
                'bf92928a1ec68f7e153751c38946f02c72ab6bd6',
                'd236d087781bf2d33426e227ceb1bbf84d60f862',
                '249e3d99dd3b918bdcafda9280fcba4a5c7e7281',
                '4b75d1db0b8ce04fde8a7ccd0d576f11f4c72415',
                '18250ed94639fb94517f9cd55c039308a9b5d2ad',
        ]
    }
}
