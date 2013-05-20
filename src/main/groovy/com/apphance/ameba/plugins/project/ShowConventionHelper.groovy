package com.apphance.ameba.plugins.project

import org.gradle.api.internal.DynamicObjectHelper
import org.gradle.api.tasks.SourceSet

/**
 * Shows all conventions.
 *
 */
class ShowConventionHelper {

    private static final int INDENT = 4

    void getIterableObject(StringBuilder sb, Object obj, int indent) {
        sb << '[\n'
        def newIndent = indent + INDENT
        obj.each { it ->
            sb << " " * newIndent
            getObjectRepr(sb, it, newIndent)
            sb << ",\n"
        }
        sb << " " * indent + ']'
    }

    void getObjectRepr(StringBuilder sb, Object obj, int indent) {
        if (obj == null || obj.class == null || obj.class == java.lang.Package.class) {
            sb << null
        } else if (obj.class == String.class || obj.class == GString.class || obj.class == File.class) {
            sb << "\"${obj}\""
        } else if (obj.class == DynamicObjectHelper.class) {
            sb << obj
        } else if (obj.class in [Integer.class, Long.class, Double.class, Float.class, Short.class, Byte.class]) {
            sb << obj
        } else if (obj instanceof Iterable || obj.class.isArray()) {
            getIterableObject(sb, obj, indent)
        } else if (obj instanceof SourceSet) {
            sb << '<' << obj << '>'
        } else {
            sb << obj
        }
    }

    void getObjectProperties(StringBuilder sb, Object obj, int indent) {
        def newIndent = indent + INDENT
        sb << '{\n'
        obj.getProperties().each { propertyName, propertyValue ->
            if (!(propertyName in ['class', 'metaClass'])) {
                sb << " " * newIndent << "${propertyName} = "
                getObjectRepr(sb, propertyValue, newIndent)
                sb << '\n'
            }
        }
        sb << " " * indent + '}'
    }

    void getConventionObject(StringBuilder sb, String pluginName, Object pluginConventionObject) {
        def clazz = pluginConventionObject.class
        sb << "// Conventions for ${pluginName} plugin" << '\n'
        try {
            def conventionDescription = clazz.getField('DESCRIPTION').get(null).split('\n')
            conventionDescription.each {
                sb << "// ${it}" << '\n'
            }
        } catch (NoSuchFieldException e) {
            // skip description ....
        }
        sb << "${pluginName} "
        getObjectProperties(sb, pluginConventionObject, 0)
        sb << '\n\n'
    }

}
