package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import groovy.transform.PackageScope
import org.gradle.api.logging.Logging

import static com.apphance.flow.util.file.FileManager.*

@Mixin(AndroidManifestHelper)
class LibraryDependencyHandler {

    def logger = Logging.getLogger(this.class)

    File projectRoot

    void handleLibraryDependencies() {
        List<File> libraryProjects = findLibraries(projectRoot)
        logger.info "Handle library dependencies in $projectRoot.absolutePath. Libraries: ${libraryProjects*.absolutePath}"

        libraryProjects.each {
            new LibraryDependencyHandler(projectRoot: it).handleLibraryDependencies()
        }

        if (libraryProjects && isAndroidLibrary(projectRoot)) {
            logger.info "android.library is true. Modifying build.xml"
            modifyBuildXml libraryProjects
        }
    }

    @PackageScope
    void modifyBuildXml(List<File> libraryProjects) {
        def fileSets = libraryProjects.collect { """ <fileset dir="${relativeTo(projectRoot, it)}/gen/"/> """ }.join(' ')
        addTarget preCompile(fileSets)

        def packages = libraryProjects.collect { androidPackage(it).replace('.', '/') }
        def excludes = packages.collect { " $it/R.class $it/R\$*.class $it/BuildConfig.class " }.join(' ')
        addTarget postCompile(excludes)
    }

    @PackageScope
    void addTarget(String target) {
        def buildFile = new File(projectRoot, 'build.xml')
        logger.info "Modifying $buildFile.absolutePath. Adding target: $target"
        replace(buildFile, '</project>', target + '\n</project>')
    }

    @PackageScope
    List<File> findLibraries(File root) {
        def propFile = new File(projectRoot, 'project.properties')
        def references = asProperties(propFile).findAll { it.key.startsWith('android.library.reference') }
        def libNames = references.collect { it.value } as List<String>
        libNames.collect { new File(root, it) }.findAll { it.exists() }
    }

    String preCompile(String replacement) {
        """
        <target name="-pre-compile">
            <copy todir="gen">
                $replacement
            </copy>
        </target>
        """
    }

    String postCompile(String replacement) {
        """
        <target name="-post-compile">
            <if condition="\${project.is.library}">
                <then>
                    <echo level="info">Creating library output jar file.</echo>
                    <delete file="bin/classes.jar"/>

                    <propertybyreplace name="project.app.package.path" input="\${project.app.package}" replace="." with="/" />

                    <jar destfile="\${out.library.jar.file}">
                        <fileset dir="\${out.classes.absolute.dir}" includes="**/*.class"
                        excludes="\${project.app.package.path}/R.class \${project.app.package.path}/R\$*.class \${project.app.package.path}/BuildConfig.class
                        $replacement "/>
                        <fileset dir="\${source.absolute.dir}" excludes="**/*.java \${android.package.excludes}" />
                    </jar>
                </then>
            </if>
        </target>
        """
    }
}
