<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:output  indent="yes" method="xml" omit-xml-declaration="no"/>
  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
  </xsl:template>

  <xsl:template match="//bundle/groupId">
    <xsl:copy><xsl:apply-templates select="@*|node()"/></xsl:copy>
    <xsl:if test=".='org.sakaiproject.nakamura'">
      <classifier>emma</classifier>
    </xsl:if>
  </xsl:template>

  <xsl:apply-templates/>

</xsl:stylesheet>
