package com.apphance.ameba.unit.wp7

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test

import com.apphance.ameba.wp7.plugins.test.TrxToXmlTransformer

class TrxToXmlTransformerTest {

	TrxToXmlTransformer transformer

	@Before
	void setUp(){
		transformer = new TrxToXmlTransformer();
	}

	@Test
	public void testTransform() throws Exception {

		Reader reader = new StringReader(input2)
		Writer writer = new StringWriter()

		transformer.transform(reader, writer)

		String output = writer.toString()


		def testResults = new XmlSlurper().parseText(output)

		def testSuite = testResults.'test-suite'[0]
		assertNotNull(testSuite)
		def results = testSuite.'results'[0]
		def testCase = results.'test-case'[0]

		assertNotNull(testCase)
		assertEquals(".TestAlwaysFail", testCase.@name.toString())
		assertEquals("True", testCase.@executed.toString())
		assertEquals("1", testCase.@asserts.toString())
	}

	def input = '''
	<TestRun id="f55f9934-da5e-4035-88b0-d130e7f73b36" name="marek_GOCAL 2012-02-22T15:44:35" runUser="marek" xmlns="http://microsoft.com/schemas/VisualStudio/TeamTest/2010">
	  <TestSettings id="59648284-e792-482c-a030-ed7e4a8bc26f" name="Local Test Run">
		<Description>This is a default test run configuration for a local test run.</Description>
	  </TestSettings>
	  <ResultSummary outcome="Passed">
		<Counters total="1" passed="1" />
	  </ResultSummary>
	  <Times creation="2012-02-22T15:44:26.5900000+01:00" queuing="2012-02-22T15:44:26.5900000+01:00" start="2012-02-22T15:44:35.7200000+01:00" finish="2012-02-22T15:44:35.8340000+01:00" />
	  <TestDefinitions>
		<UnitTest name="add" storage="AmebaTest.Tests" id="a4c516a1-68b1-4d88-bf36-0fbcea18af96">
		  <Owners>
			<Owner name="" />
		  </Owners>
		  <Execution id="fac7e050-ae8b-4ccc-990a-d755e2b0e23d" />
		  <TestMethod codeBase="AmebaTest.Tests" adapterTypeName="Microsoft.VisualStudio.TestTools.TestTypes.Unit.UnitTestAdapter, Microsoft.VisualStudio.QualityTools.Tips.UnitTest.Adapter, Version=9.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a" className="CalculatorTest" name="add" />
		</UnitTest>
	  </TestDefinitions>
	  <TestLists>
		<TestList name="All" id="ed701ec4-ec87-4e29-af7c-b8b73402894e" />
	  </TestLists>
	  <TestEntries>
		<TestEntry testId="a4c516a1-68b1-4d88-bf36-0fbcea18af96" executionId="fac7e050-ae8b-4ccc-990a-d755e2b0e23d" testListId="ed701ec4-ec87-4e29-af7c-b8b73402894e" />
	  </TestEntries>
	  <Results>
		<UnitTestResult executionId="fac7e050-ae8b-4ccc-990a-d755e2b0e23d" testId="a4c516a1-68b1-4d88-bf36-0fbcea18af96" testName="add" computerName="GOCAL" duration="00:00:00.0140000" startTime="2012-02-22T15:44:35.7330000+01:00" endTime="2012-02-22T15:44:35.7470000+01:00" testType="13cdc9d9-ddb5-4fa4-a97d-d965ccfc6d4b" outcome="Passed" testListId="ed701ec4-ec87-4e29-af7c-b8b73402894e" />
	  </Results>
	</TestRun>
'''.trim()

def input2 = '''
<TestRun id="12c42a44-0ee4-470f-b0c1-a77c94e43bd1" name="marek_GOCAL 2012-03-16T00:57:46" runUser="marek" xmlns="http://microsoft.com/schemas/VisualStudio/TeamTest/2010">
<TestSettings id="3a70f979-77a3-4329-a769-3ef39362b9cf" name="Local Test Run">
<Description>This is a default test run configuration for a local test run.</Description>
</TestSettings>
<ResultSummary outcome="Passed">
<Counters total="1" passed="1" />
</ResultSummary>
<Times creation="2012-03-16T00:57:33.4060000-08:00" queuing="2012-03-16T00:57:33.4060000-08:00" start="2012-03-16T00:57:46.1810000-08:00" finish="2012-03-16T00:57:46.3140000-08:00" />
<TestDefinitions>
<UnitTest name="TestAlwaysFail" storage="MszePL.Tests" id="ee0764dc-0cad-41d1-a4b1-a0c4797d03df">
<Owners>
<Owner name="" />
</Owners>
<Execution id="764ce9de-706e-4cb8-84d8-cf48086fc856" />
<TestMethod codeBase="MszePL.Tests" adapterTypeName="Microsoft.VisualStudio.TestTools.TestTypes.Unit.UnitTestAdapter, Microsoft.VisualStudio.QualityTools.Tips.UnitTest.Adapter, Version=9.0.0.0, Culture=neutral, PublicKeyToken=b03f5f7f11d50a3a" className="UtilsTest" name="TestAlwaysFail" />
</UnitTest>
</TestDefinitions>
<TestLists>
<TestList name="All" id="b7ba9a15-67de-4e3b-8687-00433b42b1c0" />
</TestLists>
<TestEntries>
<TestEntry testId="ee0764dc-0cad-41d1-a4b1-a0c4797d03df" executionId="764ce9de-706e-4cb8-84d8-cf48086fc856" testListId="b7ba9a15-67de-4e3b-8687-00433b42b1c0" />
</TestEntries>
<Results>
<UnitTestResult executionId="764ce9de-706e-4cb8-84d8-cf48086fc856" testId="ee0764dc-0cad-41d1-a4b1-a0c4797d03df" testName="TestAlwaysFail" computerName="GOCAL" duration="00:00:00.0780000" startTime="2012-03-16T00:57:46.1490000-08:00" endTime="2012-03-16T00:57:46.2270000-08:00" testType="13cdc9d9-ddb5-4fa4-a97d-d965ccfc6d4b" outcome="Passed" testListId="b7ba9a15-67de-4e3b-8687-00433b42b1c0" />
</Results>
</TestRun>
'''.trim()


def expected = '''
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<test-results>
	<test-suite name="" success="True" time="0.000" asserts="1">
		<results>
			<test-case name=".add" executed="True" success="True" time="0" asserts="1"/>
			</results>
	</test-suite>
</test-results>
'''.trim()

}
