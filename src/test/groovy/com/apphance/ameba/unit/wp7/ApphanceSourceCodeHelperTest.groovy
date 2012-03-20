package com.apphance.ameba.unit.wp7

import java.io.File

import org.gradle.api.AntBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*

import com.apphance.ameba.wp7.plugins.apphance.ApphanceSourceCodeHelper

class ApphanceSourceCodeHelperTest {

	String projectDir
	ApphanceSourceCodeHelper apphanceHelper
	File tmpDir


	@Before
	void setUp() {
		this.projectDir = "testProjects/wp7/AmebaTest/"
		this.apphanceHelper = new ApphanceSourceCodeHelper()
		this.tmpDir = new File("tmp")
		tmpDir.mkdir()
		tmpDir.delete()
		ProjectBuilder projectBuilder = ProjectBuilder.builder()
		Project project = projectBuilder.build()

	}

	@After
	void tearDown() {
	}

	@Test
	public void extractApphanceDllTest() {
		apphanceHelper.extractApphanceDll(tmpDir, "Apphance.WindowsPhone.dll")
		File apphanceDll = new File(tmpDir, "Apphance.WindowsPhone.dll")
		assertTrue(apphanceDll.exists())
	}


	@Test
	void addApphanceToAppCsTest() {
		def appId = "1"
		def version = "1"
		String result = apphanceHelper.addApphanceToAppCs(appCsWithoutApphance, appId, version)
		assertEquals(result, appCsWithApphance)
	}

	@Test
	void removeApphanceFromAppCsTest() {
		String result = apphanceHelper.removeApphanceFromAppCs(appCsWithApphance)
		assertEquals(result, appCsWithoutApphance)
	}


	@Test
	void addApphanceToCsProjTest() {

		String result = apphanceHelper.addApphanceToCsProj(csProjWithoutApphance, "Apphance.WindowsPhone.dll")

		def xmlSlurper = new XmlSlurper(false, false)
		def Project = xmlSlurper.parseText(result)

		boolean hasApphanceWindowsPhone = false

		Project.ItemGroup[0].each { attribute ->

			if (attribute.name.toString().equals("Reference")) {

				if(attribute.@Include.toString().equals("Apphance.WindowsPhone")) {
					// <Reference Include="Apphance.WindowsPhone" HintPath="Apphance.WindowsPhone.dll"/>
					hasApphanceWindowsPhone = true;
				}
            }
		}
		//assertTrue(hasApphanceWindowsPhone)
	}




	@Test
	public void convertApphanceLogsToSystemDebugTest() {
		String result = apphanceHelper.convertSystemDebugToApphanceLogs(sourceCodeWithApphance)
		assertEquals(result, sourceCodeWithoutApphance)
	}

	@Test
	public void convertSystemDebugToApphanceLogsTest() {
		String result = apphanceHelper.convertApphanceLogsToSystemDebug(sourceCodeWithoutApphance)
		assertEquals(result, sourceCodeWithApphance)
	}


	def appCsWithApphance='''
    public partial class App : Application
    {
        public App()
        {
            // Global handler for uncaught exceptions.
            UnhandledException += Application_UnhandledException;UnhandledException += ApphanceLibrary.Apphance.UnhandledExceptionHandler;
            }

        }

        private void Application_Launching(object sender, LaunchingEventArgs e)
        {ApphanceLibrary.Apphance.InitSession(\"1\", \"1\", 1, ApphanceLibrary.Apphance.Mode.QA_MODE);
        }

        private void Application_Activated(object sender, ActivatedEventArgs e)
        {ApphanceLibrary.Apphance.InitSession(\"1\", \"1\", 1, ApphanceLibrary.Apphance.Mode.QA_MODE);
        }
    }
	'''

	def appCsWithoutApphance='''
    public partial class App : Application
    {
        public App()
        {
            // Global handler for uncaught exceptions.
            UnhandledException += Application_UnhandledException;
            }

        }

        private void Application_Launching(object sender, LaunchingEventArgs e)
        {
        }

        private void Application_Activated(object sender, ActivatedEventArgs e)
        {
        }
    }
	'''

	def csProjWithApphance='''
<Project DefaultTargets="Build" ToolsVersion="4.0" xmlns="http://schemas.microsoft.com/developer/msbuild/2003">
  <ItemGroup>
    <Reference Include="Microsoft.Phone" />
    <Reference Include="Microsoft.Phone.Interop" />
    <Reference Include="System.Windows" />
    <Reference Include="system" />
    <Reference Include="System.Core" />
    <Reference Include="System.Net" />
    <Reference Include="System.Xml" />
	<Reference Include="Apphance.WindowsPhone" HintPath="Apphance.WindowsPhone.dll"/>
  </ItemGroup>
</Project>
	'''

	def csProjWithoutApphance='''
<Project DefaultTargets="Build" ToolsVersion="4.0" xmlns="http://schemas.microsoft.com/developer/msbuild/2003">
  <ItemGroup>
    <Reference Include="Microsoft.Phone" />
    <Reference Include="Microsoft.Phone.Interop" />
    <Reference Include="System.Windows" />
    <Reference Include="system" />
    <Reference Include="System.Core" />
    <Reference Include="System.Net" />
    <Reference Include="System.Xml" />
  </ItemGroup>
</Project>
	'''



	def sourceCodeWithApphance='''
namespace AmebaTest.Model
{
    public class Calculator
    {
        public int add(int a, int b)
        {
            return a + b;
        }
    }
}
'''

	def sourceCodeWithoutApphance='''
namespace AmebaTest.Model
{
    public class Calculator
    {
        public int add(int a, int b)
        {
            return a + b;
        }
    }
}
'''


}
