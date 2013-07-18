<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method='html' version='1.0' encoding='UTF-8' indent='yes'/>

<xsl:template match="LAC">
	<HTML><HEAD></HEAD><BODY>
	<xsl:apply-templates/>
	</BODY></HTML>
</xsl:template>

<xsl:template match="LAC-W">
<!-- //Display the word in a TextView not WebView
	<DIV>
	<SPAN style="LINE-HEIGHT: 150%; COLOR: #000000; FONT-SIZE: 180%"><B>
		<xsl:value-of select="."/>
	</B></SPAN>
	</DIV>
-->	
	<xsl:for-each select="LAC-R">
		<xsl:apply-templates/>
	</xsl:for-each>
</xsl:template>

<xsl:template match="LAC-D">
	<DIV style="LINE-HEIGHT: 120%; background-color:#cfddf0; COLOR:#000080; FONT-SIZE: 100%">
		<B>
			<xsl:value-of select="."/>
		</B>
	</DIV>
</xsl:template>

<xsl:template match="C">
	<xsl:for-each select="F">
		<DIV style="MARGIN: 5px 0px">
			<xsl:apply-templates/>
		</DIV>
	</xsl:for-each>
	
	<xsl:if test="E">
		<DIV style="MARGIN: 4px 0px; LINE-HEIGHT: 150%; FONT-SIZE: 100%">
			<B>Extension:</B>
			<FONT COLOR="#229922">
				<xsl:for-each select="E">
					<xsl:value-of select="."/>
	 				<xsl:text disable-output-escaping="yes"><![CDATA[&nbsp;]]></xsl:text>
				</xsl:for-each>
			</FONT>
		</DIV>		
	</xsl:if>
</xsl:template>

<xsl:template match="H">
	<SPAN style="LINE-HEIGHT: 120%; COLOR: #000000; FONT-SIZE: 120%">
		<xsl:apply-templates/>
	</SPAN>
</xsl:template>

<xsl:template match="L">
	<DIV style="MARGIN: 0px 0px 5px; ">
		<xsl:value-of select="."/>
	</DIV>
</xsl:template>

<xsl:template match="M">
	[<FONT COLOR="#009900">
		<xsl:value-of select="."/>
	</FONT>]
</xsl:template>

<xsl:template match="I">
	<DIV style="MARGIN: 0px 0px 5px">
		<xsl:apply-templates/>
	</DIV>
</xsl:template>

<xsl:template match="N">
	<DIV style="MARGIN: 4px 0px; LINE-HEIGHT: 120%; FONT-SIZE: 120%">
		<xsl:apply-templates/>
	</DIV>
</xsl:template>

<xsl:template match="U">
	<FONT color="#c00000">
		<xsl:value-of select="."/>
	</FONT>
</xsl:template>

<xsl:template match="x">
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="h">
	<I>
		<xsl:value-of select="."/>
	</I>
</xsl:template>

</xsl:stylesheet>
