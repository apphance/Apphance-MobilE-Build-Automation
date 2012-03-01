package com.apphance.ameba.wp7.plugins.apphance

import groovy.xml.StreamingMarkupBuilder

import java.io.File
import java.io.FileOutputStream


class ApphanceSourceCodeHelper {


	public void extractApphanceDll(File destinationDir, String dllName) {

		InputStream is = ApphanceSourceCodeHelper.class.classLoader.getResourceAsStream("com/apphance/ameba/wp7/apphance/Apphance.WindowsPhone.dll")

		//For Overwrite the file.
		OutputStream out = new FileOutputStream(new File(destinationDir, dllName), true);

		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0){
			out.write(buf, 0, len);
		}
		is.close();
		out.close();

	}


	public String addApphanceToAppCs(String appCsContent, String appId, String version) {

		def apphanceExceptionLine = "UnhandledException += ApphanceLibrary.Apphance.UnhandledExceptionHandler;";
		def apphanceInitLine = "ApphanceLibrary.Apphance.InitSession("+appId+", \""+version+"\", 1, ApphanceLibrary.Apphance.Mode.QA_MODE);";


		def exceptionPattern = /(UnhandledException\s\+\=\s+Application_UnhandledException;)/
		def launchingPattern = /(Application_Launching\([^\(\r\n]*\)\s+\{)/
		def activatedPattern = /(Application_Activated\([^\(\r\n]*\)\s+\{)/

		return appCsContent.replaceAll(exceptionPattern, "\$1"+apphanceExceptionLine)
			  .replaceAll(launchingPattern, "\$1"+apphanceInitLine)
			  .replaceAll(activatedPattern, "\$1"+apphanceInitLine)
	}


	public String removeApphanceFromAppCs(String appCsContent) {

		def apphanceInitSession = /ApphanceLibrary.Apphance.InitSession\([^\(\r\n]*\);/
		def apphanceException = /(UnhandledException\s\+\=\s+ApphanceLibrary\.Apphance\.UnhandledExceptionHandler;)/

		return appCsContent.replaceAll(apphanceInitSession, "").replaceAll(apphanceException, "")
	}

	public String addApphanceToCsProj(String csProjContent, String apphanceDllPath) {

		def xmlSlurper = new XmlSlurper()
		def xml = xmlSlurper.parseText(csProjContent)
		xml.Project.ItemGroup[0].appendNode {
			Reference(Include : "Apphance.WindowsPhone") { HintPath(apphanceDllPath) }
		}

		def outputBuilder = new StreamingMarkupBuilder()
		String result = outputBuilder.bind{ mkp.yield xml }
		return result
	}

	public String removeApphanceFromCsProj(String csProjPath, String apphanceDllPath) {

		def xmlSlurper = new XmlSlurper()
		def xml = xmlSlurper.parseText(csProj)


	}


	public String convertSystemDebugToApphanceLogs(String file) {
		return file.replaceAll(/(System\.Console\.WriteLine\()/, /(ApphanceLibrary\.Apphance\.Log\("",)/)
	}

	public String convertApphanceLogsToSystemDebug(String file) {
		return file.replaceAll(/(System\.Console\.WriteLine\()/, /(ApphanceLibrary\.Apphance\.Log\("",)/)
	}



}
