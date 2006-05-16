<?xml version="1.0" encoding="ISO-8859-1" ?>

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:param name="h-style"  select="'color: red; font-family: arial;'" />
    <xsl:param name="th-style" select="'text-align: left; vertical-align: top;  color: #330099;'" />
    <xsl:param name="th-style2" select="'text-align: left; color: blue;'" />
    <xsl:param name="td-style" select="'text-align: left; vertical-align: top;'" />
    <xsl:param name="td-style2" select="'text-align: center; vertical-align: top;'" />
    <xsl:param name="trh-style" select="'background-color: #ece7e6; font-family: arial; vertical-align: top;'" />
    <xsl:param name="tr-style" select="'background-color: #ece7e6; font-family: arial;'" />

    <xsl:param name="show-bels" select="'true'" />
    <xsl:param name="show-evt"  select="'true'" />
    <xsl:param name="show-mb"  select="'true'" />
    <xsl:param name="show-int"  select="'true'" />
    <xsl:param name="show-plan"  select="'true'" />
    
    <xsl:output method="html"/>

    <xsl:template match="agent">
        <html>
        <h2 style="{$h-style}"><b><xsl:value-of select="@name"/></b> inspection</h2>
        
        <table border="0" cellspacing="3" cellpadding="6" >
        
        <xsl:apply-templates select="beliefs" />
        <xsl:apply-templates select="circumstance/mailbox" />
        <xsl:apply-templates select="circumstance/events" />
        <xsl:apply-templates select="circumstance/plans" />
        <xsl:apply-templates select="circumstance/intentions" />
        <xsl:apply-templates select="circumstance/actions" />

        <!--xsl:apply-templates select="plans" /-->
        </table>
        </html>
    </xsl:template>

	<!-- create de +/- buttom -->
    <xsl:template name="hideshow">
        <xsl:param name="show" select="'false'" />
        <xsl:param name="item" select="'none'" />
        <xsl:param name="ds"   select="'none'" />
        <xsl:if test="$show='true'">
	        <th valign="top" style="{$th-style}">
	        	<a href="hide?{$item}" style="text-decoration: none">
	        		-<xsl:text> </xsl:text>
	        	</a>
	        	<xsl:value-of select="$ds" />
	       	</th>
        </xsl:if>
        <xsl:if test="$show='false'">
	        <th valign="top" style="{$th-style}">
	        	<a href="show?{$item}" style="text-decoration: none">+
	        		<xsl:text> </xsl:text>
	        	</a>
	        	<xsl:value-of select="$ds" />
	       	</th>
	        <td style="{$td-style}"></td>
        </xsl:if> 
    </xsl:template>
    
    <xsl:template match="beliefs">
        <tr style="{$trh-style}">
        <xsl:call-template name="hideshow">
        	<xsl:with-param name="show" select="$show-bels" />
        	<xsl:with-param name="item" select="'bels'" />
        	<xsl:with-param name="ds" select="'Beliefs'" />
        </xsl:call-template>
        <xsl:if test="$show-bels='true'">
	        <td style="{$td-style}">
		        <table cellspacing="0" cellpadding="2">
			        <xsl:apply-templates select="bel" />
		        </table>
	        </td>
        </xsl:if>
        </tr>
    </xsl:template>

    <xsl:template match="mailbox">
        <tr style="{$trh-style}">
        <xsl:call-template name="hideshow">
        	<xsl:with-param name="show" select="$show-mb" />
        	<xsl:with-param name="item" select="'mv'" />
        	<xsl:with-param name="ds" select="'MailBox'" />
        </xsl:call-template>
        <xsl:if test="$show-mb='true'">
	        <td style="{$td-style}">
	        <table cellspacing="0" cellpadding="3">
	        <xsl:apply-templates select="message" />
	        </table>
	        </td>
        </xsl:if>
        </tr>
    </xsl:template>

    <xsl:template match="bel|message">
        <tr style="{$trh-style}">
            <td style="text-align: left">
                <xsl:apply-templates />
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="events">
        <tr style="{$trh-style}">
        <xsl:call-template name="hideshow">
        	<xsl:with-param name="show" select="$show-evt" />
        	<xsl:with-param name="item" select="'evt'" />
        	<xsl:with-param name="ds" select="'Events'" />
        </xsl:call-template>
        <xsl:if test="$show-evt='true'">
	        <td style="{$td-style}">
	        <table cellspacing="0" cellpadding="3">
	        <tr style="{$trh-style}">
			<th valign="top" style="{$th-style2}">Sel</th>
			<th valign="top" style="{$th-style2}">Trigger</th>
			<th valign="top" style="{$th-style2}">Intention</th>
	        </tr>
	        <xsl:apply-templates />
	        </table>
	        </td>
        </xsl:if>
        </tr>
    </xsl:template>
    
    <xsl:template match="event">
        <tr style="{$trh-style}">
            <td valign="top" style="{$td-style2}">
	   	    <xsl:if test="@selected='true'">
  	    	  <b>X</b>
   		    </xsl:if>
            </td>

            <td valign="top" style="{$td-style}">
		        <xsl:value-of select="@trigger" />
            </td>
            <td valign="top" style="{$td-style2}">
		        <xsl:value-of select="@intention" />
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="intentions">
        <tr style="{$trh-style}">
        <xsl:call-template name="hideshow">
        	<xsl:with-param name="show" select="$show-int" />
        	<xsl:with-param name="item" select="'int'" />
        	<xsl:with-param name="ds" select="'Intentions'" />
        </xsl:call-template>
        <xsl:if test="$show-int='true'">
	        <td valign="top" style="{$td-style}">
	        <table cellspacing="0" cellpadding="5">
	        <tr style="{$trh-style}">
			<th valign="top" style="{$th-style2}">Sel</th>
			<th valign="top" style="{$th-style2}">Id</th>
			<th valign="top" style="{$th-style2}">Pen</th>
			<th valign="top" style="{$th-style2}">Intended Means Stack and Unifier</th>
	        </tr>
	        <xsl:apply-templates />
	        </table>
	        </td>
	    </xsl:if>
        </tr>
    </xsl:template>

    <xsl:template match="intention">
        <tr style="{$trh-style}">
            <td valign="top" style="{$td-style2}">
	   	    <xsl:if test="@selected='true'">
  	    	  <b>X</b>
   		    </xsl:if>
            </td>
            
            <td valign="top" style="{$td-style2}">
		        <xsl:value-of select="@id" />
            </td>

            <td valign="top" style="{$td-style2}">
     	    <xsl:if test="string-length(@pending) > 0">
  	    	  <b><xsl:value-of select="@pending" /></b>
   		    </xsl:if>
            </td>

			<td valign="top">
	        <table cellspacing="0" cellpadding="2">
	        <xsl:apply-templates />
    	    </table>
	   	    <hr/>
    	    </td>
        </tr>
    </xsl:template>
    <xsl:template match="intended-means">
    	<tr>
            <td valign="top" style="{$td-style}">
                <xsl:apply-templates select="plan"/>
            </td>
            
            <td valign="top" style="{$td-style}">
                <xsl:text> </xsl:text><xsl:text> </xsl:text>
                <xsl:apply-templates select="unifier"/>
            </td>
		</tr>
    </xsl:template>


    <xsl:template match="actions">
        <tr style="{$trh-style}">
        <th valign="top" style="{$th-style}">Actions</th>
        <td valign="top" style="{$td-style}">
        <table ellspacing="0" cellpadding="3">
        <tr style="{$trh-style}">
		<th valign="top" style="{$th-style2}">Pend</th>
		<th valign="top" style="{$th-style2}">Feed</th>
		<th valign="top" style="{$th-style2}">Sel</th>
		<th valign="top" style="{$th-style2}">Term</th>
		<th valign="top" style="{$th-style2}">Result</th>
		<th valign="top" style="{$th-style2}">Intention</th>
        </tr>
        <xsl:apply-templates select="action"/>
        </table>
        </td>
        </tr>
    </xsl:template>

    <xsl:template match="action">
        <tr style="{$trh-style}">
            
        <td valign="top" style="{$td-style2}">
   	    <xsl:if test="@pending='true'">
  	    	  X
   	    </xsl:if>
        </td>

	    <td valign="top" style="{$td-style2}">
   	    <xsl:if test="@feedback='true'">
  	    	  X
   	    </xsl:if>
	    </td>

        <td valign="top" style="{$td-style2}">
   	    <xsl:if test="@selected='true'">
  	    	  <b>X</b>
   	    </xsl:if>
        </td>

        <td valign="top" style="{$td-style}">
		        <xsl:value-of select="@term" /> 
        </td>
        <td valign="top" style="{$td-style2}">
		        <xsl:value-of select="@result" />
        </td>
        <td valign="top" style="{$td-style2}">
		        <xsl:value-of select="@intention" />
        </td>
        </tr>
    </xsl:template>

    <xsl:template match="plans">
        <tr style="{$trh-style}">
        <xsl:call-template name="hideshow">
        	<xsl:with-param name="show" select="$show-plan" />
        	<xsl:with-param name="item" select="'plan'" />
        	<xsl:with-param name="ds" select="'Plans'" />
        </xsl:call-template>
        <xsl:if test="$show-plan='true'">
	        <td>
	        <table ellspacing="0" cellpadding="3">
	        <tr style="{$trh-style}">
	    	<!--th valign="top" style="{$th-style2}">Rel</th-->
		    <th valign="top" style="{$th-style2}">App</th>
		    <th valign="top" style="{$th-style2}">Sel</th>
		    <th valign="top" style="{$th-style2}">Plan</th>
		    <th valign="top" style="{$th-style2}">Unifier</th>
	        </tr>
	        <xsl:apply-templates />
	        </table>
	        </td>
		</xsl:if>
        </tr>
    </xsl:template>

    <xsl:template match="option">
        <tr style="{$trh-style}">
            
        <!-- 
        <td valign="top" style="{$td-style2}">
   	    <xsl:if test="@relevant='true'">
  	    	  X
   	    </xsl:if>
        </td>
        -->

	    <td valign="top" style="{$td-style2}">
   	    <xsl:if test="@applicable='true'">
  	    	  X
   	    </xsl:if>
	    </td>

        <td valign="top" style="{$td-style2}">
   	    <xsl:if test="@selected='true'">
  	    	  <b>X</b>
   	    </xsl:if>
        </td>

        <td valign="top" style="{$td-style}">
                <xsl:apply-templates select="plan"/>
        </td>
        <td valign="top" style="{$td-style}">
                <xsl:apply-templates select="unifier"/>
        </td>
        </tr>
    </xsl:template>


    <xsl:template match="plan">
	    <xsl:if test="string-length(@label) > 0">
           <span style="color: rgb(51, 51, 51);">
           @<xsl:value-of select="@label" /> 
           </span><br/>
        </xsl:if>
        
        <xsl:value-of select="@trigger" /> 
        
        <xsl:text> </xsl:text>
        <b>:</b>
        <xsl:text> </xsl:text>
        <xsl:value-of select="@context" /> 
        
        <blockquote>
	        <b>&lt;-</b><xsl:text> </xsl:text>
    	    <xsl:value-of select="@body" />
        </blockquote>
    </xsl:template>
    
</xsl:stylesheet> 
