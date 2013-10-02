package com.apphance.flow.docs

import com.apphance.flow.configuration.AbstractConfiguration
import com.apphance.flow.util.FlowUtils
import groovy.json.JsonSlurper
import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import org.codehaus.groovy.groovydoc.GroovyClassDoc
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool
import org.gradle.api.Plugin
import org.gradle.api.internal.AbstractTask

import static com.apphance.flow.util.file.FileManager.relativeTo
import static groovy.io.FileType.FILES
import static org.apache.commons.lang.StringUtils.splitByCharacterTypeCamelCase
import static org.apache.maven.artifact.ant.shaded.FileUtils.extension
import static org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo.*
import static org.gradle.api.logging.Logging.getLogger
import static org.gradle.tooling.GradleConnector.newConnector

@Mixin(FlowUtils)
class FlowPluginReference {

    public static final String[] GRADLE_DAEMON_ARGS = ['-XX:MaxPermSize=1024m', '-XX:+CMSClassUnloadingEnabled', '-XX:+CMSPermGenSweepingEnabled',
            '-XX:+HeapDumpOnOutOfMemoryError', '-Xmx1024m'] as String[]

    def static logger = getLogger(FlowPluginReference)

    private TemplateEngine tmplEngine = new SimpleTemplateEngine()

    private File pluginsHtml = new File('build/doc/plugins.html')
    private File confsHtml = new File('build/doc/confs.html')

    @Lazy List<GroovyClassDoc> plugins = { classes.findAll { it.interfaces()*.name().contains Plugin.simpleName } }()
    @Lazy List<GroovyClassDoc> configurations = { classes.findAll { superClasses(it).contains AbstractConfiguration.simpleName } }()
    @Lazy List<GroovyClassDoc> tasks = { classes.findAll { superClasses(it).contains AbstractTask.simpleName } }()
    List<GroovyClassDoc> classes

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

    public void run() {
        logger.lifecycle 'Building Apphance Flow docs'

        def androidProject = new File('doc-projects/android')
        applyAllPluginsInDocMode(androidProject)
        def androidJson = toJson(new File(androidProject, 'build/doc/doc.json'))

        def iosProject = new File('doc-projects/iOS')
        applyAllPluginsInDocMode(iosProject)
        def iosJson = toJson(new File(iosProject, 'build/doc/doc.json'))

        processPlugins(androidJson, iosJson)
        processConfigurations(androidJson, iosJson)
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

    private void processPlugins(Map androidJson, Map iosJson) {
        def androidPlugins = androidJson.plugins.sort { it.key }
        def iosPlugins = iosJson.plugins.sort { it.key }

        def commonPlugins = [
                '0': androidPlugins.remove('0'),
                '1': androidPlugins.remove('1')
        ].sort { it.key }
        iosPlugins.remove('0')
        iosPlugins.remove('1')

        def pluginGroups = [commonPlugins, androidPlugins, iosPlugins].collect(this.&generatePluginDoc)
        def pluginSection = [
                [
                        sectionName: 'Common tasks',
                        section: pluginGroups[0],
                ],
                [
                        sectionName: 'Android tasks',
                        section: pluginGroups[1],
                ],
                [
                        sectionName: 'iOS tasks',
                        section: pluginGroups[2],
                ]
        ]

        pluginsHtml.parentFile.mkdirs()
        pluginsHtml.text = fillTemplate('flow_doc_task_site.template',
                [
                        commonPlugins: commonPlugins.values().collect { [plugin: splitByCharacterTypeCamelCase(it.plugin).join(' ')] },
                        androidPlugins: androidPlugins.values().collect { [plugin: splitByCharacterTypeCamelCase(it.plugin).join(' ')] },
                        iosPlugins: iosPlugins.values().collect { [plugin: splitByCharacterTypeCamelCase(it.plugin).join(' ')] },
                        tasks: pluginSection.collect { fillTemplate('flow_doc_section.template', it) }.join('\n')
                ])
    }

    private String generatePluginDoc(Map plugins) {
        plugins.collect { String id, Map m ->
            fillTemplate('flow_doc_task_group.template',
                    [
                            header: splitByCharacterTypeCamelCase(m.plugin).join(' '),
                            groupName: splitByCharacterTypeCamelCase(m.plugin).join(' '),
                            groupDescription: docText(m.plugin),
                            tasks: m.tasks.collect { t ->
                                [
                                        taskName: t.taskName,
                                        taskDescription: [t.description, docText(t.taskClass)].grep().join('<p><p>')
                                ]
                            }
                    ]
            )
        }.join('\n')
    }

    private void processConfigurations(Map androidJson, Map iosJson) {
        def androidConfs = androidJson.configurations.sort { it.key }
        def iosConfs = iosJson.configurations.sort { it.key }

        def releaseConf = 'Release Configuration'
        def apphanceConf = 'Apphance Configuration'
        def findConfKey = { confs, conf -> confs.find { it.value.conf == conf }.key }

        def commonConfs = [
                '0': androidConfs.remove(findConfKey(androidConfs, releaseConf)),
                '1': androidConfs.remove(findConfKey(androidConfs, apphanceConf)),
        ].sort { it.key }

        commonConfs['0'].confName = commonConfs['0'].conf = releaseConf

        iosConfs.remove(findConfKey(iosConfs, apphanceConf))
        iosConfs.remove(findConfKey(iosConfs, releaseConf))

        def confGroups = [commonConfs, androidConfs, iosConfs].collect(this.&generateConfDoc)

        def confSection = [
                [
                        sectionName: 'Common configurations',
                        section: confGroups[0],
                ],
                [
                        sectionName: 'Android configurations',
                        section: confGroups[1],
                ],
                [
                        sectionName: 'iOS configurations',
                        section: confGroups[2],
                ]
        ]

        confsHtml.parentFile.mkdirs()
        confsHtml.text = fillTemplate('flow_doc_conf_site.template',
                [
                        commonConfs: commonConfs.values().collect { [conf: it.conf] },
                        androidConfs: androidConfs.values().collect { [conf: it.conf] },
                        iosConfs: iosConfs.values().collect { [conf: it.conf] },
                        confs: confSection.collect { fillTemplate('flow_doc_section.template', it) }.join('\n')
                ]
        )
    }

    private String generateConfDoc(Map configurations) {
        configurations.collect { String id, Map conf ->
            fillTemplate('flow_doc_conf_group.template',
                    [
                            confName: conf.conf,
                            confDescription: docText(conf.confClass),
                            confProperties: conf.props.collect {
                                [
                                        name: it.name,
                                        description: it.description
                                ]
                            }
                    ]
            )
        }.join('\n')
    }

    String docText(String entityName) {
        (plugins + tasks + configurations).find { it.name() == entityName }?.commentText()
    }

    private String fillTemplate(String name, Map binding) {
        def tmpl = getClass().getResource(name)
        tmplEngine.createTemplate(tmpl).make(binding)
    }

    public static void main(String[] args) {
        new FlowPluginReference().run()
    }
}
