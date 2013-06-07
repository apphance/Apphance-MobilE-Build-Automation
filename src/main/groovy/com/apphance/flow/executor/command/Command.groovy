package com.apphance.flow.executor.command

import groovy.text.SimpleTemplateEngine

class Command {

    File runDir
    List<String> cmd
    List<String> input

    Map<String, Object> params = [:]
    Map<String, Object> secretParams = [:]
    Map<String, String> environment = [:]

    boolean failOnError = true

    def getCommandForExecution() {
        getFilledCommand(params + secretParams)
    }

    def getCommandForPublic() {
        getFilledCommand(params + secretParams.collectEntries { [it.key, '???'] }).join(' ')
    }

    private getFilledCommand(Map args) {
        try {
            //there's a need to explicitly cast 'it' to String in case of not String object passed by 'cmd' list
            return cmd.collect { new SimpleTemplateEngine().createTemplate(it as String).make(args).toString() }

        } catch (e) {
            throw new IllegalStateException(
                    """Failed to construct command from parameters.
                    |Command: $cmd
                    |Params: $params
                    |Environment: $environment
                    |Secret params names: ${secretParams.keySet()}""".stripMargin(), e)
        }
    }

    @Override
    public String toString() {
        this.properties
    }
}