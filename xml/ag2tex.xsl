<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:output method="text" />
	<xsl:strip-space elements="*" />
	

    <xsl:template match="agent">
% This stylesheet is not complet
\documentclass{article}

\usepackage{booktabs}

\begin{document}

\section*{Inspection of agent \emph{<xsl:value-of select="@name"/>} (cycle \#<xsl:value-of select="@cycle"/>)}

        <xsl:apply-templates select="beliefs" />
        <xsl:apply-templates select="circumstance/intentions" />

        <!--xsl:apply-templates select="circumstance/mailbox" />
        <xsl:apply-templates select="circumstance/events" />
        <xsl:apply-templates select="circumstance/plans" />
        <xsl:apply-templates select="circumstance/actions" /-->
\end{document}
    </xsl:template>

    <xsl:template name="commands">
<xsl:text>
\newcommand{\aslnumber}[1]{$#1$}
\newcommand{\aslstring}[1]{\textsf{#1}}
\newcommand{\aslvar}[1]{\textit{#1}}
\newcommand{\asllabel}[1]{\textbf{#1}}
</xsl:text>
    </xsl:template>
    
    <xsl:template match="beliefs">

\subsection*{Beliefs and Rules}

<xsl:text>
\noindent
{\ttfamily
</xsl:text>
        <xsl:for-each select="literal|rule">
            <xsl:apply-templates select="." />
			<xsl:text>\\
</xsl:text>
	    </xsl:for-each>
		<xsl:text>
}</xsl:text>
    </xsl:template>


    <xsl:template match="intentions">

\subsection*{Intentions}

<xsl:text/>

	        <xsl:apply-templates />
    </xsl:template>



    <xsl:template match="intention">
		<xsl:text>
\noindent
Intention: </xsl:text><xsl:value-of select="@id" />
		<xsl:text>\\[.1cm]

\noindent
{\ttfamily
\begin{tabular}{llll}\toprule

</xsl:text>

        <xsl:for-each select="intended-means">
	       	<xsl:text>\multicolumn{3}{l}{</xsl:text>
			<xsl:apply-templates select="plan/trigger" />
			<xsl:text>} &amp; </xsl:text>

        	<xsl:apply-templates select="unifier"/>
	        <xsl:text>\\
</xsl:text>			
		
			<xsl:apply-templates select="plan"/>

			<xsl:if test="not(position()=last())">
		        <xsl:text>\\\midrule

</xsl:text>			
			</xsl:if> 
			<xsl:if test="position()=last()">
		        <xsl:text>\\\bottomrule

</xsl:text>			
			</xsl:if> 
        </xsl:for-each>

		<xsl:text>\end{tabular}
}\\[.3cm]

</xsl:text>
    </xsl:template>



    <xsl:template match="plan">
        <xsl:if test="count(context) > 0">
		    <xsl:text>&amp;:  &amp; </xsl:text>
			<xsl:apply-templates select="context" />
		    <xsl:text>\\
</xsl:text>
		</xsl:if>
        
        <xsl:if test="count(body/body-literal) > 0">
			<xsl:text disable-output-escaping="yes">&amp;&lt;- &amp; </xsl:text>
			<xsl:apply-templates select="body"/>
		</xsl:if>
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
&amp;   &amp; </xsl:text>
		  </xsl:if>
          <xsl:if test="position()=last()">.</xsl:if>
        </xsl:for-each>
    </xsl:template>


    <xsl:template match="expression">
		<xsl:text>(</xsl:text>
		<xsl:apply-templates select="left" />
        <xsl:choose>
      		<xsl:when test="contains(@operator, '&amp;')">
	        	<xsl:text disable-output-escaping="yes"> \&amp; </xsl:text>
       		</xsl:when>
       		<xsl:otherwise>
        		<xsl:value-of select="@operator" />
       		</xsl:otherwise>        		
        </xsl:choose>
		<xsl:apply-templates select="right" />
		<xsl:text>)</xsl:text>
    </xsl:template>
    
    
    <xsl:template match="trigger">
	    	<xsl:value-of select="@add"/>
	    	<xsl:value-of select="@type"/>
	    	<xsl:apply-templates />
    </xsl:template>



        
    <xsl:template match="term">
		<xsl:text/><xsl:value-of select="@functor"/><xsl:text/>
		<xsl:if test="count(arguments) > 0">
			<xsl:text>(</xsl:text>
            <xsl:for-each select="arguments/*">
				<xsl:apply-templates select="." />
				<xsl:if test="not(position()=last())">, </xsl:if>
			</xsl:for-each>
			<xsl:text>)</xsl:text>
		</xsl:if>
		<xsl:if test="count(annotations) > 0">
			<xsl:text>\small{[</xsl:text>
			<xsl:for-each select="annotations/*">
				<xsl:apply-templates select="." />
				<xsl:if test="not(position()=last())">, </xsl:if>
			</xsl:for-each>
			<xsl:text>]}</xsl:text>
		</xsl:if>
    </xsl:template>

    <xsl:template match="var-term">
		<xsl:text>\aslvar{</xsl:text>
		<xsl:value-of select="text()"/>
		<xsl:text>}</xsl:text>
    </xsl:template>
    <xsl:template match="number-term">
		<xsl:text>\aslnumber{</xsl:text>
		<xsl:value-of select="text()"/>
		<xsl:text>}</xsl:text>
    </xsl:template>
    <xsl:template match="string-term">
		<xsl:text>\aslstring{</xsl:text>
   		<xsl:value-of select="text()"/>
		<xsl:text>}</xsl:text>
    </xsl:template>
</xsl:stylesheet> 
