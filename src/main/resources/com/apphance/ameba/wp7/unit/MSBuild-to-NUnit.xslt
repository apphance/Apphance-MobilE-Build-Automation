<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:t="http://microsoft.com/schemas/VisualStudio/TeamTest/2010">

  <xsl:output encoding="utf-8" standalone="no" version="1.0"/>
  <xsl:template match="/">
    <xsl:text>&#10;</xsl:text>

    <!-- <test-results -->
    <xsl:element name="test-results">
      <xsl:text>&#10;&#x9;</xsl:text>

      <!-- <test-suite -->
      <xsl:element name="test-suite">

        <!-- name="" -->
        <xsl:attribute name="name">
          <xsl:value-of select="substring-after(t:TestRun/t:TestDefinitions/t:UnitTest/@storage,'release\')"/>
        </xsl:attribute>

        <!-- success="" -->
        <xsl:attribute name="success">
          <xsl:choose>
            <xsl:when test="t:TestRun/t:ResultSummary/@outcome='Passed'">
              <xsl:text>True</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>False</xsl:text>
            </xsl:otherwise>

          </xsl:choose>
        </xsl:attribute>

        <!-- time="" -->
        <xsl:attribute name="time">0.000</xsl:attribute>

        <!-- asserts="" -->
        <xsl:attribute name="asserts">
          <xsl:value-of select="count(t:TestRun/t:TestDefinitions/t:UnitTest)"/>
        </xsl:attribute>
        <xsl:text>&#10;&#x9;&#x9;</xsl:text>

        <!-- <results> -->
        <xsl:element name="results">
          <xsl:text>&#10;&#x9;&#x9;&#x9;</xsl:text>

          <xsl:for-each select="t:TestRun/t:TestDefinitions/t:UnitTest">
            <xsl:variable name="id" select="@id"></xsl:variable>

            <!-- <test-case -->
            <xsl:element name="test-case">

              <!-- name="" -->
              <xsl:attribute name="name">
                <xsl:value-of select="substring-before(t:TestMethod/@className, ',')"/>.<xsl:value-of select="@name"></xsl:value-of>
              </xsl:attribute>

              <!-- executed="" -->
              <xsl:attribute name="executed">True</xsl:attribute>

              <!-- success="" -->
              <xsl:attribute name="success">
                <xsl:choose>
                  <xsl:when test="/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/@outcome='Passed'">
                    <xsl:text>True</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:text>False</xsl:text>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:attribute>

              <!-- time="" -->
              <xsl:attribute name="time">
                <xsl:value-of select="
                              (substring(/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/@duration, 1, 2) * 60 * 60) +
                              (substring(/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/@duration, 4, 2) * 60) +
                              (substring(/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/@duration, 7, 2)) +
                              round((substring(/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/@duration, 9, 4)))"/><xsl:value-of select="@duration"></xsl:value-of>
              </xsl:attribute>

              <!-- asserts="" -->
              <xsl:attribute name="asserts">1</xsl:attribute>


              <xsl:if test="/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/@outcome!='Passed'">
                <xsl:text>&#10;&#x9;&#x9;&#x9;&#x9;</xsl:text>

                <!-- <failure> -->
                <xsl:element name="failure">
                  <xsl:text>&#10;&#x9;&#x9;&#x9;&#x9;&#x9;</xsl:text>

                  <!-- <message> -->
                  <xsl:element name="message">
                    <xsl:value-of select="/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/t:Output/t:ErrorInfo/t:Message"/>
                  </xsl:element>
                  <xsl:text>&#10;&#x9;&#x9;&#x9;&#x9;&#x9;</xsl:text>
                  <!-- </message> -->

                  <!-- <stack-trace> -->
                  <xsl:element name="stack-trace">
                    <xsl:value-of select="/t:TestRun/t:Results/t:UnitTestResult[@testId=$id]/t:Output/t:ErrorInfo/t:StackTrace"/>
                  </xsl:element>
                  <xsl:text>&#10;&#x9;&#x9;&#x9;&#x9;</xsl:text>
                  <!-- </stack-trace> -->

                </xsl:element>
                <xsl:text>&#10;&#x9;&#x9;&#x9;</xsl:text>
                <!-- </failure> -->

              </xsl:if>
            </xsl:element>

            <xsl:text>&#10;&#x9;&#x9;&#x9;</xsl:text>
            <!-- </test-case> -->
          </xsl:for-each>
        </xsl:element>
        <xsl:text>&#10;&#x9;</xsl:text>
        <!-- </results> -->

      </xsl:element>
      <!-- </test-suite> -->
      <xsl:text>&#10;</xsl:text>

    </xsl:element>
    <!-- </test-results -->

  </xsl:template>
</xsl:stylesheet>
