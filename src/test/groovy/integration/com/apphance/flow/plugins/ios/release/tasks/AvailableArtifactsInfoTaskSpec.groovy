package com.apphance.flow.plugins.ios.release.tasks

import com.apphance.flow.configuration.ios.IOSConfiguration
import com.apphance.flow.configuration.ios.IOSReleaseConfiguration
import com.apphance.flow.configuration.ios.variants.IOSSchemeVariant
import com.apphance.flow.configuration.ios.variants.IOSVariantsConfiguration
import com.apphance.flow.configuration.properties.*
import com.apphance.flow.configuration.reader.PropertyReader
import com.apphance.flow.plugins.ios.builder.IOSArtifactProvider
import com.apphance.flow.plugins.ios.parsers.MobileProvisionParser
import com.apphance.flow.plugins.release.FlowArtifact
import org.gradle.api.Project
import spock.lang.Specification

import static com.apphance.flow.configuration.ProjectConfiguration.TMP_DIR
import static com.apphance.flow.configuration.ios.IOSBuildMode.DEVICE
import static com.apphance.flow.configuration.release.ReleaseConfiguration.ALL_EMAIL_FLAGS
import static com.apphance.flow.configuration.release.ReleaseConfiguration.OTA_DIR
import static com.apphance.flow.configuration.release.ReleaseConfiguration.getOTA_DIR
import static com.google.common.io.Files.createTempDir
import static org.gradle.testfixtures.ProjectBuilder.builder

class AvailableArtifactsInfoTaskSpec extends Specification {

    def rootDir = createTempDir()
    def tmpDir = createTempDir()

    def p = builder().withProjectDir(new File('testProjects/ios/GradleXCode')).build()

    def projectName = 'GradleXCode'
    def projectUrl = "http://ota.polidea.pl/$projectName".toURL()
    def fullVersionString = '1.0_32'

    def otaFolderPrefix
    def releaseConf = new IOSReleaseConfiguration()
    def variantsConf

    def task = p.task(AvailableArtifactsInfoTask.NAME, type: AvailableArtifactsInfoTask) as AvailableArtifactsInfoTask

    def setup() {

        def reader = GroovyStub(PropertyReader) {
            systemProperty('version.code') >> '32'
            systemProperty('version.string') >> '1.0'
            envVariable('RELEASE_NOTES') >> 'release\nnotes'
        }

        def conf = GroovySpy(IOSConfiguration) {
            getFullVersionString() >> fullVersionString
            getProjectName() >> new StringProperty(value: projectName)
            getVersionString() >> '1.0'
        }
        conf.project = GroovyStub(Project) {
            getRootDir() >> rootDir
            file(TMP_DIR) >> tmpDir
        }
        conf.reader = reader

        releaseConf.conf = conf
        releaseConf.projectURL = new URLProperty(value: projectUrl)
        releaseConf.iconFile = new FileProperty(value: 'icon_retina.png')
        releaseConf.releaseMailFlags = new ListStringProperty(value: ALL_EMAIL_FLAGS)
        releaseConf.manifestFiles = [
                'MainVariant': new FlowArtifact(url: "http://ota.polidea.pl/GradleXCode/MainVariant".toURL()),
                'Variant2': new FlowArtifact(url: "http://ota.polidea.pl/GradleXCode/Variant2".toURL())
        ]
        releaseConf.reader = reader

        def variants = [
                GroovyMock(IOSSchemeVariant) {
                    getTarget() >> 'GradleXCode'
                    getConfiguration() >> 'MainConf'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    getMobileprovision() >> new FileProperty(value: 'sample.mobileprovision')
                    getFullVersionString() >> fullVersionString
                    getName() >> 'MainVariant'
                },
                GroovyMock(IOSSchemeVariant) {
                    getTarget() >> 'GradleXCode2'
                    getConfiguration() >> 'Conf2'
                    getMode() >> new IOSBuildModeProperty(value: DEVICE)
                    getMobileprovision() >> new FileProperty(value: 'sample2.mobileprovision')
                    getFullVersionString() >> fullVersionString
                    getName() >> 'Variant2'
                }
        ]

        variantsConf = GroovyMock(IOSVariantsConfiguration)
        variantsConf.variants >> variants
        variantsConf.mainVariant >> variants[0]
        conf.variantsConf = variantsConf

        otaFolderPrefix = "${releaseConf.projectDirName}/${conf.fullVersionString}"

        def artifactProvider = new IOSArtifactProvider(releaseConf: releaseConf, variantsConf: variantsConf)

        task.mpParser = GroovyMock(MobileProvisionParser) {
            udids(_) >> ['a', 'b']
        }
        task.conf = conf
        task.releaseConf = releaseConf
        task.variantsConf = variantsConf
        task.artifactProvider = artifactProvider
    }

