package com.apphance.ameba.android

import java.util.List;
import org.gradle.api.Project

import com.apphance.ameba.ProjectHelper;


class AndroidCommandParser {
    public static List getTargets(Project project) {
		ProjectHelper projectHelper = new ProjectHelper()
		String [] commandTarget =[
            'android',
            'list',
            'target'
        ]
        def outFile = new File(project.rootDir,"tmp/android_list_target.txt")
		outFile.delete()
        Process process = projectHelper.executeCommandInBackground(project.rootDir, outFile, commandTarget)
        //Process process = ['android', 'list', 'target'].execute()
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
