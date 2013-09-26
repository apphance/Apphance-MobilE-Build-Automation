package com.apphance.flow.docs

import spock.lang.Shared
import spock.lang.Specification

class FlowTemplateEngineSpec extends Specification {

    @Shared tmplEngine = new FlowTemplateEngine()

    def 'task template is filled with data'() {
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

    def 'conf template is filled with data'() {
        given:
        def conf = [
                confName: 'Conf',
                confDescription: 'Conf description',
                confProperties: [
                        [
                                name: 'sample.property.name',
                                description: 'This is very important property'
                        ],
                        [
                                name: 'sample.property.name2',
                                description: 'This is very, very important property'
                        ],
                ],
        ]
        expect:
        tmplEngine.fillConfTemplate(conf) == '<h3 style="border-bottom:dotted 1px #aaa">\n' +
                '    <font size="4">Conf</font>\n' +
                '</h3>\n' +
                '\n' +
                '<br/>\n' +
                '\n' +
                '<div>\n' +
                '    Conf description\n' +
                '</div>\n' +
                '\n' +
                '\n' +
                '<h4>Properties</h4>\n' +
                '\n' +
                '\n' +
                '<div class="sites-codeblock sites-codesnippet-block">\n' +
                '    <div>\n' +
                '        <div style="font-family:courier new,monospace">\n' +
                '            <code>\n' +
                '                <font color="#6aa84f">sample.property.name</font>\n' +
                '            </code>\n' +
                '        </div>\n' +
                '    </div>\n' +
                '</div>\n' +
                '<br/>\n' +
                '<div style="text-align:justify">\n' +
                '    This is very important property\n' +
                '</div>\n' +
                '<br/>\n' +
                '<br/>\n' +
                '\n' +
                '<div class="sites-codeblock sites-codesnippet-block">\n' +
                '    <div>\n' +
                '        <div style="font-family:courier new,monospace">\n' +
                '            <code>\n' +
                '                <font color="#6aa84f">sample.property.name2</font>\n' +
                '            </code>\n' +
                '        </div>\n' +
                '    </div>\n' +
                '</div>\n' +
                '<br/>\n' +
                '<div style="text-align:justify">\n' +
                '    This is very, very important property\n' +
                '</div>\n' +
                '<br/>\n' +
                '<br/>\n' +
                '\n' +
                ''
    }

}
