package com.apphance.ameba.documentation

import org.junit.Test

import static org.junit.Assert.assertTrue

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
