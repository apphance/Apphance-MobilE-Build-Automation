package com.apphance.ameba.wp7.plugins.test

import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 *	Transforms mstest *.trx output files to junit/nuint xml
 */
class TrxToXmlTransformer {

	static Logger logger = Logging.getLogger(TrxToXmlTransformer.class)

	//private File xslt

	public TrxToXmlTransformer() {
	}

	public void transform(Reader input, Writer output) {
		StreamSource source = new StreamSource(input)
		StreamResult result = new StreamResult(output)
		transformInternal(source, result)
	}

	public void transform(InputStream input, OutputStream output) {
		transformInternal(new StreamSource(input), new StreamResult(output))
	}

	public void transform(File input, File output) {
		transformInternal(new StreamSource(input), new StreamResult(output))
	}

	void transformInternal(StreamSource streamSource, StreamResult streamResult) {
		InputStream xslt = Wp7TestPlugin.class.classLoader.getResourceAsStream('com/apphance/ameba/wp7/unit/MSBuild-to-NUnit.xslt')
		def factory = TransformerFactory.newInstance()
		def transformer = factory.newTransformer(new StreamSource(xslt))
		transformer.transform(streamSource, streamResult)
	}

	/*
	 private void transformInternal(String input, StreamResult streamResult) {
	 def factory = TransformerFactory.newInstance()
	 StringWriter writer = new StringWriter()
	 def transformer = factory.newTransformer(new StreamSource(new StringReader(xslt)))
	 transformer.transform(new StreamSource(new StringReader(input)), streamResult)
	 }
	 */
}

