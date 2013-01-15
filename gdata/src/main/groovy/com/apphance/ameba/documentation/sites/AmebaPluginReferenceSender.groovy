package com.apphance.ameba.documentation.sites

import com.google.gdata.data.HtmlTextConstruct
import com.google.gdata.data.sites.ContentEntry

class AmebaPluginReferenceSender {
    private static File getPluginReferenceFile(String... args) {
        if (args.length < 3) {
            throw new IllegalArgumentException("The third argument of the call should be file name")
        }
        File documentation = new File(args[2])
        if (!documentation.exists()) {
            throw new IllegalStateException("The file ${documentation} does not exist")
        }
        return documentation
    }

    private static uploadPluginReference(ContentEntry page, File documentation) {
        println "Uploading new content"
        page.setContent(new HtmlTextConstruct(documentation.text))
        page.update()
        println "New content uploaded"
    }

    public static void main(String[] args) {
        SitesCommunicator sitesCommunicator = new SitesCommunicator()
        File documentation = getPluginReferenceFile(args)
        sitesCommunicator.readUserPassword(args)
        sitesCommunicator.createService()
        sitesCommunicator.loggingIn()
        ContentEntry page = sitesCommunicator.retrievePluginReferencePage()
        uploadPluginReference(page, documentation)
    }


}
