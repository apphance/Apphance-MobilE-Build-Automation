package com.apphance.flow.detection.project

import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.detection.project.ProjectType.IOS

class ProjectTypeDetector {

    private static final String IOS_PROJECT_FILE_SUFFIX = '.xcodeproj'
    private static final String ANDROID_PROJECT_DESCRIPTOR = 'AndroidManifest.xml'

    ProjectType detectProjectType(File projectRoot) {
        def matchingTypes = [
                (ANDROID): isAndroidProject(projectRoot),
                (IOS): isiOSProject(projectRoot)
        ].findAll { it.value }.keySet() as List

        switch (matchingTypes.size()) {
            case 1:
                return matchingTypes[0]
            case 0:
                throw new RuntimeException('No valid project detected')
            default:
                throw new RuntimeException("More than one valid project detected: $matchingTypes")
        }
    }

    private ProjectType isAndroidProject(File projectRoot) {
        projectRoot.list().find { it == ANDROID_PROJECT_DESCRIPTOR } ? ANDROID : null
    }

    private ProjectType isiOSProject(File projectRoot) {
        projectRoot.list().find { it.endsWith(IOS_PROJECT_FILE_SUFFIX) } ? IOS : null
    }
}
