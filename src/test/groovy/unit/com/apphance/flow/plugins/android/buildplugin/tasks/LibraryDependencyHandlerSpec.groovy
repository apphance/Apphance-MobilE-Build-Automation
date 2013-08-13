package com.apphance.flow.plugins.android.buildplugin.tasks

import com.apphance.flow.TestUtils
import com.apphance.flow.util.FlowUtils
import spock.lang.Specification

import static com.apphance.flow.util.file.FileManager.relativeTo

@Mixin([TestUtils, FlowUtils])
class LibraryDependencyHandlerSpec extends Specification {

    File root, uk, pl, wawa, wola
    def handler = new LibraryDependencyHandler()

    def setup() {
        root = temporaryDir
        newFile root, 'project.properties', 'android.library.reference.1=libs/Poland\nandroid.library.reference.2=libs/UK'

        uk = newDir root, 'libs/UK'
        pl = newDir root, 'libs/Poland'
        newFile uk, 'project.properties', 'android.library=true'
        newFile uk, 'AndroidManifest.xml', '<?xml version="1.0" encoding="utf-8"?>xml<manifest package="com.uk"/>'
        newFile pl, 'project.properties', 'android.library=true\nandroid.library.reference.1=libs/Warsaw'
        newFile pl, 'AndroidManifest.xml', '<?xml version="1.0" encoding="utf-8"?><manifest package="com.pl"/>'

        wawa = newDir pl, 'libs/Warsaw'
        newFile wawa, 'project.properties', 'android.library=true\nandroid.library.reference.1=libs/Wola'
        newFile wawa, 'AndroidManifest.xml', '<?xml version="1.0" encoding="utf-8"?><manifest package="com.wawa"/>'

        wola = newDir wawa, 'libs/Wola'
        newFile wola, 'project.properties', 'android.library=true'
        newFile wola, 'AndroidManifest.xml', '<?xml version="1.0" encoding="utf-8"?><manifest package="com.wola"/>'
    }

    def 'test recursive invocation'() {
        given:
        def handler = GroovySpy(LibraryDependencyHandler)

        when:
        def allLibraries = handler.handleLibraryDependencies(root)

        then:
        1 * handler.handleLibraryDependencies(root)
        1 * handler.handleLibraryDependencies(uk)
        1 * handler.handleLibraryDependencies(pl)
        1 * handler.handleLibraryDependencies(wawa)
        1 * handler.handleLibraryDependencies(wola)

        1 * handler.modifyBuildXml(pl, ['libs/Warsaw'], ['com/wawa/*', 'com/wola/*']) >> null
        1 * handler.modifyBuildXml(wawa, ['libs/Wola'], ['com/wola/*']) >> null
        0 * handler.modifyBuildXml(_, _, _) >> null
        1 * handler.getExcludes([wola])
        1 * handler.getExcludes([wawa, wola])
        0 * handler.getExcludes(_)

        allLibraries.collect { relativeTo(root, it) }.sort() == ['libs/Poland', 'libs/Poland/libs/Warsaw', 'libs/Poland/libs/Warsaw/libs/Wola', 'libs/UK']

    }

    def 'test find libraries'() {
        expect:
        handler.findLibraries(root)*.name.sort() == ["Poland", 'UK']
    }

    def 'test preCompile returns correct target'() {
        expect:
        handler.preCompile('replacement')*.trim() == """
        <target name="-pre-compile">
            <copy todir="gen">
                replacement
            </copy>
        </target>
        """*.trim()
    }

    def 'test postCompile returns correct target'() {
        expect:
        handler.postCompile('replacement').split('\n')*.trim() == """
        <target name="-post-compile">
            <if condition="\${project.is.library}">
                <then>
                    <echo level="info">Creating library output jar file.</echo>
                    <delete file="bin/classes.jar"/>

                    <delete>
                        <fileset dir="\${out.classes.absolute.dir}" includes="replacement" />
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
        """.split('\n')*.trim()
    }

    def 'test add target'() {
        given:
        def buildXml = newFile temporaryDir, 'build.xml', "<project></project>"

        when:
        handler.addTarget(buildXml.parentFile, '<target name="someName"></target>')

        then:
        buildXml.text == '<project><target name="someName"></target>\n</project>'
    }

    def 'test modify build.xml'() {
        given:
        def handler = GroovySpy(LibraryDependencyHandler)
        def dir = temporaryDir
        def relativePaths = ['../somePath', 'other/path/../to/library']
        def excludes = ['pl/com/company/*', 'pl/org/linux/*']

        when:
        handler.modifyBuildXml(dir, relativePaths, excludes)

        then:
        1 * handler.addTarget(dir, handler.preCompile(' <fileset dir="../somePath/gen/"/>   <fileset dir="other/path/../to/library/gen/"/> ')) >> null
        1 * handler.addTarget(dir, handler.postCompile('pl/com/company/* pl/org/linux/*')) >> null

        0 * handler.addTarget(_, _) >> null
    }

    def 'test search aidl file'() {
        given:
        File lib1 = temporaryDir
        File src1 = new File(lib1, 'src')
        src1.mkdir()
        new File(src1, '1.aidl').createNewFile()

        File lib2 = temporaryDir
        File src2 = new File(lib2, 'src/some/sub/dir')
        src2.mkdirs()
        new File(src2, '2.aidl').createNewFile()

        expect:
        handler.aidlFiles([lib1, lib2])*.name.sort() == ['1.aidl', '2.aidl']
    }

    def 'test excludesFromAidlFile'() {
        given:
        File aidl = new File(temporaryDir, 'SomeName.aidl') << 'package com.google.package;'

        expect:
        handler.excludesFromAidlFile(aidl) == ['com/google/package/SomeName.class', 'com/google/package/SomeName$*.class', 'com/google/package/SomeName.aidl']
    }

    def 'test aidlExcludes'() {
        given:
        def handler = GroovySpy(LibraryDependencyHandler)
        handler.aidlFiles(_) >> [
                new File(temporaryDir, 'SomeName.aidl') << 'package com.google.package;',
                new File(temporaryDir, 'Other.aidl') << 'package com.google.other;']

        expect:
        handler.aidlExcludes([]) ==
                ['com/google/package/SomeName.class', 'com/google/package/SomeName$*.class', 'com/google/package/SomeName.aidl',
                        'com/google/other/Other.class', 'com/google/other/Other$*.class', 'com/google/other/Other.aidl']
    }

    def 'test getExcludes call aidl excludes'() {
        given:
        def handler = GroovySpy(LibraryDependencyHandler)
        def allLibraries = [tempFile]

        when:
        handler.getExcludes(allLibraries)

        then:
        1 * handler.libPackageExcludes(allLibraries) >> []
        1 * handler.aidlExcludes(allLibraries) >> []

    }

}
