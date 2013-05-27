package com.apphance.ameba.plugins.android.test.tasks

import com.apphance.ameba.configuration.android.AndroidConfiguration
import groovy.text.SimpleTemplateEngine
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.ameba.plugins.AmebaCommonBuildTaskGroups.AMEBA_TEST
import static org.gradle.api.logging.Logging.getLogger

class PrepareRobolectricTask extends DefaultTask {

    private l = getLogger(getClass())

    static String NAME = 'prepareRobolectric'
    String group = AMEBA_TEST
    String description = 'Prepares file structure for Robolectric test framework'

    private String robolectricPath = 'test/robolectric'

    @Inject AndroidConfiguration androidConf

    @TaskAction
    void prepareRobolectric() {
        File path = new File(project.rootDir.path, robolectricPath)
        if (path.exists()) {
            println "Robolectric test directory exists, now I'm going to recreate the project (no source files are going to be touched)"
            setUpRobolectricProject(path)
        } else {
            setUpRobolectricProject(path)
            copyFirstTestActivity(path)
        }
    }

    private void setUpRobolectricProject(File path) {
        path.mkdirs()
        makeRobolectricDirs(path)
        project.configurations.robolectric.each {
            downloadFile(it.toURI().toURL(), new File(path.path + File.separator + 'libs' + File.separator + it.name))
        }
        copyFindSdkGrade(path)
        copyBuildGrade(path)
    }

    private void makeRobolectricDirs(File path) {
        new File(path.path + File.separator + 'libs').mkdirs()
        new File(path.path + File.separator + 'src' + File.separator + 'main' + File.separator + 'java').mkdirs()
        new File(roboPath(path)).mkdirs()
    }

    private String roboPath(File path) {
        String _path = androidConf.mainPackage.replace('.', File.separator)
        return path.path + File.separator + 'src' + File.separator + 'test' + File.separator + 'java' + File.separator + _path + File.separator + 'test'
    }

    @groovy.transform.PackageScope
    void downloadFile(URL url, File file) {
        l.info("Downloading file from ${url} to ${file}")
        def stream = new FileOutputStream(file)
        def out = new BufferedOutputStream(stream)
        out << url.openStream()
        out.close()
    }

    private void copyFindSdkGrade(File path) {
        copyFromResources(path, 'findSdk.gradle');
    }

    private void copyBuildGrade(File path) {
        copyFromResources(path, 'build.gradle');
    }

    private void copyFromResources(File path, String fileName) {
        FileOutputStream output = new FileOutputStream(path.path + File.separator + fileName)
        InputStream stream = this.class.getResource(fileName + '_').openStream();

        byte[] buffer = new byte[256];

        while (true) {
            def bytesRead = stream.read(buffer)
            if (bytesRead == -1)
                break;
            output.write(buffer, 0, bytesRead)
        }
    }

    private void copyFirstTestActivity(File path) {
        File output = new File(roboPath(path) + File.separator + 'MyFirstTest.java')

        URL testClassTemplate = this.class.getResource("MyFirstTest.java_")

        SimpleTemplateEngine engine = new SimpleTemplateEngine()
        def binding = [packageName: androidConf.mainPackage]
        def result = engine.createTemplate(testClassTemplate).make(binding)
        output.write(result.toString())
    }
}
