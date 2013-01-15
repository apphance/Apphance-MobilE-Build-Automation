package com.apphance.ameba.android.plugins.test

import groovy.json.JsonBuilder
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.ClientContext
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.BasicHttpContext

import java.nio.charset.Charset

class ApphanceNetworkHelper {

    String username
    String pass
    DefaultHttpClient httpClient
    HttpHost targetHost
    BasicHttpContext localcontext

    public ApphanceNetworkHelper(String username, String pass) {
        this.username = username
        this.pass = pass
        targetHost = new HttpHost("apphance-app.appspot.com", 443, "https");
        httpClient = new DefaultHttpClient()
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials("${username}", "${pass}"))
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local
        // auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
    }

    HttpResponse sendUpdateVersion(String apphanceKey, String versionString, long versionNumber, boolean setAsCurrent, ArrayList resourcesToUpdate) {
        HttpPost post = new HttpPost('/api/application.update_version')
        def jsonBuilder = new JsonBuilder(
                [
                        api_key: apphanceKey,
                        version: [
                                name: versionString,
                                number: versionNumber
                        ],
                        current: setAsCurrent,
                        update_resources: resourcesToUpdate
                ]
        )
        StringEntity se = new StringEntity(jsonBuilder.toString());
        post.setEntity(se);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Connection", "close")
        post.setHeader("Host", targetHost.getHostName())

        return httpClient.execute(targetHost, post, localcontext)
    }

    HttpResponse uploadResource(File resource, String url) {
        def boundary = "----------------------------90505c6cdd54"
        MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT, boundary, Charset.forName("UTF-8"));
        reqEntity.addPart(new FormBodyPart("apk", new FileBody(resource, "application/octet-stream")))
        HttpPost uploadReq = new HttpPost(url.replace("https://apphance-app.appspot.com", ""))
        uploadReq.setHeader("Content-type", "multipart/form-data; boundary=" + boundary)
        uploadReq.setHeader("Accept", "*/*")
        uploadReq.setHeader("Connection", "close")
        uploadReq.setHeader("Host", targetHost.getHostName())
        uploadReq.setEntity(reqEntity)
        return httpClient.execute(targetHost, uploadReq, localcontext)
    }

    void closeConnection() {
        httpClient.getConnectionManager().shutdown();
    }
}
