package com.apphance.flow.di

import com.apphance.flow.detection.project.ProjectTypeDetector
import com.google.inject.AbstractModule
import org.gradle.api.Project

import static com.apphance.flow.detection.os.OS.isFamilyWin9x
import static com.apphance.flow.detection.os.OS.isFamilyWindows
import static com.apphance.flow.detection.project.ProjectType.ANDROID
import static com.apphance.flow.executor.ExecutableCommand.*
import static com.google.inject.name.Names.named

class AndroidModule extends AbstractModule {

    private Project project
    private ProjectTypeDetector projectTypeDetector = new ProjectTypeDetector()

    private executables = [
            WIN: [
                    'executable.android': WIN_EXECUTABLE_ANDROID,
                    'executable.adb': WIN_EXECUTABLE_ADB,
                    'executable.emulator': WIN_EXECUTABLE_EMULATOR,
                    'executable.ant': WIN_EXECUTABLE_ANT,
            ],
            OTHER: [
                    'executable.android': STD_EXECUTABLE_ANDROID,
                    'executable.adb': STD_EXECUTABLE_ADB,
                    'executable.emulator': STD_EXECUTABLE_EMULATOR,
                    'executable.ant': STD_EXECUTABLE_ANT,
            ]
    ]

    AndroidModule(Project project) {
        this.project = project
    }

    @Override
    protected void configure() {
        def os = 'OTHER'
        if ((isFamilyWindows() || isFamilyWin9x()) && projectTypeDetector.detectProjectType(project.rootDir) == ANDROID) {
            os = 'WIN'
        }
        executables[os].each { k, v -> bindConstant().annotatedWith(named(k)).to(v) }
    }
}
