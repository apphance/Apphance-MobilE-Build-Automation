package com.apphance.ameba.configuration

import com.apphance.ameba.configuration.properties.AbstractProperty

import static java.lang.System.out

class ConversationManager {

    def reader = buildReader()

    def resolveConfigurations(List<Configuration> configurations) {

        configurations.each { Configuration c ->
            if (!c.enabled) {
                print "Enable plugin ${c.configurationName}? [y/n] "
                out.flush()
                String line = reader.readLine()
                if (line.equalsIgnoreCase('y')) {
                    c.enabled = true
                }
            }
            if (c.enabled) {
                c.amebaProperties.each { AbstractProperty ap ->
                    print "${ap.message} [${ap.defaultValue()}]: "
                    out.flush()
                    readPropertyValue(ap)
                }
            }
        }
        //TODO serializer
    }

    private void readPropertyValue(Prop ap) {
        String line = reader.readLine()
        if (line.trim().empty) {
            ap.value = ap.defaultValue()
        } else {
            ap.value = line
        }
    }

    private Reader buildReader() {
        new BufferedReader(new InputStreamReader(System.in))
    }
}
