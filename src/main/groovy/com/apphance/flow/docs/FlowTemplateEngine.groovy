package com.apphance.flow.docs

import groovy.text.SimpleTemplateEngine

class FlowTemplateEngine {

    private engine = new SimpleTemplateEngine()

    String fillTaskTemplate(Map group) {
        def tmpl = getClass().getResource('flow_doc_task.template')
        engine.createTemplate(tmpl).make(group)
    }
}
