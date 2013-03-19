package com.apphance.ameba.executor.command

import spock.lang.Specification

class CommandSpec extends Specification {

    def 'command for public is displayed correct'() {

        expect:
        commandForPublic == new Command(cmd: cmd, params: params, secretParams: secretParams).commandForPublic

        where:
        commandForPublic                     | cmd                                              | params                 | secretParams
        'ls -al'                             | ['ls', '-al']                                    | [:]                    | [:]
        'ls -al /tmp'                        | ['ls', '-al', '$dir']                            | [dir: '/tmp']          | [:]
        'upload -u user -p ??? /tmp/app.apk' | ['upload', '-u', 'user', '-p', '$pass', '$file'] | [file: '/tmp/app.apk'] | [pass: 'pass']

    }

    def 'command for execution is displayed correct'() {
        expect:
        commandForExecution == new Command(cmd: cmd, params: params, secretParams: secretParams).commandForExecution

        where:
        commandForExecution                                    | cmd                                              | params                 | secretParams
        ['ls', '-al']                                          | ['ls', '-al']                                    | [:]                    | [:]
        ['ls', '-al', '/tmp']                                  | ['ls', '-al', '$dir']                            | [dir: '/tmp']          | [:]
        ['upload', '-u', 'user', '-p', 'pass', '/tmp/app.apk'] | ['upload', '-u', 'user', '-p', '$pass', '$file'] | [file: '/tmp/app.apk'] | [pass: 'pass']
    }

    def 'command for execution with env variables is displayed correct'() {
        expect:
        commandForExecution == new Command(cmd: cmd, params: params, secretParams: secretParams, environment: env).commandForExecution

        where:
        commandForExecution                                                          | cmd                                                                    | params                 | secretParams   | env
        ['ls', '-al', '$SAMPLE_ENV']                                                 | ['ls', '-al', '\\$SAMPLE_ENV']                                         | [:]                    | [:]            | [SAMPLE_ENV: '/tmp']
        ['ls', '-al', '/tmp', '$ANOTHER_ENV']                                        | ['ls', '-al', '$dir', '\\$ANOTHER_ENV']                                | [dir: '/tmp']          | [:]            | [ANOTHER_ENV: 'sample_dir']
        ['upload', '-u', 'user', '-p', 'pass', '/tmp/app.apk', '$YET_ANOTHER_ENV'] | ['upload', '-u', 'user', '-p', '$pass', '$file', '\\$YET_ANOTHER_ENV'] | [file: '/tmp/app.apk'] | [pass: 'pass'] | [YET_ANOTHER_ENV: 'sample_key']
    }

    def 'building command for public fails for incorrect params'() {
        when:
        new Command(cmd: ['ls', '-al', '$dir'], params: [dri: '/tmp']).commandForPublic

        then:
        def exception = thrown(IllegalStateException)
        exception.message == '''|Failed to construct command from parameters.
                                |Command: [ls, -al, $dir]
                                |Params: [dri:/tmp]
                                |Environment: [:]
                                |Secret params names: []'''.stripMargin()
    }

    def 'building command for execution fails for incorrect params'() {
        when:
        new Command(cmd: ['ls', '-al', '$pass'], secretParams: [pasa: 'pass']).commandForExecution

        then:
        def exception = thrown(IllegalStateException)
        exception.message == '''|Failed to construct command from parameters.
                                |Command: [ls, -al, $pass]
                                |Params: [:]
                                |Environment: [:]
                                |Secret params names: [pasa]'''.stripMargin()
    }
}
