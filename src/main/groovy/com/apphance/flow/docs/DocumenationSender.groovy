package com.apphance.flow.docs

import com.google.gdata.client.sites.SitesService
import com.google.gdata.data.TextContent
import com.google.gdata.data.XhtmlTextConstruct
import com.google.gdata.data.sites.ContentEntry
import com.google.gdata.data.sites.ContentFeed
import com.google.gdata.util.XmlBlob

import static org.gradle.api.logging.Logging.getLogger

class DocumenationSender {

    def static logger = getLogger(FlowPluginReference)

    public static void main(String[] args) {
        logger.lifecycle "Sending flow documentation to flow.apphance.com"

        //TODO use token for authentication
        //TODO configure site url, and html content
    }

    public void send(String user, String pass, String htmlContent, String site) {
        def client = new SitesService('polidea-flow')
        client.useSsl()
        client.setUserCredentials(user, pass)

        def contentFeed = client.getFeed(new URL(site), ContentFeed.class);
        def contentEntry = contentFeed.entries[0] as ContentEntry
        def textContent = contentEntry.content as TextContent

        XmlBlob xml = new XmlBlob();
        xml.setBlob(htmlContent);
        textContent.setContent(new XhtmlTextConstruct(xml))
        contentEntry.update()
    }
}
