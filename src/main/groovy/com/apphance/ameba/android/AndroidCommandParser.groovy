package com.apphance.ameba.android

import java.util.List;


class AndroidCommandParser {
    public static List getTargets() {
        Process process = ['android', 'list', 'target'].execute()
        process.waitFor()

        def res = process.inputStream.text
        return extractTargets(res)
    }

    public static List extractTargets(String text) {
        List targets = []
        text.split('\n').each {
            if (it.startsWith('id:') ) {
                def matcher =  (it =~ /id:.*"(.*)"/)
                if (matcher.matches()) {
                    targets << matcher[0][1]
                }
            }
        }
        return targets.sort()
    }
}
