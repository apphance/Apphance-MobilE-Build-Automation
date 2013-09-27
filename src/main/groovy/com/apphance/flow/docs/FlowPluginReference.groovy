package com.apphance.flow.docs

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.util.FlowUtils
import groovy.json.JsonSlurper
import org.codehaus.groovy.groovydoc.GroovyClassDoc
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool
import org.gradle.api.Plugin
import org.gradle.api.internal.AbstractTask

import static com.apphance.flow.util.file.FileManager.relativeTo
import static groovy.io.FileType.FILES
import static org.apache.maven.artifact.ant.shaded.FileUtils.extension
import static org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo.*
import static org.gradle.api.logging.Logging.getLogger
import static org.gradle.tooling.GradleConnector.newConnector

@Mixin(FlowUtils)
class FlowPluginReference {

    def static logger = getLogger(FlowPluginReference)

    List<GroovyClassDoc> classes
    def tmplEngine = new FlowTemplateEngine()
    def outputHtml = new File('build/doc/doc.html')

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled', '-XX:+CMSPermGenSweepingEnabled',
            '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    @Lazy List<GroovyClassDoc> plugins = { classes.findAll { it.interfaces()*.name().contains Plugin.simpleName } }()
    @Lazy List<GroovyClassDoc> configurations = { classes.findAll { superClasses(it).contains AbstractConfiguration.simpleName } }()
    @Lazy List<GroovyClassDoc> tasks = { classes.findAll { superClasses(it).contains AbstractTask.simpleName } }()

    FlowPluginReference() {
        File src = new File('src')
        GroovyDocTool docTool = new GroovyDocTool(new ClasspathResourceManager(), [src.path] as String[],
                DEFAULT_DOC_TEMPLATES, DEFAULT_PACKAGE_TEMPLATES, DEFAULT_CLASS_TEMPLATES, [], new Properties()
        )
        List<String> groovySources = []
        src.eachFileRecurse(FILES) {
            if (extension(it.name) == 'groovy') groovySources << relativeTo(src, it)
        }

        docTool.add(groovySources)
        classes = docTool.rootDoc.classes()
    }

    String docText(String entityName) {
        (plugins + tasks + configurations).find { it.name() == entityName }?.commentText()
    }

    List<String> superClasses(GroovyClassDoc classDoc) {
        def superclass = classDoc.superclass()
        superclass && classDoc.name() != 'Object' ? [superclass.name()] + superClasses(superclass) : []
    }

    public static void main(String[] args) {
        new FlowPluginReference().run()
    }

    public void run() {
        logger.lifecycle "Building Apphance Flow plugin reference"

        def docProject = new File("testProjects/android/android-basic")
        applyAllPluginsInDocMode(docProject)

        def json = new JsonSlurper().parseText(new File(docProject, 'build/doc/doc.json').text)
        def pluginHtml = json.plugins.collect { String pluginName, List tasks ->
            tmplEngine.fillTaskTemplate([
                    header: pluginName,
                    groupName: pluginName,
                    groupDescription: docText(pluginName),
                    tasks: tasks.collect {
                        [
                                taskName: it.taskName,
                                taskDescription: [it.description, docText(it.taskClass)].grep().join('<p><p>')
                        ]
                    }
            ])
        }.join('\n')

        def confHtml = json.configurations.collect { String confName, List propertyFields ->
            tmplEngine.fillConfTemplate([
                    confName: confName,
                    confDescription: docText(confName),
                    confProperties: propertyFields.collect {
                        [
                                name: it.name,
                                description: it.description
                        ]
                    }
            ])
        }.join('\n')

        outputHtml.parentFile.mkdirs()
        outputHtml.text = pluginHtml + confHtml
    }

    private void applyAllPluginsInDocMode(File docProject) {
        def connection = newConnector().forProjectDirectory(docProject).connect()
        def buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.withArguments("-PdocMode=true", '-i', "-PflowProjectPath=${new File('.').absolutePath}").run()
    }
}
