package com.apphance.flow.plugins.apphance

import com.apphance.flow.util.Preconditions
import groovy.json.JsonOutput
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
import org.apache.http.util.EntityUtils
import org.gradle.api.GradleException
import org.gradle.api.logging.Logging

import java.nio.charset.Charset

import static org.apache.http.client.protocol.ClientContext.AUTH_CACHE
import static org.apache.http.entity.mime.HttpMultipartMode.STRICT

@Mixin(Preconditions)
 class ApphanceNetworkHelper {

    def static logger = Logging.getLogger(this.class)

    private String username
    private String pass
    private HttpHost targetHost
    private DefaultHttpClient httpClient
    private BasicHttpContext localContext

    ApphanceNetworkHelper(String user, String pass) {
        this.username = user
        this.pass = pass
        this.targetHost = new HttpHost('apphance-app.appspot.com', 443, 'https')
        this.httpClient = new DefaultHttpClient()
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(user, pass)
        )
        AuthCache authCache = new BasicAuthCache()
        authCache.put(targetHost, new BasicScheme())

        this.localContext = new BasicHttpContext()
        localContext.setAttribute(AUTH_CACHE, authCache)
    }

    HttpResponse updateArtifactQuery(String apphanceKey, String versionCode, String versionNumber, boolean setAsCurrent, List resourcesToUpdate) {
        logger.lifecycle "Updating arfifact. Version string: $versionCode, version code: $versionCode"
        def response = callApphanceApi('application.update_version', updateArtifactsJSONQuery(apphanceKey, versionCode, versionNumber, setAsCurrent,
                resourcesToUpdate))
        logger.lifecycle "Upload version query response: ${response.statusLine}"
        throwIfConditionTrue !response.entity, "Error while uploading version query, empty response received"
        response
    }

    HttpResponse callApphanceApi(String endpoint, String content) {
        def post = new HttpPost("/api/$endpoint")
        post.entity = new StringEntity(content)
        post.setHeader 'Accept', 'application/json'
        post.setHeader 'Content-type', 'application/json'
        post.setHeader 'Connection', 'close'
        post.setHeader 'Host', targetHost.getHostName()

        httpClient.execute(targetHost, post, localContext)
    }

    private String updateArtifactsJSONQuery(apphanceKey, versionString, versionCode, setAsCurrent, resourcesToUpdate) {
        JsonOutput.toJson([
                api_key: apphanceKey,
                version: [
                        name: versionString,
                        number: versionCode.toInteger()
                ],
                current: setAsCurrent,
                update_resources: resourcesToUpdate
        ])
    }

    HttpResponse uploadResource(File resource, String url, String formBodyPart) {
        logger.lifecycle "Updating arfifact  $resource.absolutePath"
        HttpPost uploadReq = new HttpPost(url.replace('https://apphance-app.appspot.com', ''))

        def boundary = '----------------------------90505c6cdd54'

        def reqEntity = new MultipartEntity(STRICT, boundary, Charset.forName('UTF-8'))
        reqEntity.addPart(new FormBodyPart(formBodyPart, new FileBody(resource, 'application/octet-stream')))
        uploadReq.setEntity(reqEntity)

        uploadReq.setHeader('Content-type', "multipart/form-data; boundary=$boundary")
        uploadReq.setHeader('Accept', '*/*')
        uploadReq.setHeader('Connection', 'close')
        uploadReq.setHeader('Host', targetHost.getHostName())

        def response = httpClient.execute(targetHost, uploadReq, localContext)
        logger.lifecycle "Upload apk response: ${response.statusLine}"
        response
    }

    String updateArtifactJson(String apphanceKey, String versionString, String versionCode, boolean setAsCurrent, List resourcesToUpdate) {
        toJson {
            updateArtifactQuery(apphanceKey, versionString, versionCode, setAsCurrent, resourcesToUpdate)
        }
    }

    String uploadResourceJson(File resource, String url, String formBodyPart) {
        toJson {
            uploadResource(builderInfo.originalFile, resp.update_urls.apk, 'apk')
        }
    }

    static String toJson(Closure<HttpResponse> action) {
        HttpResponse response = action()
        String json = response.entity.content.text
        EntityUtils.consume(response.entity)
        logger.info "Full response: $json"
        json
    }

    void close() {
        httpClient.getConnectionManager().shutdown()
    }

    void call(Closure action) {
        try {
            action(this)
        } catch (e) {
            def msg = "Error while calling apphance api: ${e.message}"
            logger.error(msg)
            throw new GradleException(msg, e)
        } finally {
            this?.close()
        }
    }
}
