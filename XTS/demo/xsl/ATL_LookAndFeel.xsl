<!--
  JBoss, Home of Professional Open Source
  Copyright 2006, Red Hat Middleware LLC, and individual contributors
  as indicated by the @author tags. 
  See the copyright.txt in the distribution for a full listing 
  of individual contributors.
  This copyrighted material is made available to anyone wishing to use,
  modify, copy, or redistribute it subject to the terms and conditions
  of the GNU Lesser General Public License, v. 2.1.
  This program is distributed in the hope that it will be useful, but WITHOUT A
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
  PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
  You should have received a copy of the GNU Lesser General Public License,
  v.2.1 along with this distribution; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  MA  02110-1301, USA.

  
  (C) 2005-2006,
  @author JBoss Inc.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html" doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN" doctype-system="http://www.w3.org/TR/REC-html40/loose.dtd" encoding="ISO-8859-1" indent="yes"/>

    <xsl:param name="rootpath" select="/page/@rootpath"/>
    <xsl:param name="sitemap">../docs/includes/site-map.xml</xsl:param>

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="page">
        <HTML>
            <HEAD>
                <META name="description" content="Arjuna Technologies: Suppliers of transactioning, messaging, and coordination middleware for reliable distributed systems"/>
                <META name="keywords" content="transactions, transactioning, atomic transaction, OTS, JTS, BTP, JMS, WS-Coordination, WS-Transaction, BPEL, JAXTX, Activity Service"/>
                <TITLE><xsl:value-of select="@title"/></TITLE>
                <xsl:element name="LINK">
                    <xsl:attribute name="href"><xsl:value-of select="$rootpath"/>/styles.css</xsl:attribute>
                    <xsl:attribute name="rel">stylesheet</xsl:attribute>
                    <xsl:attribute name="type">text/css</xsl:attribute>
                </xsl:element>
                <xsl:element name="SCRIPT">
                    <xsl:attribute name="type">text/javascript</xsl:attribute>
                    <xsl:attribute name="language">JavaScript</xsl:attribute>

if (parent != self)
    top.location.href = location.href;

function gotoURL(url)
{
    if (url != "")
        location.href = url;
}

function printThisPage()
{
    window.print();
}

