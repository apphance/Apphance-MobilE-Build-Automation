package com.apphance.ameba.configuration

import java.lang.reflect.Field

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
                c.amebaProperties.each { Field f ->
                    f.accessible = true
                    Prop ap = (Prop) f.get(c)
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
