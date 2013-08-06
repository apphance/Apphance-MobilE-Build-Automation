package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.google.common.io.Files
import org.gradle.api.logging.Logging

import static java.nio.charset.StandardCharsets.UTF_8

class LibraryDependencyHandler {

    def logger = Logging.getLogger(this.class)

    File root
    @Lazy File propFile = new File(root, 'project.properties')
    @Lazy File buildFile = new File(root, 'build.xml')
    def androidManifestHelper = new AndroidManifestHelper()

    @Lazy Properties projectProperties = {
        def projectProperties = new Properties()
        assert root?.exists()
        assert propFile.exists()
        projectProperties.load(Files.newReader(propFile, UTF_8))
        projectProperties
    }()

    List<File> handleLibraryDependencies() {
        logger.info "Handle library dependencies in $root.absolutePath"
        List<File> libraryProjects = findLibraries(root)
        logger.info "Found libraries: ${libraryProjects*.absolutePath}"

        if (libraryProjects.empty) return []

        List<File> dependentLibraries = libraryProjects.collect { new LibraryDependencyHandler(root: it).handleLibraryDependencies() }.flatten()
        logger.info "Got dependent libraries: ${dependentLibraries*.absolutePath}"
        dependentLibraries.findAll { File dep -> !libraryProjects.any { dep.absolutePath == it.absolutePath } }.each { File it ->
            String lib = root.toPath().relativize(it.toPath()).toString()
            logger.info "Adding $lib as library to $propFile.absolutePath"
            androidManifestHelper.addLibrary(propFile, lib)
        }

        if (projectProperties.getProperty('android.library') as Boolean) {
            logger.info "android.library is true. Modifying build.xml"
            modifyBuildXml(libraryProjects)
        }

        libraryProjects + dependentLibraries
    }

    void modifyBuildXml(List<File> libraryProjects) {
        def fileSet = libraryProjects.collect { "<fileset dir=\"${root.toPath().relativize(it.toPath())}/gen/\"/>" }.join(' ')
        def preCompile = """
                <target name="-pre-compile">
                    <copy todir="gen">
                        $fileSet
                    </copy>
                </target>"""
        addTarget(preCompile)

        def excludes = libraryProjects.collect { androidManifestHelper.androidPackage(it).replace('.', '/') }.collect {
            " $it/R.class $it/R\$*.class $it/BuildConfig.class "
        }.join(' ')

        addTarget(postCompile.replace('excludesPlaceholder', excludes))
    }

    void addTarget(String target) {
        logger.info "Modifying $buildFile.absolutePath. Adding target: $target"
        buildFile.text = buildFile.text.replace('</project>', target + '</project>')
    }

    List<File> findLibraries(File root) {
        List<String> libNames = projectProperties.findAll { it.key.startsWith('android.library.reference') }.collect { it.value } as List<String>
        libNames.collect { new File(root, it) }.findAll { it }
    }

    String postCompile = """
    <target name="-post-compile">
        <if condition="\${project.is.library}">
        <then>
            <echo level="info">Creating library output jar file.</echo>
            <delete file="bin/classes.jar"/>

            <propertybyreplace name="project.app.package.path" input="\${project.app.package}" replace="." with="/" />

            <jar destfile="\${out.library.jar.file}">
                <fileset dir="\${out.classes.absolute.dir}" includes="**/*.class"
                excludes="\${project.app.package.path}/R.class \${project.app.package.path}/R\$*.class \${project.app.package.path}/BuildConfig.class
                excludesPlaceholder
                "/>
                <fileset dir="\${source.absolute.dir}" excludes="**/*.java \${android.package.excludes}" />
            </jar>
        </then>
        </if>
    </target>"""
}
