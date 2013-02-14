package com.apphance.ameba.plugins

import com.apphance.ameba.plugins.projectconfiguration.ProjectConfigurationPlugin
import com.google.inject.AbstractModule
import com.google.inject.Guice
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class AmebaPlugin implements Plugin<Project> {

    def l = Logging.getLogger(this.class)

    @Override
    void apply(Project project) {
        l.lifecycle(AMEBA_ASCII_ART)

        def injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(Project).toInstance(project)
            }
        })

        injector
                .getInstance(ProjectConfigurationPlugin)
                .apply(project)

    }

    static String AMEBA_ASCII_ART = '''\

                         ____
   ,---,               ,'  , `.    ,---,.    ,---,.    ,---,
  '  .' \\           ,-+-,.' _ |  ,'  .' |  ,'  .'  \\  '  .' \\
 /  ;    '.      ,-+-. ;   , ||,---.'   |,---.' .' | /  ;    '.
:  :       \\    ,--.'|'   |  ;||   |   .'|   |  |: |:  :       \\
:  |   /\\   \\  |   |  ,', |  '::   :  |-,:   :  :  /:  |   /\\   \\
|  :  ' ;.   : |   | /  | |  ||:   |  ;/|:   |    ; |  :  ' ;.   :
|  |  ;/  \\   \\'   | :  | :  |,|   :   .'|   :     \\|  |  ;/  \\   \\
'  :  | \\  \\ ,';   . |  ; |--' |   |  |-,|   |   . |'  :  | \\  \\ ,'
|  |  '  '--'  |   : |  | ,    '   :  ;/|'   :  '; ||  |  '  '--'
|  :  :        |   : '  |/     |   |    \\|   |  | ; |  :  :
|  | ,'        ;   | |`-'      |   :   .'|   :   /  |  | ,'
`--''          |   ;/          |   | ,'  |   | ,'   `--''
               '---'           `----'    `----'


'''

}
