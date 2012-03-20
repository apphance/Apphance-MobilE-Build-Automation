package com.apphance.ameba.android

import java.util.List

import org.gradle.api.Project

import com.apphance.ameba.ProjectHelper


class AndroidCommandParser {

    public static List getTargets(Project project) {
        ProjectHelper projectHelper = new ProjectHelper()
        String [] commandTarget =[
            'android',
            'list',
            'target'
        ]
        File outFile = project.file("tmp/android_list_target.txt")
        outFile.delete()
        outFile.parentFile.mkdirs()
        Process process = projectHelper.executeCommandInBackground(project.rootDir, outFile, commandTarget)
        process.waitFor()

        def res = outFile.text
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