    def cleanup() {
        rootDir.deleteDir()
        tmpDir.deleteDir()
    }

    def 'task action is executed and all artifacts are prepared'() {
        when:
        task.availableArtifactsInfo()

        then:
        def releaseDir = new File(rootDir.absolutePath, "${OTA_DIR}/$projectName/$fullVersionString")
        releaseDir.exists()
        releaseDir.isDirectory()
        [
                'index.html',
                'icon_retina.png',
                'file_index.html',
                'plain_file_index.html',
                'message_file.html',
                "qrcode-$projectName-${fullVersionString}.png"
        ].every {
            def f = new File(releaseDir, it)
            f.exists() && f.isFile() && f.size() > 0
        }
    }

    def 'index.html is generated and validated'() {
        when:
        task.otaIndexFileArtifact(otaFolderPrefix)
        task.prepareOTAIndexFile()

        then:
        !releaseConf.otaIndexFile.location.text.contains('null')
        def slurper = new XmlSlurper().parse(releaseConf.otaIndexFile.location)
        slurper.head.title.text() == "$projectName - iOS"
        slurper.body.div[0].div[0].h1.text() == 'OTA installation - iOS'
        slurper.body.div[0].div[1].div[0].ul.li.img.@src.text() == 'icon_retina.png'
        slurper.body.div[0].div[1].div.ul.li[1].text().trim().startsWith('Version: 1.0')
        slurper.body.div[0].div[1].div[1].section.header.h3.div.text() == 'Main installation'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[0].text() == 'MainVariant'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[1].text().trim() == 'Install'
        slurper.body.div[0].div[1].div[2].ul.li.div.div[1].a.@href.text() ==
                'itms-services://?action=download-manifest&url=http%3A%2F%2Fota.polidea.pl%2FGradleXCode%2FMainVariant'
        slurper.body.div[0].div[1].ul.text().contains('release')
        slurper.body.div[0].div[1].ul.text().contains('notes')
        slurper.body.div[0].div[1].div[3].text() == 'Other installations'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[0].text() == 'Variant2'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[1].text().trim() == 'Install'
        slurper.body.div[0].div[1].div[4].ul.li.div[0].div[1].a.@href.text() ==
                'itms-services://?action=download-manifest&url=http%3A%2F%2Fota.polidea.pl%2FGradleXCode%2FVariant2'
    }

