package com.apphance.flow.docs

import com.apphance.flow.configuration.AbstractConfiguration
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

class FlowPluginReference {

    def static logger = getLogger(FlowPluginReference)

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

    List<GroovyClassDoc> getPlugins() {
        classes.findAll { it.interfaces()*.name().contains Plugin.simpleName }
    }

    List<GroovyClassDoc> getConfigurations() {
        classes.findAll { superClasses(it).contains AbstractConfiguration.simpleName }
    }

    List<GroovyClassDoc> getTasks() {
        classes.findAll { superClasses(it).contains AbstractTask.simpleName }
    }

    List<String> superClasses(GroovyClassDoc classDoc) {
        def superclass = classDoc.superclass()
        superclass && classDoc.name() != 'Object' ? [superclass.name()] + superClasses(superclass) : []
    }

    public static void main(String[] args) {
        logger.lifecycle "Building Apphance Flow plugin reference"

        def reference = new FlowPluginReference()
        //TODO: generate documentation using groovydoc taken from reference and html templates
    }
}
