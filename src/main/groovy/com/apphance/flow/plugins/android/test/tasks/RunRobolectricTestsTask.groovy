package com.apphance.flow.plugins.android.test.tasks

import com.apphance.flow.configuration.android.AndroidTestConfiguration
import com.apphance.flow.configuration.android.variants.AndroidVariantConfiguration
import com.apphance.flow.executor.AntExecutor
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

import static com.apphance.flow.plugins.FlowTasksGroups.FLOW_TEST

class RunRobolectricTestsTask extends DefaultTask {

    String group = FLOW_TEST
    String description = 'Runs Robolectric test on the project'

    @Inject AntExecutor antExecutor
    @Inject AndroidTestConfiguration androidTestConf

    AndroidVariantConfiguration variantConf

    @TaskAction
    def runRobolectricTests() {
        logger.lifecycle "Running robolectric test for variant: $variantConf.name"
        addTestTasksToBuildXml new File(variantConf.tmpDir, 'build.xml')
        antExecutor.executeTarget variantConf.tmpDir, 'test'
    }

    void addTestTasksToBuildXml(File buildXml) {
        logger.info "Adding ant robolectric test tasks to $buildXml.absolutePath"

        if (buildXml.text =~ /target\W*name\W*=\W*"test"/) {
            logger.lifecycle "Not adding test target. It already exists."
        } else {
            buildXml.text = buildXml.text.replace('</project>', robolectricTasks + '\n</project>')
        }
    }

    @Lazy
    def robolectricTestPath = (androidTestConf?.testDir?.value?.toString() ?: 'test') + '/robolectric'

    @Lazy
    def robolectricTasks = """
        <property name="test.absolute.dir" location="$robolectricTestPath"/>
        <property name="out.test-classes.dir" value="\${out.absolute.dir}/testClasses"/>
        <property name="out.test-classes.absolute.dir" value="\${out.test-classes.dir}"/>
        <property name="publish.dir" location="\${out.absolute.dir}/publish"/>

        <property environment="env"/>
        <condition property="build.number" value="\${env.BUILD_NUMBER}" else="unknown">
            <isset property="env.BUILD_NUMBER"/>
        </condition>
        <exec executable="date" outputproperty="build.date"/>

        <target name="-pre-compile">
            <property name="tested.project.absolute.dir" location="." />
            <property name="tested.project.test.absolute.dir"
                location="\${tested.project.absolute.dir}/$robolectricTestPath"/>
            <property name="out.test.classes.absolute.dir" location="\${out.dir}/testClasses" />
        </target>

        <target name="compile.tests" depends="-set-debug-files, -set-debug-mode, -compile">
            <mkdir dir="\${out.test.classes.absolute.dir}"/>

            <path id="project.javac.classpath">
                <path refid="project.all.jars.path"/>
                <path refid="tested.project.classpath"/>
            </path>

            <javac encoding="\${java.encoding}"
                   source="\${java.source}"
                   target="\${java.target}"
                   debug="true"
                   extdirs=""
                   destdir="\${out.test.classes.absolute.dir}"
                   bootclasspathref="project.target.class.path"
                   verbose="\${verbose}"
                   classpathref="project.javac.classpath"
                   includeantruntime="false">
                <src path="\${source.absolute.dir}" />
                <src path="\${gen.absolute.dir}" />

                <!-- test src -->
                <src path="\${tested.project.test.absolute.dir}"/>
                <classpath>
                    <pathelement path="\${out.classes.absolute.dir}"/>
                    <fileset dir="\${jar.libs.dir}" includes="*.jar"/>
                    <fileset dir="\${tested.project.absolute.dir}/lib/test" includes="**/*.jar"/>
                </classpath>
            </javac>
        </target>

        <target name="-pre-clean" description="cleanup module">
            <delete dir="\${out.test-classes.absolute.dir}"/>
            <delete dir="\${tmp.dir}"/>
        </target>

        <target name="test" depends="compile.tests" description="test all">
            <mkdir dir="\${basedir}/out/reports/tests"/>
            <junit showoutput="true" failureproperty="junit.failure">
                <formatter type="plain" usefile="false" />
                <formatter type="xml"/>
                <batchtest todir="\${basedir}/out/reports/tests">
                    <fileset dir="\${test.absolute.dir}">
                        <include name="**/*Test.java"/>
                    </fileset>
                </batchtest>
                <classpath>
                    <pathelement path="\${out.classes.absolute.dir}"/>
                    <pathelement path="\${out.test-classes.absolute.dir}"/>
                    <fileset dir="\${jar.libs.dir}" includes="*.jar"/>
                    <fileset dir="\${tested.project.absolute.dir}/lib/test" includes="**/*.jar"/>
                    <path refid="project.target.class.path"/>
                    <pathelement path="\${sdk.dir}/extras/android/support/v4/android-support-v4.jar"/>
                </classpath>
            </junit>
            <fail if="junit.failure" message="Unit test(s) failed.  See reports!"/>
        </target>"""
}
