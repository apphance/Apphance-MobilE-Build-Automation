package com.apphance.ameba.android

import com.apphance.ameba.ProjectHelper
import org.gradle.api.Project

/**
 * Parses android utility command line output.
 *
 */
class AndroidCommandParser {

    private static String[] COMMAND_TARGETS = ['android',
            'list',
            'target']

    public static List getTargets(Project project) {
        ProjectHelper projectHelper = new ProjectHelper()
        def targets = projectHelper.executeCommand(project, COMMAND_TARGETS)
        extractTargets(targets.join('\n'))
    }



    public static List extractTargets(String text) {
        def targets = []
        def targetPattern = /id:.*"(.*)"/
        def targetPrefix = 'id:'
        text.split('\n').each {
            def targetMatcher = (it =~ targetPattern)
            if (it.startsWith(targetPrefix) && targetMatcher.matches()) {
                targets << targetMatcher[0][1]
            }
        }
        targets.sort()
    }
}
