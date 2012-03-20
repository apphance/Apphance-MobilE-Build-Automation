package com.apphance.ameba.documentation;

import static org.junit.Assert.*;
import com.apphance.ameba.documentation.AmebaPluginReferenceBuilder

import org.junit.Test;

class TestDocumentation {
    @Test
    public void testGenerateDocumentation() throws Exception {
        File pluginsReference = new File("tmp/plugins_reference.html")
        AmebaPluginReferenceBuilder documentationBuilder = new AmebaPluginReferenceBuilder()
        pluginsReference.delete()
        documentationBuilder.buildDocumentation()
        assertTrue(pluginsReference.exists())
    }
}