function emailThisPage()
{
    location.href = "mailto:?subject=" + document.title + "&amp;body=" + location.href;
}
                </xsl:element>
            </HEAD>
            <BODY bgcolor="#FFFFFF" leftmargin="0" topmargin="0" marginwidth="0" marginheight="0">
                <TABLE width="727" border="0" align="center" cellpadding="0" cellspacing="0">
                    <xsl:apply-templates/>
                </TABLE>
            </BODY>
        </HTML>
    </xsl:template>

    <xsl:template match="header">
        <TR>
            <TD width="494">
                <xsl:element name="IMG">
                    <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/JBoss_DivOfRH_RGB.gif</xsl:attribute>
                    <xsl:attribute name="alt">arjuna logo</xsl:attribute>
                    <xsl:attribute name="width">178</xsl:attribute>
                    <xsl:attribute name="height">63</xsl:attribute>
                </xsl:element>
            </TD>
            <TD width="233">
                <xsl:element name="IMG">
                    <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/arjuna_strapline.gif</xsl:attribute>
                    <xsl:attribute name="alt">arjuna strap line</xsl:attribute>
                    <xsl:attribute name="width">233</xsl:attribute>
                    <xsl:attribute name="height">63</xsl:attribute>
                </xsl:element>
            </TD>
        </TR>
    </xsl:template>

    <xsl:template match="content[area]">
        <TR>
            <TD colspan="2">
                <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                    <TR>
                        <TD width="6" height="214">
                            <xsl:element name="IMG">
                                <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                <xsl:attribute name="width">6</xsl:attribute>
                                <xsl:attribute name="height">10</xsl:attribute>
                            </xsl:element>
                        </TD>
                        <xsl:apply-templates select="navigation"/>
                        <TD width="6" height="214">
                            <xsl:element name="IMG">
                                <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                <xsl:attribute name="width">6</xsl:attribute>
                                <xsl:attribute name="height">10</xsl:attribute>
                            </xsl:element>
                        </TD>
                        <TD width="555" valign="top">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <xsl:apply-templates select="area"/>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
    </xsl:template>

    <xsl:template match="content[area-left]">
        <TR>
            <TD colspan="2">
                <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                    <TR>
                        <TD width="6" height="214">
                            <xsl:element name="IMG">
                                <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                <xsl:attribute name="width">6</xsl:attribute>
                                <xsl:attribute name="height">10</xsl:attribute>
                            </xsl:element>
                        </TD>
                        <xsl:apply-templates select="navigation"/>
                        <TD width="6" height="214">
                            <xsl:element name="IMG">
                                <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                <xsl:attribute name="width">6</xsl:attribute>
                                <xsl:attribute name="height">10</xsl:attribute>
                            </xsl:element>
                        </TD>
                        <TD width="356" valign="top">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <xsl:apply-templates select="area-left"/>
                            </TABLE>
                        </TD>
                        <TD width="6" height="214">
                            <xsl:element name="IMG">
                                <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                <xsl:attribute name="width">6</xsl:attribute>
                                <xsl:attribute name="height">10</xsl:attribute>
                            </xsl:element>
                        </TD>
                        <TD width="172" valign="top">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <xsl:apply-templates select="area-right|area-right-blue"/>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
    </xsl:template>

    <xsl:template match="navigation">
        <TD width="172" valign="top">
            <FORM name="form1" method="post" action="">
                <TABLE width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#9E9E9E">
                    <TR>
                        <TD height="57">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <TR>
                                    <xsl:element name="TD">
                                        <xsl:attribute name="height">8</xsl:attribute>
                                        <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/header_grid.gif</xsl:attribute>
                                        <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
                                        <xsl:element name="IMG">
                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                            <xsl:attribute name="width">6</xsl:attribute>
                                            <xsl:attribute name="height">10</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </TR>
                            </TABLE>
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="8">
                                <TR>
                                    <TD valign="top" bgcolor="#FFFFFF">
                                        <xsl:apply-templates mode="nav"/>
                                    </TD>
                                </TR>
                                <TR>
                                    <xsl:element name="TD">
                                        <xsl:attribute name="valign">top</xsl:attribute>
                                        <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/search_bg.gif</xsl:attribute>
                                        <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
                                        <xsl:attribute name="class">bodytext</xsl:attribute>
                                        <BR/>
                                        <TABLE width="100%" border="0" cellspacing="0" cellpadding="4">
                                            <TR>
                                                <TD width="15">
                                                    <A href="javascript:printThisPage();">
                                                        <xsl:element name="IMG">
                                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/icon_printer.gif</xsl:attribute>
                                                            <xsl:attribute name="width">15</xsl:attribute>
                                                            <xsl:attribute name="height">14</xsl:attribute>
                                                            <xsl:attribute name="border">0</xsl:attribute>
                                                        </xsl:element>
                                                    </A>
                                                </TD>
                                                <TD class="bodytext">
                                                    <A href="javascript:printThisPage();">print this page</A>
                                                </TD>
                                            </TR>
                                            <TR>
                                                <TD width="15">
                                                    <A href="javascript:emailThisPage();">
                                                        <xsl:element name="IMG">
                                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/icon_email.gif</xsl:attribute>
                                                            <xsl:attribute name="width">15</xsl:attribute>
                                                            <xsl:attribute name="height">14</xsl:attribute>
                                                            <xsl:attribute name="border">0</xsl:attribute>
                                                        </xsl:element>
                                                    </A>
                                                </TD>
                                                <TD class="bodytext">
                                                    <A href="javascript:emailThisPage();">email this page</A>
                                                </TD>
                                            </TR>
                                        </TABLE>
                                    </xsl:element>
                                </TR>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </FORM>
        </TD>
    </xsl:template>

    <xsl:template match="sitemap" mode="sitemap">
        <xsl:element name="SELECT">
            <xsl:attribute name="name">select</xsl:attribute>
            <xsl:attribute name="class">bodytext</xsl:attribute>
            <xsl:attribute name="onChange">gotoURL(this.options[selectedIndex].value);</xsl:attribute>
            <OPTION label="site-map" value="">site-map</OPTION>
            <xsl:apply-templates select="links" mode="sitemap"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="links" mode="sitemap">
        <xsl:element name="OPTGROUP">
            <xsl:attribute name="label"><xsl:value-of select="@title"/></xsl:attribute>
            <xsl:apply-templates select="link" mode="sitemap"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="link" mode="sitemap">
        <xsl:element name="OPTION">
            <xsl:attribute name="label"><xsl:value-of select="text()"/></xsl:attribute>
            <xsl:attribute name="value"><xsl:value-of select="$rootpath"/>/<xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:value-of select="text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="links[@include]" mode="nav">
        <xsl:apply-templates select="document(@include)" mode="nav"/>
    </xsl:template>

    <xsl:template match="links[link]|links[abslink]" mode="header">
        <TR>
            <TD colspan="2">
                <TABLE width="100%" border="0" cellspacing="0" cellpadding="8">
                    <TR>
                        <TD class="menuoptions">
                            <xsl:apply-templates mode="header"/>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
    </xsl:template>

    <xsl:template match="links[link]|links[abslink]" mode="nav">
        <P class="menuheader">
            <xsl:value-of select="@title"/>
            <DIV class="menuoptions">
                <xsl:apply-templates mode="nav"/>
            </DIV>
        </P>
    </xsl:template>

    <xsl:template match="links[news]|links[absnews]" mode="header">
        <xsl:message terminate="yes">
            "news/absnews" in header not supported
        </xsl:message>
    </xsl:template>

    <xsl:template match="links[news]|links[absnews]" mode="nav">
        <P class="menuheader" valign="top">
            <xsl:value-of select="@title"/>
            <DIV class="quotetext">
                <xsl:apply-templates mode="nav"/>
            </DIV>
        </P>
    </xsl:template>

    <xsl:template match="link[position()!=last()]" mode="header">
        <xsl:element name="A">
             <xsl:attribute name="href"><xsl:value-of select="$rootpath"/>/<xsl:value-of select="@ref"/></xsl:attribute>
             <xsl:value-of select="text()"/>
        </xsl:element>
        <xsl:element name="IMG">
            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/menu_spacer.gif</xsl:attribute>
            <xsl:attribute name="width">11</xsl:attribute>
            <xsl:attribute name="height">10</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="link[position()=last()]" mode="header">
        <xsl:element name="A">
             <xsl:attribute name="href"><xsl:value-of select="$rootpath"/>/<xsl:value-of select="@ref"/></xsl:attribute>
             <xsl:value-of select="text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="link" mode="nav">
        <xsl:element name="A">
             <xsl:attribute name="href"><xsl:value-of select="$rootpath"/>/<xsl:value-of select="@ref"/></xsl:attribute>
             <xsl:value-of select="text()"/>
        </xsl:element>
        <BR/>
    </xsl:template>

    <xsl:template match="abslink[position()!=last()]" mode="header">
        <xsl:element name="A">
            <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:attribute name="target"><xsl:value-of select="@target"/></xsl:attribute>
            <xsl:value-of select="text()"/>
        </xsl:element>
        <xsl:element name="IMG">
            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/menu_spacer.gif</xsl:attribute>
            <xsl:attribute name="width">11</xsl:attribute>
            <xsl:attribute name="height">10</xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="abslink[position()=last()]" mode="header">
        <xsl:element name="A">
            <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:attribute name="target"><xsl:value-of select="@target"/></xsl:attribute>
            <xsl:value-of select="text()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="abslink" mode="nav">
        <xsl:element name="A">
            <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:value-of select="text()"/>
        </xsl:element>
        <BR/>
    </xsl:template>

    <xsl:template match="news" mode="header">
        <xsl:message terminate="yes">
            "news" in header not supported
        </xsl:message>
    </xsl:template>

    <xsl:template match="news" mode="nav">
        <xsl:element name="A">
            <xsl:attribute name="href"><xsl:value-of select="$rootpath"/>/<xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:attribute name="class">newsflash</xsl:attribute>
            <xsl:attribute name="style">text-decoration: none</xsl:attribute>
            <xsl:value-of select="text()"/>
        </xsl:element>
        <BR/>
    </xsl:template>

    <xsl:template match="absnews" mode="header">
        <xsl:message terminate="yes">
            "absnews" in header not supported
        </xsl:message>
    </xsl:template>

    <xsl:template match="absnews" mode="nav">
        <xsl:element name="A">
            <xsl:attribute name="href"><xsl:value-of select="@ref"/></xsl:attribute>
            <xsl:attribute name="class">newsflash</xsl:attribute>
            <xsl:attribute name="style">text-decoration: none</xsl:attribute>
            <xsl:value-of select="text()"/>
        </xsl:element>
        <BR/>
    </xsl:template>

    <xsl:template match="separator" mode="header">
    </xsl:template>

    <xsl:template match="separator" mode="nav">
    </xsl:template>

    <xsl:template match="spacer" mode="header">
    </xsl:template>

    <xsl:template match="spacer" mode="nav">
    </xsl:template>

    <xsl:template match="area">
        <TR>
            <TD width="537" valign="top">
                <TABLE width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="9E9E9E">
                    <TR>
                        <TD height="57">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <TR>
                                    <xsl:element name="TD">
                                        <xsl:attribute name="height">8</xsl:attribute>
                                        <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/header_grid.gif</xsl:attribute>
                                        <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
                                        <xsl:element name="IMG">
                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                            <xsl:attribute name="width">6</xsl:attribute>
                                            <xsl:attribute name="height">10</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </TR>
                            </TABLE>
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="8">
                                <TR>
                                    <TD valign="top" bgcolor="#FFFFFF">
                                        <P class="menuheader">
                                           <xsl:value-of select="@title"/>
                                           <DIV class="bodytext">
                                               <xsl:apply-templates/>
                                           </DIV>
                                        </P>
                                    </TD>
                                </TR>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <xsl:element name="TD">
                <xsl:attribute name="height">8</xsl:attribute>
                <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
            </xsl:element>
        </TR>
    </xsl:template>

    <xsl:template match="area-left">
        <TR>
            <TD width="365" valign="top">
                <TABLE width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="9E9E9E">
                    <TR>
                        <TD height="57">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <TR>
                                    <xsl:element name="TD">
                                        <xsl:attribute name="height">8</xsl:attribute>
                                        <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/header_grid.gif</xsl:attribute>
                                        <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
                                        <xsl:element name="IMG">
                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                            <xsl:attribute name="width">6</xsl:attribute>
                                            <xsl:attribute name="height">10</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </TR>
                            </TABLE>
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="8">
                                <TR>
                                    <TD valign="top" bgcolor="#FFFFFF">
                                        <P class="menuheader">
                                           <xsl:value-of select="@title"/>
                                           <DIV class="bodytext">
                                               <xsl:apply-templates/>
                                           </DIV>
                                        </P>
                                    </TD>
                                </TR>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <xsl:element name="TD">
                <xsl:attribute name="height">8</xsl:attribute>
                <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
            </xsl:element>
        </TR>
    </xsl:template>

    <xsl:template match="area-right">
        <TR>
            <TD width="172" valign="top">
                <TABLE width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="9E9E9E">
                    <TR>
                        <TD height="57">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <TR>
                                    <xsl:element name="TD">
                                        <xsl:attribute name="height">8</xsl:attribute>
                                        <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/header_grid.gif</xsl:attribute>
                                        <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
                                        <xsl:element name="IMG">
                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                            <xsl:attribute name="width">6</xsl:attribute>
                                            <xsl:attribute name="height">10</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </TR>
                            </TABLE>
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="8">
                                <TR>
                                    <TD valign="top" bgcolor="#FFFFFF">
                                        <P class="menuheader">
                                           <xsl:value-of select="@title"/>
                                           <DIV class="bodytext">
                                               <xsl:apply-templates/>
                                           </DIV>
                                        </P>
                                    </TD>
                                </TR>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <xsl:element name="TD">
                <xsl:attribute name="height">8</xsl:attribute>
                <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
            </xsl:element>
        </TR>
    </xsl:template>

    <xsl:template match="area-right-blue">
        <TR>
            <TD width="172" valign="top">
                <TABLE width="100%" border="0" cellpadding="0" cellspacing="1" bgcolor="#9E9E9E">
                    <TR>
                        <TD height="57">
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="0">
                                <TR>
                                    <xsl:element name="TD">
                                        <xsl:attribute name="height">8</xsl:attribute>
                                        <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/header_grid.gif</xsl:attribute>
                                        <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
                                        <xsl:element name="IMG">
                                            <xsl:attribute name="src"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                                            <xsl:attribute name="width">6</xsl:attribute>
                                            <xsl:attribute name="height">10</xsl:attribute>
                                        </xsl:element>
                                    </xsl:element>
                                </TR>
                            </TABLE>
                            <TABLE width="100%" border="0" cellspacing="0" cellpadding="8">
                                <TR>
                                    <TD valign="top" bgcolor="#ECECEC">
                                        <P class="menuheader">
                                           <xsl:value-of select="@title"/>
                                           <DIV class="bodytext">
                                               <xsl:apply-templates/>
                                           </DIV>
                                        </P>
                                    </TD>
                                </TR>
                            </TABLE>
                        </TD>
                    </TR>
                </TABLE>
            </TD>
        </TR>
        <TR>
            <xsl:element name="TD">
                <xsl:attribute name="height">8</xsl:attribute>
                <xsl:attribute name="background"><xsl:value-of select="$rootpath"/>/images/look_and_feel/trans_spacer.gif</xsl:attribute>
                <xsl:attribute name="bgcolor">#FFFFFF</xsl:attribute>
            </xsl:element>
        </TR>
    </xsl:template>

    <xsl:template match="area/@text">
        <SPAN class="bodytext">
            <xsl:value-of select="text()"/>
        </SPAN>
    </xsl:template>

    <xsl:template match="area//html//*">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="area-left/@text">
        <SPAN class="bodytext">
            <xsl:value-of select="text()"/>
        </SPAN>
    </xsl:template>

    <xsl:template match="area-left//html//*">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="area-right/@text">
        <SPAN class="bodytext">
            <xsl:value-of select="text()"/>
        </SPAN>
    </xsl:template>

    <xsl:template match="area-right//html//*">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="area-right-blue/@text">
        <SPAN class="bodytext">
            <xsl:value-of select="text()"/>
        </SPAN>
    </xsl:template>

    <xsl:template match="area-right-blue//html//*">
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template match="footer">
        <TR>
            <TD width="727" valign="top" align="right" colspan="3">
                <FONT style="font-family: Arial, Helvetica, sans-serif" size="1">

                    Copyright 2002-2005 Arjuna Technologies

                    <br/>Copyright 2005-2008 JBoss Inc.

                    <br/>All Rights Reserved.

                    <br/><a class="linkscoloured" href="http://www.jboss.org/jbosstm/">http://www.jboss.org/jbosstm/</a>
                </FONT>
            </TD>
        </TR>
    </xsl:template>
</xsl:stylesheet>
