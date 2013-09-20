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

class FlowPluginReference {

    List<GroovyClassDoc> classes

    FlowPluginReference() {
        File src = new File('src')
        GroovyDocTool htmlTool = new GroovyDocTool(new ClasspathResourceManager(), [src.path] as String[],
                DEFAULT_DOC_TEMPLATES, DEFAULT_PACKAGE_TEMPLATES, DEFAULT_CLASS_TEMPLATES, [], new Properties()
        )
        List<String> groovySources = []
        src.eachFileRecurse(FILES) {
            if (extension(it.name) == 'groovy') groovySources << relativeTo(src, it)
        }

        htmlTool.add(groovySources)
        classes = htmlTool.rootDoc.classes()
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
        classDoc.superclass() && classDoc.name() != 'Object' ?
            [classDoc.superclass().name()] + superClasses(classDoc.superclass()) : []
    }
}
