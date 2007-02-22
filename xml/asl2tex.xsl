<?xml version="1.0" ?>

<xsl:stylesheet
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     version="1.0"
>

<xsl:output method="text" />
<xsl:strip-space elements="*"/>
<xsl:include href="ag2tex.xsl" />

<xsl:template match="/">
% This stylesheet is not complet
\documentclass{article}

\newenvironment{asl}{\ttfamily\begin{tabbing}~~~\=$\leftarrow$ \= ~~~ \= 
\kill}{\end{tabbing}}

	<xsl:call-template name="commands" />

\begin{document}

    <xsl:apply-templates select="//beliefs" />


\subsection*{Plans}


\begin{asl}
<xsl:text/>
		<xsl:apply-templates select="//plan"/>

		
\end{asl}
\end{document}
</xsl:template>


    <xsl:template match="plan">
	    <xsl:if test="count(label) > 0 and not(starts-with(label/literal/term/@functor,'l__'))">
            <xsl:text>@\asllabel{</xsl:text>
			<xsl:apply-templates select="label" />
			<xsl:text>}\\
</xsl:text>
        </xsl:if>

		<xsl:apply-templates select="trigger" />

        <xsl:if test="count(context) > 0">
    		<xsl:text>\\
</xsl:text>
		    <xsl:text>\>:  \> \context{</xsl:text>
			<xsl:apply-templates select="context" />
		    <xsl:text>}</xsl:text>
		</xsl:if>
        
        <xsl:if test="count(body/body-literal) > 0">
		    <xsl:text>\\
</xsl:text>
			<xsl:text disable-output-escaping="yes">\>&lt;- \> \planbody{</xsl:text>
			<xsl:apply-templates select="body"/>
		    <xsl:text>}\\
</xsl:text>
		</xsl:if>

        <xsl:if test="count(body/body-literal) = 0">
		    <xsl:text>.\\
</xsl:text>
		</xsl:if>

		    <xsl:text>\\
</xsl:text>
    </xsl:template>


    <xsl:template match="body">
        <xsl:for-each select="body-literal">
           <xsl:choose>
        		<xsl:when test="literal/@ia = 'true'">
		        	<xsl:apply-templates />
        		</xsl:when>
        		<xsl:when test="string-length(@type) = 0">
		        	<xsl:apply-templates />
        		</xsl:when>
        		<xsl:when test="@type = '?'">
		        	?<xsl:apply-templates />
        		</xsl:when>
        		<xsl:when test="@type = '!' or @type = '!!'">
		        	<xsl:value-of select="@type"/>
					<xsl:apply-templates />
        		</xsl:when>
        		<xsl:when test="@type = '+' or @type = '-'">
		        	<xsl:value-of select="@type"/>
					<xsl:apply-templates />
        		</xsl:when>
        		<xsl:otherwise>
	        		<xsl:value-of select="@type"/>
					<xsl:apply-templates />
        		</xsl:otherwise>        		
        	  </xsl:choose>
          <xsl:if test="not(position()=last())">
			<xsl:text>;\\
    </xsl:text>
		  </xsl:if>
        </xsl:for-each>
    </xsl:template>


</xsl:stylesheet>
