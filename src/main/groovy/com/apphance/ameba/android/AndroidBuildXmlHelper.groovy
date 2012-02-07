package com.apphance.ameba.android

import java.io.File

import javax.xml.parsers.DocumentBuilderFactory

import com.sun.org.apache.xpath.internal.XPathAPI


class AndroidBuildXmlHelper {
    org.w3c.dom.Element getParsedBuildXml(File projectDirectory) {
        def builder     = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        def inputStream = new FileInputStream("${projectDirectory}/build.xml")
        return builder.parse(inputStream).documentElement
    }

    String readProjectName(File projectDirectory) {
        def root = getParsedBuildXml(projectDirectory)
        def project = XPathAPI.selectSingleNode(root,'/project')
        return project.attributes.getNamedItem('name').value
    }

    void replaceProjectName(File projectDirectory, String newProjectName) {
        File file= new File(projectDirectory, 'build.xml')
        def root = getParsedBuildXml(projectDirectory)
        def project = XPathAPI.selectSingleNode(root,'/project')
        project.attributes.nodes.each { attribute ->
            if (attribute.name == 'name') {
                attribute.value = newProjectName
            }
        }
        file.delete()
        file.write(root as String)
    }
}
