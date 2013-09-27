package com.apphance.flow.docs

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.plugins.project.ProjectPlugin
import com.apphance.flow.plugins.release.ReleasePlugin
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
    private FlowTemplateEngine tmplEngine = new FlowTemplateEngine()
    private File pluginHtml = new File('build/doc/plugin.html')
    private File confHtml = new File('build/doc/conf.html')

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

    List<String> superClasses(GroovyClassDoc classDoc) {
        def superclass = classDoc.superclass()
        superclass && classDoc.name() != 'Object' ? [superclass.name()] + superClasses(superclass) : []
    }

    public static void main(String[] args) {
        new FlowPluginReference().run()
    }

    public void run() {
        logger.lifecycle "Building Apphance Flow plugin reference"

        def androidProject = new File("demo/android/android-basic")
        def iosProject = new File('doc-projects/iOS')

        applyAllPluginsInDocMode(androidProject)
        def androidJson = toJson(new File(androidProject, 'build/doc/doc.json'))

        applyAllPluginsInDocMode(iosProject)
        def iosJson = toJson(new File(iosProject, 'build/doc/doc.json'))

        def projectPlugin = ProjectPlugin.simpleName
        def releasePlugin = ReleasePlugin.simpleName

        iosJson.plugins.remove(projectPlugin)
        iosJson.plugins.remove(releasePlugin)

        def commonPlugins = [
                (projectPlugin): androidJson.plugins.remove(projectPlugin),
                (releasePlugin): androidJson.plugins.remove(releasePlugin)
        ]
        def androidPlugins = androidJson.plugins
        def iosPlugins = iosJson.plugins

        def pluginsRef = [commonPlugins, androidPlugins, iosPlugins].collect(this.&generatePluginDoc).join('\n')
        def configurationsRef = [androidJson.configurations, iosJson.configurations].collect(this.&generateConfDoc).join('\n')

        pluginHtml.parentFile.mkdirs()
        pluginHtml.text = pluginsRef

        confHtml.parentFile.mkdirs()
        confHtml.text = configurationsRef
    }

    private void applyAllPluginsInDocMode(File docProject) {
        def connection = newConnector().forProjectDirectory(docProject).connect()
        def buildLauncher = connection.newBuild()
        buildLauncher.setJvmArguments(GRADLE_DAEMON_ARGS)
        buildLauncher.withArguments("-PdocMode=true", '-i', "-PflowProjectPath=${new File('.').absolutePath}").run()
    }

    private Map toJson(File doc) {
        new JsonSlurper().parseText(doc.text) as Map
    }

    private String generatePluginDoc(Map plugins) {
        plugins.collect { String pluginName, List tasks ->
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
    }

    private String generateConfDoc(Map configurations) {
        configurations.collect { String confName, List propertyFields ->
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
    }

    String docText(String entityName) {
        (plugins + tasks + configurations).find { it.name() == entityName }?.commentText()
    }
}
