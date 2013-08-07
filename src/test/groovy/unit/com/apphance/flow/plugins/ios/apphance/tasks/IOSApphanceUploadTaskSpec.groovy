package com.apphance.flow.plugins.ios.apphance.tasks

import com.apphance.flow.configuration.apphance.ApphanceConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSVariant
import com.apphance.flow.configuration.properties.StringProperty
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.apphance.ApphanceNetworkHelper
import com.apphance.flow.plugins.release.FlowArtifact
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.StatusLine
import org.apache.http.entity.StringEntity
import org.gradle.api.GradleException
import spock.lang.Specification

import static org.gradle.testfixtures.ProjectBuilder.builder

class IOSApphanceUploadTaskSpec extends Specification {

    def project = builder().build()
    def task = project.task('UploadTask', type: IOSApphanceUploadTask) as IOSApphanceUploadTask

    def 'exception is thrown when user, pass or key empty'() {
        given:
        task.variant = GroovyMock(IOSVariant) {
            getApphanceAppKey() >> new StringProperty(value: null)
            getName() >> 'Variant1'
        }
        task.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getUser() >> new StringProperty(value: user)
            getPass() >> new StringProperty(value: pass)
        }
        task.reader = GroovyMock(PropertyReader) {
            systemProperty('apphance.user') >> user
            systemProperty('apphance.pass') >> pass
            envVariable('APPHANCE_USER') >> user
            envVariable('APPHANCE_PASS') >> pass
        }

        when:
        task.upload()

        then:
        def e = thrown(GradleException)
        e.message == message

