package com.apphance.ameba.executor

import groovy.text.SimpleTemplateEngine

class Command {

    File runDir
    Collection<String> cmd
    Collection<String> input

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
            return cmd.collect { lookupElementInEnv(it) ? it : new SimpleTemplateEngine().createTemplate(it).make(args).toString() }

        } catch (e) {
            throw new IllegalStateException(
                    """Failed to construct command from parameters.
Command: $cmd
Params: $params
Environment: $environment
Secret params names: ${secretParams.keySet()}""", e)
        }
    }

    private String lookupElementInEnv(String element) {
        environment[element.replace('$', '')]
    }

    @Override
    public String toString() {
        this.properties
    }

    static void main(args) {
        def pb = new ProcessBuilder('ls', '$LOL')
        pb.environment().put(' LOL', '/Users/opal')
        pb.directory(new File('/Users/opal'))
        println pb.environment()
        def p = pb.start()

        println p.waitFor()
        println p.err.readLines()
        println p.in.readLines()
    }
}