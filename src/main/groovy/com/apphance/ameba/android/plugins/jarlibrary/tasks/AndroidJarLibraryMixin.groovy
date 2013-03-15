package com.apphance.ameba.android.plugins.jarlibrary.tasks

class AndroidJarLibraryMixin {

    String getJarLibraryFilePath(String mainProjectName, String versionString) {
        "bin/${mainProjectName}_${versionString}.jar".toString()
    }
}
