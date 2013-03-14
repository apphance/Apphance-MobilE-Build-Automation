package com.apphance.ameba.executor

import org.apache.tools.ant.BuildException
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files

class AntExecutorSpec extends Specification {

    @Shared File rootDir = new File('src/test/resources/com/apphance/ameba/executor/antTestProject')

    @Unroll
    def "exception when no build file. Dir #dir, file: #buildFileName"(){
        when:
        new AntExecutor(dir, buildFileName)

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "No $buildFileName in ${dir.path}"

        where:
        dir                | buildFileName
        rootDir            | 'missingFile.xml'
        rootDir.parentFile | 'build.xml'
    }

    def "exception when bad target name"() {
        given:
        def antExecutor = new AntExecutor(rootDir)

        when:
        antExecutor.executeTarget(AntExecutor.CLEAN)

        then:
        def e = thrown(BuildException)
        e.message == 'Target "clean" does not exist in the project "Ant project from src/test/resources/com/apphance/ameba/executor/antTestProject". '
    }

    def "successfully execute target"() {
        given:
        def md5File = new File(rootDir.absolutePath + '/' + 'build.xml.MD5')
        Files.deleteIfExists(md5File.toPath())
        AntExecutor antExecutor = new AntExecutor(rootDir)

        expect:
        !md5File.exists()

        when:
        antExecutor.executeTarget("testTarget")

        then:
        md5File.exists()

        cleanup:
        Files.deleteIfExists(md5File.toPath())
    }

    def "successfully execute target with properties"() {
        given:
        def antExecutor = new AntExecutor(rootDir)

        when:
        antExecutor.executeTarget("testTargetUsingProperties", [firstPart: 'part1', secondPart: 'part2'])

        then:
        antExecutor.antProject.properties.part1part2.length() == 32
    }
}