        where:
        user   | pass   | message
        null   | null   | "Impossible to find user name for apphance.com! Define it in flow.properties configuration file or 'apphance.user' system property or 'APPHANCE_USER' environment variable!"
        'user' | null   | "Impossible to find password for apphance.com! Define it in flow.properties configuration file or 'apphance.pass' system property or 'APPHANCE_PASS' environment variable!"
        'user' | 'pass' | 'Impossible to find apphance key for variant: Variant1. Define it in appropriate section of flow.properties file!'
    }

    def 'artifacts are uploaded'() {
        given:
        def variant = GroovyMock(IOSVariant) {
            getApphanceAppKey() >> new StringProperty(value: '3145')
            getVersionCode() >> '3145'
            getVersionString() >> '31.4.5'
            getName() >> 'Variant1'
        }
        task.variant = variant
        task.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getUser() >> new StringProperty(value: 'user')
            getPass() >> new StringProperty(value: 'pass')
        }
        task.networkHelper = GroovyMock(ApphanceNetworkHelper) {
            updateArtifactQuery(variant.apphanceAppKey.value, variant.versionString, variant.versionCode, false, ['ipa', 'dsym']) >> GroovyMock(HttpResponse) {
                getEntity() >> new StringEntity('{\n' +
                        '    "status": "OK",\n' +
                        '    "update_urls": {\n' +
                        '        "dsym": "https://apphance-app.appspot.com/_ah/upload/hash1/ALBNUaYAAAAAUcg0R43P3RSCB5cITrM_cgk5mzsdWFlE/",\n' +
                        '        "ipa": "https://apphance-app.appspot.com/_ah/upload/hash2/ALBNUaYAAAAAUcg0R_saODaHQF9t8jeMXB1RV0zvm-EI/",\n' +
                        '    }\n' +
                        '}')
            }
            uploadResource(_, "https://apphance-app.appspot.com/_ah/upload/hash2/ALBNUaYAAAAAUcg0R_saODaHQF9t8jeMXB1RV0zvm-EI/", 'ipa') >> GroovyMock(HttpResponse) {
                getStatusLine() >> GroovyMock(StatusLine) {
                    getStatusCode() >> 200
                }
            }
            uploadResource(_, "https://apphance-app.appspot.com/_ah/upload/hash1/ALBNUaYAAAAAUcg0R43P3RSCB5cITrM_cgk5mzsdWFlE/", 'dsym') >> GroovyMock(HttpResponse) {
                getStatusLine() >> GroovyMock(StatusLine) {
                    getStatusCode() >> 200
                }
            }
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            getIpaFiles() >> ['Variant1': new FlowArtifact(location: GroovyMock(File))]
            getAhSYMDirs() >> ['Variant1': new FlowArtifact(location: GroovyMock(File) {
                listFiles(_) >> [GroovyMock(File) {
                    exists() >> true
                    isFile() >> true
                    getName() >> 'sample.ahsym'
                }]
            })]
        }

        when:
        task.upload()

        then:
        noExceptionThrown()
    }

    def 'error handled while ipa uploading'() {
        given:
        def variant = GroovyMock(IOSVariant) {
            getApphanceAppKey() >> new StringProperty(value: '3145')
            getVersionCode() >> '3145'
            getVersionString() >> '31.4.5'
            getName() >> 'Variant1'
        }
        task.variant = variant
        task.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getUser() >> new StringProperty(value: 'user')
            getPass() >> new StringProperty(value: 'pass')
        }
        task.networkHelper = GroovyMock(ApphanceNetworkHelper) {
            updateArtifactQuery(variant.apphanceAppKey.value, variant.versionString, variant.versionCode, false, ['ipa', 'dsym']) >> GroovyMock(HttpResponse) {
                getEntity() >> new StringEntity('{\n' +
                        '    "status": "OK",\n' +
                        '    "update_urls": {\n' +
                        '        "dsym": "https://apphance-app.appspot.com/_ah/upload/hash1/ALBNUaYAAAAAUcg0R43P3RSCB5cITrM_cgk5mzsdWFlE/",\n' +
                        '        "ipa": "https://apphance-app.appspot.com/_ah/upload/hash2/ALBNUaYAAAAAUcg0R_saODaHQF9t8jeMXB1RV0zvm-EI/",\n' +
                        '    }\n' +
                        '}')
            }
            uploadResource(_, "https://apphance-app.appspot.com/_ah/upload/hash2/ALBNUaYAAAAAUcg0R_saODaHQF9t8jeMXB1RV0zvm-EI/", 'ipa') >> GroovyMock(HttpResponse) {
                getStatusLine() >> GroovyMock(StatusLine) {
                    getStatusCode() >> 500
                }
                getEntity() >> GroovyMock(HttpEntity)
            }
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            getIpaFiles() >> ['Variant1': new FlowArtifact(location: GroovyMock(File))]
        }

        when:
        task.upload()

        then:
        def e = thrown(GradleException)
        e.message == 'Error while uploading iOS artifacts to apphance. Error while uploading ipa: null. Response: null'
    }

    def 'error handled while ahSYM uploaded'() {
        given:
        def variant = GroovyMock(IOSVariant) {
            getApphanceAppKey() >> new StringProperty(value: '3145')
            getVersionCode() >> '3145'
            getVersionString() >> '31.4.5'
            getName() >> 'Variant1'
        }
        task.variant = variant
        task.apphanceConf = GroovyMock(ApphanceConfiguration) {
            getUser() >> new StringProperty(value: 'user')
            getPass() >> new StringProperty(value: 'pass')
        }
        task.networkHelper = GroovyMock(ApphanceNetworkHelper) {
            updateArtifactQuery(variant.apphanceAppKey.value, variant.versionString, variant.versionCode, false, ['ipa', 'dsym']) >> GroovyMock(HttpResponse) {
                getEntity() >> new StringEntity('{\n' +
                        '    "status": "OK",\n' +
                        '    "update_urls": {\n' +
                        '        "dsym": "https://apphance-app.appspot.com/_ah/upload/hash1/ALBNUaYAAAAAUcg0R43P3RSCB5cITrM_cgk5mzsdWFlE/",\n' +
                        '        "ipa": "https://apphance-app.appspot.com/_ah/upload/hash2/ALBNUaYAAAAAUcg0R_saODaHQF9t8jeMXB1RV0zvm-EI/",\n' +
                        '    }\n' +
                        '}')
            }
            uploadResource(_, "https://apphance-app.appspot.com/_ah/upload/hash2/ALBNUaYAAAAAUcg0R_saODaHQF9t8jeMXB1RV0zvm-EI/", 'ipa') >> GroovyMock(HttpResponse) {
                getStatusLine() >> GroovyMock(StatusLine) {
                    getStatusCode() >> 200
                }
            }
            uploadResource(_, "https://apphance-app.appspot.com/_ah/upload/hash1/ALBNUaYAAAAAUcg0R43P3RSCB5cITrM_cgk5mzsdWFlE/", 'dsym') >> GroovyMock(HttpResponse) {
                getStatusLine() >> GroovyMock(StatusLine) {
                    getStatusCode() >> 500
                }
                getEntity() >> GroovyMock(HttpEntity)
            }
        }
        task.releaseConf = GroovyMock(IOSReleaseConfiguration) {
            getIpaFiles() >> ['Variant1': new FlowArtifact(location: GroovyMock(File))]
            getAhSYMDirs() >> ['Variant1': new FlowArtifact(location: GroovyMock(File) {
                listFiles(_) >> [GroovyMock(File) {
                    exists() >> true
                    isFile() >> true
                    getName() >> 'sample.ahsym'
                }]
            })]
        }

        when:
        task.upload()

        then:
        def e = thrown(GradleException)
        e.message == 'Error while uploading iOS artifacts to apphance. Error while uploading ahsym: null. Response: null'
    }
}