    def 'file_index.html is generated and validated'() {
        given:
        releaseConf.QRCodeFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'qr.png'
        }
        releaseConf.mailMessageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'mail_message'
        }
        releaseConf.imageMontageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'image_montage.png'
        }
        releaseConf.plainFileIndexFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'plain_file_index.html'
        }
        releaseConf.distributionZipFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.dist.zip'
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.dist.zip'
                }
        ]
        releaseConf.dSYMZipFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.dsym.zip'
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.dsym.zip'
                }
        ]
        releaseConf.ipaFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.ipa'
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.ipa'
                }
        ]
        releaseConf.manifestFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.manifest'
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.manifest'
                }
        ]
        releaseConf.mobileProvisionFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.mobileprovision'
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.mobileprovision'
                }
        ]
        releaseConf.ahSYMDirs = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.ahsym'
                    getChildArtifacts() >> [GroovyMock(FlowArtifact) {
                        getName() >> 'ahSym1'
                        getRelativeUrl(_) >> 'ahSym1'
                    }]
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.ahsym'
                    getChildArtifacts() >> [GroovyMock(FlowArtifact) {
                        getName() >> 'ahSym2'
                        getRelativeUrl(_) >> 'ahSym1'
                    }]
                }
        ]

        when:
        task.fileIndexArtifact(otaFolderPrefix)
        task.prepareFileIndexFile(['MainVariant': ['a', 'b'], 'Variant2': ['c', 'd']])

        then:
        !releaseConf.fileIndexFile.location.text.contains('null')
        def slurper = new XmlSlurper().parse(releaseConf.fileIndexFile.location)
        slurper.body.div[0].div[0].h1.text() == 'Files to download'
        slurper.body.div[0].div[1].div[0].section.header.h3.text() == projectName
        slurper.body.div[0].div[1].div[0].section.header.div.text().trim().startsWith('Version: 1.0')
        slurper.body.div[0].div[1].div[0].section.p.ul.li*.text().containsAll(variantsConf.variants*.name)

        slurper.body.div[0].div[1].div[1].section.p.ul.li*.text().containsAll(
                ['Mail message', 'Image montage file', 'QR Code', 'Plain file index', 'Valid UDIDs']
        )
        slurper.body.div[0].div[1].div[1].section.p.ul.li.a.@href*.text().containsAll(
                ['mail_message', 'image_montage.png', 'qr.png', 'plain_file_index.html', '#provprofiles']
        )

        slurper.body.div[1].div[0].text() == 'MainVariant'
        slurper.body.div[1].div[1].section.@id.text() == 'MainConf'
        slurper.body.div[1].div[1].section.header.h3.text() == 'Configuration: MainConf'
        slurper.body.div[1].div[1].section.p.ul.li.a.@href*.text().containsAll(
                ['MainVariant.dist.zip', 'MainVariant.dsym.zip', 'MainVariant.ipa', 'MainVariant.manifest', 'MainVariant.mobileprovision', '#MainVariant-ahSYM']
        )

        slurper.body.div[2].div[0].text() == 'Variant2'
        slurper.body.div[2].div[1].section.@id.text() == 'Conf2'
        slurper.body.div[2].div[1].section.header.h3.text() == 'Configuration: Conf2'
        slurper.body.div[2].div[1].section.p.ul.li.a.@href*.text().containsAll(
                ['Variant2.dist.zip', 'Variant2.dsym.zip', 'Variant2.ipa', 'Variant2.manifest', 'Variant2.mobileprovision', '#Variant2-ahSYM']
        )

        slurper.body.div[3].@id.text() == 'MainVariant-ahSYM'
        slurper.body.div[3].div[0].text() == 'MainVariant'
        slurper.body.div[3].div[1].section.header.text() == 'Configuration: MainConf'
        slurper.body.div[3].div[1].section.p.ul.li*.text().contains('ahSym1')

        slurper.body.div[4].@id.text() == 'Variant2-ahSYM'
        slurper.body.div[4].div[0].text() == 'Variant2'
        slurper.body.div[4].div[1].section.header.text() == 'Configuration: Conf2'
        slurper.body.div[4].div[1].section.p.ul.li*.text().contains('ahSym2')

        slurper.body.div[5].div[1].div[1].h2*.text().containsAll(['MainVariant', 'Variant2'])
        slurper.body.div[5].div[1].div[1].ul.li*.text().containsAll(['a', 'b', 'c', 'd'])
    }

    def 'plain_file_index.html is generated and validated'() {
        given:
        releaseConf.QRCodeFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'qr.png'
            getUrl() >> 'http://qr.png'.toURL()
        }
        releaseConf.mailMessageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'mail_message'
            getUrl() >> 'http://mail_message'.toURL()
        }
        releaseConf.imageMontageFile = GroovyMock(FlowArtifact) {
            getRelativeUrl(_) >> 'image_montage.png'
            getUrl() >> 'http://image_montage.png'.toURL()
        }
        releaseConf.distributionZipFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.dist.zip'
                    getUrl() >> 'http://MainVariant.dist.zip'.toURL()
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.dist.zip'
                    getUrl() >> 'http://Variant2.dist.zip'.toURL()
                }
        ]
        releaseConf.dSYMZipFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.dsym.zip'
                    getUrl() >> 'http://MainVariant.dsym.zip'.toURL()
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.dsym.zip'
                    getUrl() >> 'http://Variant2.dsym.zip'.toURL()
                }
        ]
        releaseConf.ipaFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.ipa'
                    getUrl() >> 'http://MainVariant.ipa'.toURL()
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.ipa'
                    getUrl() >> 'http://Variant2.ipa'.toURL()
                }
        ]
        releaseConf.manifestFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.manifest'
                    getUrl() >> 'http://MainVariant.manifest'.toURL()
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.manifest'
                    getUrl() >> 'http://Variant2.manifest'.toURL()
                }
        ]
        releaseConf.mobileProvisionFiles = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.mobileprovision'
                    getUrl() >> 'http://MainVariant.mobileprovision'.toURL()
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.mobileprovision'
                    getUrl() >> 'http://Variant2.mobileprovision'.toURL()
                }
        ]
        releaseConf.ahSYMDirs = [
                'MainVariant': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'MainVariant.ahsym'
                    getUrl() >> 'http://MainVariant.ahsym'.toURL()
                    getChildArtifacts() >> [GroovyMock(FlowArtifact) {
                        getName() >> 'ahSym1'
                        getRelativeUrl(_) >> 'ahSym1'
                        getUrl() >> 'http://ahSym1.dir'.toURL()
                    }]
                },
                'Variant2': GroovyMock(FlowArtifact) {
                    getRelativeUrl(_) >> 'Variant2.ahsym'
                    getUrl() >> 'http://Variant2.ahsym'.toURL()
                    getChildArtifacts() >> [GroovyMock(FlowArtifact) {
                        getName() >> 'ahSym2'
                        getRelativeUrl(_) >> 'ahSym2'
                        getUrl() >> 'http://ahSym2.dir'.toURL()
                    }]
                }
        ]

        when:
        task.plainFileIndexArtifact(otaFolderPrefix)
        task.preparePlainFileIndexFile()

        then:
        def slurper = new XmlSlurper().parse(releaseConf.plainFileIndexFile.location)
        !releaseConf.plainFileIndexFile.location.text.contains('null')
        slurper.head.title.text() == 'GradleXCode'
        slurper.body.h1[0].text() == 'GradleXCode'
        slurper.body.ul[0].li[0].ul.li.a*.text()*.trim().containsAll(
                ['http://MainVariant.dist.zip', 'http://MainVariant.dsym.zip', 'http://MainVariant.ipa', 'http://MainVariant.manifest', 'http://MainVariant.mobileprovision']
        )
        slurper.body.ul[0].li[1].ul.li.a*.text()*.trim().containsAll(
                ['http://Variant2.dist.zip', 'http://Variant2.dsym.zip', 'http://Variant2.ipa', 'http://Variant2.manifest', 'http://Variant2.mobileprovision']
        )
        slurper.body.ul.li.a*.text()*.trim().containsAll([
                'http://mail_message', 'http://image_montage.png', 'http://qr.png'
        ])
        slurper.body.ul.li.a.@href*.text()*.trim().containsAll([
                'mail_message', 'image_montage.png', 'qr.png'
        ])
    }

    def 'message_file.html is generated and validated'() {
        given:
        releaseConf.otaIndexFile = GroovySpy(FlowArtifact) {
            getUrl() >> 'http://ota.polidea.pl/otaIndexFile.html'.toURL()
        }
        releaseConf.fileIndexFile = GroovySpy(FlowArtifact) {
            getUrl() >> 'http://ota.polidea.pl/fileIndexFile.html'.toURL()
        }

        when:
        task.mailMsgArtifact()
        task.prepareMailMsg()

        then:
        !releaseConf.mailMessageFile.location.text.contains('null')
        releaseConf.releaseMailSubject == "iOS $projectName $fullVersionString is ready to install"
        def slurper = new XmlSlurper().parse(releaseConf.mailMessageFile.location)
        slurper.head.title.text() == 'GradleXCode - iOS'
        slurper.body.b[0].text() == 'GradleXCode'
        slurper.body.b[1].text() == '1.0'
        slurper.body.p[0].ul.li.a.@href.text() == 'http://ota.polidea.pl/otaIndexFile.html'
        slurper.body.p[1].ul.li[0].text() == 'release'
        slurper.body.p[1].ul.li[1].text() == 'notes'
        slurper.body.p[2].ul.li.a.@href.text() == 'http://ota.polidea.pl/fileIndexFile.html'
    }
}
