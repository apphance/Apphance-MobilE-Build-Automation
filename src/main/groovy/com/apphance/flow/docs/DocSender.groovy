package com.apphance.flow.docs

import com.google.gdata.client.sites.SitesService
import com.google.gdata.data.TextContent
import com.google.gdata.data.XhtmlTextConstruct
import com.google.gdata.data.sites.ContentEntry
import com.google.gdata.util.XmlBlob
import org.gradle.api.GradleException

import static org.gradle.api.logging.Logging.getLogger

class DocSender {

    def static logger = getLogger(DocGenerator)

    String user
    String pass
    SitesService client

    public static void main(String[] args) {
        logger.lifecycle "Sending flow documentation to flow.apphance.com"
        new DocSender(System.getProperty('site.user'), System.getProperty('site.pass')).send()
    }

    DocSender(String user, String pass) {
        this.user = user
        this.pass = pass

        if (!user || !pass) {
            throw new GradleException("'site.user' and 'site.pass' properties should be provided ")
        }

        client = new SitesService('polidea-flow')
        client.useSsl()
        client.setUserCredentials(user, pass)
    }

    public void send() {
        String confSite = 'https://sites.google.com/feeds/content/apphance.com/mobile-build-automation/1242463515220240138'
        File confFile = new File('build/doc/confs.html')

        String taskSite = 'https://sites.google.com/feeds/content/apphance.com/mobile-build-automation/1696209147441457392'
        File taskFile = new File('build/doc/plugins.html')

        sendFileToSite(confFile, confSite, 'configuration reference')
        sendFileToSite(taskFile, taskSite, 'plugin/tasks reference')
    }

    private void sendFileToSite(File file, String site, String message) {
        def contentEntry = client.getEntry(new URL(site), ContentEntry.class)
        def textContent = contentEntry.content as TextContent

        if (file.exists() && file.size() > 0) {
            logger.lifecycle "Sending new $message"

            XmlBlob xml = new XmlBlob()
            xml.setBlob(file.text)
            textContent.setContent(new XhtmlTextConstruct(xml))
            contentEntry.update()
        } else {
            logger.error "No such file: $file.absolutePath"
        }
    }
}
