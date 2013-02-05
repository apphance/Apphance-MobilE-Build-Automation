package com.apphance.ameba.android.plugins.test

import groovy.json.JsonBuilder
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.AuthCache
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.FormBodyPart
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.BasicAuthCache
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.protocol.BasicHttpContext

import java.nio.charset.Charset

import static org.apache.http.client.protocol.ClientContext.AUTH_CACHE
import static org.apache.http.entity.mime.HttpMultipartMode.STRICT

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
                new UsernamePasswordCredentials("${username}", "${pass}")
        )
        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        localcontext = new BasicHttpContext();
        localcontext.setAttribute(AUTH_CACHE, authCache);
    }

    HttpResponse updateArtifactQuery(String apphanceKey, String versionString, long versionNumber, boolean setAsCurrent, ArrayList resourcesToUpdate) {

        HttpPost post = new HttpPost('/api/application.update_version')

        String message = prepareUpdateArtifactJSON(apphanceKey, versionString, versionNumber, setAsCurrent, resourcesToUpdate)
        StringEntity se = new StringEntity(message)
        post.entity = se

        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        post.setHeader("Connection", "close")
        post.setHeader("Host", targetHost.getHostName())

        httpClient.execute(targetHost, post, localcontext)
    }

    String prepareUpdateArtifactJSON(apphanceKey, versionString, versionNumber, setAsCurrent, resourcesToUpdate) {
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
        jsonBuilder.toString()
    }

    HttpResponse uploadResource(File resource, String url, String formBodyPart) {
        HttpPost uploadReq = new HttpPost(url.replace("https://apphance-app.appspot.com", ""))

        def boundary = "----------------------------90505c6cdd54"

        MultipartEntity reqEntity = new MultipartEntity(STRICT, boundary, Charset.forName("UTF-8"));
        reqEntity.addPart(new FormBodyPart(formBodyPart, new FileBody(resource, "application/octet-stream")))
        uploadReq.setEntity(reqEntity)

        uploadReq.setHeader("Content-type", "multipart/form-data; boundary=" + boundary)
        uploadReq.setHeader("Accept", "*/*")
        uploadReq.setHeader("Connection", "close")
        uploadReq.setHeader("Host", targetHost.getHostName())

        httpClient.execute(targetHost, uploadReq, localcontext)
    }

    void closeConnection() {
        httpClient.getConnectionManager().shutdown();
    }
}
