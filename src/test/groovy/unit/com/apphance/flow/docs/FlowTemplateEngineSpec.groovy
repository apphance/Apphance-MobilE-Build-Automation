package com.apphance.flow.docs

import spock.lang.Shared
import spock.lang.Specification

class FlowTemplateEngineSpec extends Specification {

    @Shared tmplEngine = new FlowTemplateEngine()

    def 'template task is filled with data'() {
        given:
        def tasksGroup = [
                header: 'Some group',
                groupName: 'Some group',
                groupNameFull: 'This is flow some group',
                groupDescription: 'This is the group description',
                tasks: [
                        [
                                taskName: 't1',
                                taskDescription: 't1 desc',
                        ],
                        [
                                taskName: 't2',
                                taskDescription: 't2 desc',
                        ],
                ],
        ]

        expect:
        println tmplEngine.fillTaskTemplate(tasksGroup)
        tmplEngine.fillTaskTemplate(tasksGroup) == '<h3 style="border-bottom:dotted 1px #aaa">\n' +
                '    <font size="4">Some group</font>\n' +
                '</h3>\n' +
                '\n' +
                '<div>\n' +
                '    <br>\n' +
                '</div>\n' +
                '\n' +
                '<div>\n' +
                '    Name: <span style="color:#bd4401;font-weight:bold">Some group</span>\n' +
                '</div>\n' +
                '\n' +
                '<div>\n' +
                '    <br>\n' +
                '</div>\n' +
                '\n' +
                '<div>This is flow some group</div>\n' +
                '<br>\n' +
                '<div>This is the group description</div>\n' +
                '\n' +
                '\n' +
                '<h4>Tasks</h4>\n' +
                '<div>\n' +
                '    <ul>\n' +
                '        \n' +
                '        <li>\n' +
                '            <span style="background-color:transparent">\n' +
                '                <b>t1</b>\n' +
                '                <br/>\n' +
                '                t1 desc\n' +
                '            </span>\n' +
                '        </li>\n' +
                '        \n' +
                '        <li>\n' +
                '            <span style="background-color:transparent">\n' +
                '                <b>t2</b>\n' +
                '                <br/>\n' +
                '                t2 desc\n' +
                '            </span>\n' +
                '        </li>\n' +
                '        \n' +
                '    </ul>\n' +
                '</div>\n' +
                ''
    }
}
