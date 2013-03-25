package com.apphance.ameba.configuration

class ConversationManager {

    def reader = buildReader()

    def resolveConfigurations(List<Configuration> configurations) {

        configurations.each { Configuration configuration ->
            if (!configuration.enabled) {

                print "Enable plugin ${configuration.getPluginName()}? [y/n] "
                System.out.flush()
                String line = reader.readLine()
                if (line.equalsIgnoreCase('y')) {
                    configuration.enabled = true
                }
            }
            if (configuration.enabled) {
                configuration.amebaProperties.each { AmebaProperty property2 ->
                    print "${property2.message} [${property2.defaultValue()}]: "
                    System.out.flush()
                    //TODO possible values
                    while (!property2.validator(readPropertyValue(property2)));
                }
            }
        }
        //TODO serializer
    }

    private String readPropertyValue(AmebaProperty propertyLol) {
        String line = reader.readLine()
        if (line == 'y') {//FIXME
            propertyLol.value = propertyLol.defaultValue()
        } else {
            propertyLol.value = line
        }
    }

    private Reader buildReader() {
        new BufferedReader(new InputStreamReader(System.in))
    }
}
