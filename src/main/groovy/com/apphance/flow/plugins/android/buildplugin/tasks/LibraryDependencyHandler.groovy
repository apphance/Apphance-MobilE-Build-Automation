package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.plugins.android.parsers.AndroidManifestHelper
import com.apphance.flow.util.FlowUtils
import groovy.transform.PackageScope
import org.gradle.api.logging.Logging

import static com.apphance.flow.util.file.FileManager.*
import static groovy.io.FileType.FILES

@Mixin([AndroidManifestHelper, FlowUtils])
class LibraryDependencyHandler {

    def logger = Logging.getLogger(this.class)

    List<File> handleLibraryDependencies(File projectRoot) {
        List<File> libraryProjects = findLibraries(projectRoot)
        logger.info "Handle library dependencies in $projectRoot.absolutePath. Libraries: ${libraryProjects*.absolutePath}"

        def allLibraries = libraryProjects
        libraryProjects.each { allLibraries += handleLibraryDependencies(it) }

        if (libraryProjects && isAndroidLibrary(projectRoot)) {
            logger.info "android.library is true. Modifying build.xml"

            def relativePaths = libraryProjects.collect { relativeTo(projectRoot, it) }
            def excludes = getExcludes(allLibraries)
            modifyBuildXml projectRoot, relativePaths, excludes
        }
        allLibraries
    }

    @PackageScope
    List<String> getExcludes(List<File> allLibraries) {
        libPackageExcludes(allLibraries) + aidlExcludes(allLibraries)
    }

    List<String> libPackageExcludes(List<File> allLibraries) {
        def packages = allLibraries.collect { androidPackage(it).replace('.', '/') }
        packages.collect { "$it/*" } as List<String>
    }

    @PackageScope
    List<String> aidlExcludes(List<File> allLibraries) {
        aidlFiles(allLibraries).collect { excludesFromAidlFile(it) }.flatten()
    }

    @PackageScope
    List<File> aidlFiles(List<File> allLibraries) {
        List<File> files = []
        allLibraries.each { File lib ->
            File src = new File(lib, 'src')
            logger.info "Searching for AIDL files in: ${src.absolutePath}"
            if (!src.exists()) {
                logger.info "No src dir. Continue in next lib"
                return
            }
            src.traverse(type: FILES, nameFilter: ~/(?i).*\.aidl/) {
                logger.info "Found AIDL file: $it.absolutePath. Name: $it.name, package: ${getPackage(it)}"
                files += it
            }
        }
        files
    }

    @PackageScope
    List<String> excludesFromAidlFile(File aidlFile) {
        String aidlPackage = getPackage(aidlFile).replace('.', '/')
        String fullName = aidlPackage + '/' + aidlFile.name.replaceAll("(?i).aidl", '')
        ["${fullName}.class".toString(), "${fullName}\$*.class", "${aidlPackage}/$aidlFile.name"]
    }

    @PackageScope
    void modifyBuildXml(File projectRoot, List<String> relativePaths, List<String> excludes) {
        def fileSets = relativePaths.collect { """ <fileset dir="$it/gen/"/> """ }.join(' ')
        addTarget projectRoot, preCompile(fileSets)
        addTarget projectRoot, postCompile(excludes.join(' '))
    }

    @PackageScope
    void addTarget(File projectRoot, String target) {
        def buildFile = new File(projectRoot, 'build.xml')
        logger.info "Modifying $buildFile.absolutePath. Adding target: $target"
        replace(buildFile, '</project>', target + '\n</project>')
    }

    @PackageScope
    List<File> findLibraries(File root) {
        def propFile = new File(root, 'project.properties')
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

                    <delete>
                        <fileset dir="\${out.classes.absolute.dir}" includes="$replacement" />
                    </delete>

                    <propertybyreplace name="project.app.package.path" input="\${project.app.package}" replace="." with="/" />

                    <jar destfile="\${out.library.jar.file}">
                        <fileset dir="\${out.classes.absolute.dir}" includes="**/*.class"
                        excludes="\${project.app.package.path}/R.class \${project.app.package.path}/R\$*.class \${project.app.package.path}/BuildConfig.class"/>

                        <fileset dir="\${source.absolute.dir}" excludes="**/*.java \${android.package.excludes}" />
                    </jar>
                </then>
            </if>
        </target>
        """
    }
}
