package com.apphance.flow.plugins.android.apphance

import com.apphance.flow.plugins.android.apphance.tasks.AddApphanceToAndroid

import static com.apphance.flow.configuration.apphance.ApphanceMode.PROD
import static com.apphance.flow.configuration.apphance.ApphanceMode.QA

class Main {

    public static void main(String[] args) {
        println "Apphance Flow main method"
        if(args.size() == 2) {
            String mode = args[0]
            assert mode in ['prod', 'pre']
            def apphanceKey = args[1]
            File currentDir = new File('.')
            File manifest = new File(currentDir, 'AndroidManifest.xml')
            assert manifest.exists(), "It is not an Android project. Run this application inside root directory of your app"

            println "Adding apphance in mode: $mode, key: $apphanceKey"

            def apphance = new AddApphanceToAndroid(currentDir, apphanceKey, mode == 'prod' ? PROD : QA, '1.9-RC1')
            apphance.addApphance()

        } else {
            println "Usage: java -jar flow.jar [prod|pre] <APPHANCE_KEY> "
        }
    }
}
