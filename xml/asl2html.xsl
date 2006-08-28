<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>

<xsl:output method="html" />
<xsl:strip-space elements="*"/>
<xsl:include href="agInspection.xsl" />

<xsl:template match="/">
	<html>

    <xsl:apply-templates select="//beliefs" />

	<xsl:apply-templates select="//plans"/>
	</html>
</xsl:template>

<xsl:template match="beliefs">
	<h2>Beliefs and Rules</h2>
    <xsl:for-each select="literal|rule">
    	<span style="color: {$bc}">
        <xsl:apply-templates select="." />
        <br/>
        </span>
    </xsl:for-each>
</xsl:template>

<xsl:template match="plans">
	<h2>Plans</h2>
    <xsl:for-each select="plan">
        <xsl:apply-templates select="." />
        <br/>
    </xsl:for-each>
</xsl:template>

    <xsl:template match="body">
        <xsl:for-each select="body-literal">
           <xsl:choose>
        		<xsl:when test="literal/@ia = 'true'">
		        	<span style="color: {$iac}"><xsl:apply-templates />	</span>
        		</xsl:when>
        		<xsl:when test="string-length(@type) = 0">
		        	<span style="color: {$ac}"><xsl:apply-templates />	</span>
        		</xsl:when>
        		<xsl:when test="@type = '?'">
		        	<span style="color: {$tgc}">?<xsl:apply-templates />	</span>
        		</xsl:when>
        		<xsl:when test="@type = '!' or @type = '!!'">
		        	<span style="color: {$agc}"><xsl:value-of select="@type"/><xsl:apply-templates />	</span>
        		</xsl:when>
        		<xsl:when test="@type = '+' or @type = '-'">
		        	<span style="color: {$bc}"><xsl:value-of select="@type"/><xsl:apply-templates />	</span>
        		</xsl:when>
        		<xsl:otherwise>
	        		<xsl:value-of select="@type"/><xsl:apply-templates />
        		</xsl:otherwise>        		
        	  </xsl:choose>
          <xsl:if test="not(position()=last())">; <br/></xsl:if>
          <xsl:if test="position()=last()">.</xsl:if>
        </xsl:for-each>
    </xsl:template>


</xsl:stylesheet>
