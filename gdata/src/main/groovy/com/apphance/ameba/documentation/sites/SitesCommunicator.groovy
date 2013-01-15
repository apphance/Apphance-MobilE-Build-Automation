package com.apphance.ameba.documentation.sites

import com.google.gdata.client.sites.ContentQuery
import com.google.gdata.client.sites.SitesService
import com.google.gdata.data.ILink.Type
import com.google.gdata.data.PlainTextConstruct
import com.google.gdata.data.media.MediaFileSource
import com.google.gdata.data.sites.AttachmentEntry
import com.google.gdata.data.sites.ContentEntry
import com.google.gdata.data.sites.ContentFeed
import com.google.gdata.data.sites.SitesLink
import com.google.gdata.util.ServiceException

import javax.activation.MimetypesFileTypeMap

class SitesCommunicator {
    public static final String APP_NAME = 'apphance-ameba-v0.99.3'
    public static final String CONTENT_URL = 'https://sites.google.com/feeds/content/apphance.com/mobile-build-automation/'

    String username = null
    String password = null
    SitesService service = null

    public readUserPassword(String... args) {
        username = args[0]
        println "Username: ${username}"
        password = args[1]
    }

    public createService() {
        service = new SitesService(APP_NAME)
        service.useSsl()
    }

    public loggingIn() {
        println "Logging in to Ameba's sites"
        service.setUserCredentials(username, password)
    }

    public ContentEntry retrievePluginReferencePage() {
        println "Retrieving plugin reference page"
        ContentQuery query = new ContentQuery(new URL(CONTENT_URL));
        query.setPath('/introduction/plugin-reference/')
        ContentFeed contentFeed = service.getFeed(query, ContentFeed.class)
        def entries = contentFeed.getEntries()
        ContentEntry page = entries.get(0)
        return page
    }

    public ContentEntry retrieveDownloadPage() {
        println "Retrieving download page"
        ContentQuery query = new ContentQuery(new URL(CONTENT_URL));
        query.setKind('filecabinet')
        ContentFeed contentFeed = service.getFeed(query, ContentFeed.class);
        def entries = contentFeed.getEntries()
        entries.each {
            println it.title.text
        }
        ContentEntry page = entries.get(0);
        return page
    }

    public List<ContentEntry> retrieveAllPagesIndex() {
        println "Retrieving all pages index"
        ContentQuery query = new ContentQuery(new URL(CONTENT_URL));
        query.setMaxResults(200)
        query.setKind("webpage")
        ContentFeed contentFeed = service.getFeed(query, ContentFeed.class);
        List<ContentEntry> entries = contentFeed.getEntries().findAll { true }
        return entries
    }

    public AttachmentEntry uploadAttachment(File file, ContentEntry parentPage,
                                            String title, String description) throws IOException, ServiceException {
        println "Uploading attachment"
        MimetypesFileTypeMap mediaTypes = new MimetypesFileTypeMap();
        mediaTypes.addMimeTypes("application/zip zip");
        AttachmentEntry newAttachment = new AttachmentEntry();
        newAttachment.setMediaSource(new MediaFileSource(file, mediaTypes.getContentType(file)));
        newAttachment.setTitle(new PlainTextConstruct(title));
        newAttachment.setSummary(new PlainTextConstruct(description));
        newAttachment.addLink(SitesLink.Rel.PARENT, Type.ATOM, parentPage.getSelfLink().getHref());
        return service.insert(new URL(CONTENT_URL), newAttachment);
    }

}
