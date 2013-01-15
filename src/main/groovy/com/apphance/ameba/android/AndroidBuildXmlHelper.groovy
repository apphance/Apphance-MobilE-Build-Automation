package com.apphance.ameba.android

import com.sun.org.apache.xpath.internal.XPathAPI

import javax.xml.parsers.DocumentBuilderFactory

/**
 * Performs various Android manifest XML operations.
 *
 */
class AndroidBuildXmlHelper {
    org.w3c.dom.Element getParsedBuildXml(File projectDirectory) {
        def builderFactory = DocumentBuilderFactory.newInstance()
        builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        builderFactory.setFeature("http://xml.org/sax/features/validation", false)
        def builder = builderFactory.newDocumentBuilder()
        def inputStream = new FileInputStream("${projectDirectory}/build.xml")
        return builder.parse(inputStream).documentElement
    }

    String readProjectName(File projectDirectory) {
        def root = getParsedBuildXml(projectDirectory)
        def project = XPathAPI.selectSingleNode(root, '/project')
        return project.attributes.getNamedItem('name').value
    }

    void replaceProjectName(File projectDirectory, String newProjectName) {
        File file = new File(projectDirectory, 'build.xml')
        def root = getParsedBuildXml(projectDirectory)
        def project = XPathAPI.selectSingleNode(root, '/project')
        project.attributes.nodes.each { attribute ->
            if (attribute.name == 'name') {
                attribute.value = newProjectName
            }
        }
        file.delete()
        file.write(root as String)
    }
}
