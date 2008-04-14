<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" "http://www.w3.org/TR/REC-html40/loose.dtd">
<html>

<!-- $Id: index.jsp,v 1.6 2005/02/25 09:26:46 kconner Exp $ -->

<head>
<title>XML Transaction Service Demo Application Client</title>
</head>

<body topmargin="0" leftmargin="0" marginwidth="0" marginheight="0" vlink="#336699" alink="#003366" link="#003366" text="#000000" bgcolor="#ffffff">

<!-- logo and header text -->
<table width="740" cellpadding="0" cellspacing="0" border="0">
  <tr>
    <td colspan="2" height="28">&nbsp;</td>
  </tr>
  <tr>
    <td width="20">&nbsp;</td>
    <td align="center" width="170" bgcolor="#ffffff">
      <img src="images/JBoss_DivOfRH_RGB.gif" alt="jboss logo" border="0">
    </td>
    <td width="20">&nbsp;</td>
    <td valign="middle" align="left" width="530" bgcolor="#ffffff">
      <br/>
      <font size="5" style="font-family: Arial, Helvetica, sans-serif">
        XML Transaction Service Demonstrator Application
      </font>
    </td>
  </tr>
</table>


<TABLE width="740" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD width="20">&nbsp;</TD><TD valign="top" width="170">

<!-- nav menu -->
<TABLE width="170" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD height="20" width="10" bgcolor="#ffffff">&nbsp;</TD><TD height="20" width="10" bgcolor="#ffffff">&nbsp;</TD><TD height="20" width="130" bgcolor="#ffffff">&nbsp;</TD><TD height="20" width="10" bgcolor="#ffffff">&nbsp;</TD><TD height="20" width="10" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD colspan="2" valign="top" align="left" height="20" width="20" bgcolor="#336699"><IMG src="images/tl_navcorner_20.gif" alt="" border="0"></TD><TD align="center" height="20" width="130" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Web Services</B></FONT></TD><TD colspan="2" valign="top" align="right" height="20" width="20" bgcolor="#336699"><IMG src="images/tr_navcorner_20.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD valign="middle" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/a_nav_6699cc.gif" name="N800004" alt="" border="0"></TD><TD colspan="3" width="150" bgcolor="#e3e3e3"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><A href="#Restaurant"  style="text-decoration: none">Restaurant Booking Service</A></FONT></TD><TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="middle" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/a_nav_6699cc.gif" name="N800004" alt="" border="0"></TD><TD colspan="3" width="150" bgcolor="#e3e3e3"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><A href="#Theatre"  style="text-decoration: none">Theatre Booking Service</A></FONT></TD><TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="middle" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/a_nav_6699cc.gif" name="N800004" alt="" border="0"></TD><TD colspan="3" width="150" bgcolor="#e3e3e3"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><A href="#Taxi"  style="text-decoration: none">Taxi Booking Service</A></FONT></TD><TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD colspan="2" valign="bottom" align="left" height="20" width="20" bgcolor="#e3e3e3"><IMG src="images/bl_navcorner_20.gif" alt="" border="0"></TD><TD height="20" width="130" bgcolor="#e3e3e3">&nbsp;</TD><TD colspan="2" valign="bottom" align="right" height="20" width="20" bgcolor="#e3e3e3"><IMG src="images/br_navcorner_20.gif" alt="" border="0"></TD>
</TR>
</table>

<p></p>
</TD><TD width="20">&nbsp;</TD><TD valign="top" width="530">

<form method="GET" action="basicclientrpc">

<% if(null != request.getAttribute("result")) { %>
<!-- tx result panel -->
<TABLE width="530" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD colspan="3" height="20" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD valign="top" align="left" width="10" bgcolor="#336699"><IMG src="images/tl_corner_10.gif" alt="" border="0"></TD><TD valign="middle" align="left" width="510" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Transaction Result</B></FONT></TD><TD valign="top" align="right" width="10" bgcolor="#336699"><IMG src="images/tr_corner_10.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD><TD width="510" bgcolor="#e3e3e3">
<FONT size="2" style="font-family: Arial, Helvetica, sans-serif">
<div>
<p>
<%= request.getAttribute("result") %>
</p>
</FONT></TD>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="bottom" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/bl_corner_10.gif" alt="" border="0"></TD><TD width="510" bgcolor="#e3e3e3">&nbsp;</TD><TD valign="bottom" align="right" width="10" bgcolor="#e3e3e3"><IMG src="images/br_corner_10.gif" alt="" border="0"></TD>
</TR>
</TABLE>
<% } // end if %>


<!-- transaction type selection panel -->
<TABLE width="530" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD colspan="3" height="20" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD valign="top" align="left" width="10" bgcolor="#336699"><IMG src="images/tl_corner_10.gif" alt="" border="0"></TD><TD valign="middle" align="left" width="510" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Restaurant Service - Booking Form</B></FONT></TD><TD valign="top" align="right" width="10" bgcolor="#336699"><IMG src="images/tr_corner_10.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD><TD width="510" bgcolor="#e3e3e3">
<FONT size="2" style="font-family: Arial, Helvetica, sans-serif">
<div>
<p>
Transaction Type:
<SELECT NAME="txType">
<option value="AtomicTransaction">Atomic Transaction</option>
<option value="BusinessActivity">Business Activity</option>
</SELECT>
</p>
</FONT></TD>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="bottom" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/bl_corner_10.gif" alt="" border="0"></TD><TD width="510" bgcolor="#e3e3e3">&nbsp;</TD><TD valign="bottom" align="right" width="10" bgcolor="#e3e3e3"><IMG src="images/br_corner_10.gif" alt="" border="0"></TD>
</TR>
</TABLE>


<!-- restaurant booking panel -->
<TABLE width="530" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD colspan="3" height="20" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD valign="top" align="left" width="10" bgcolor="#336699"><IMG src="images/tl_corner_10.gif" alt="" border="0"></TD><TD valign="middle" align="left" width="510" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Restaurant Service - Booking Form</B></FONT></TD><TD valign="top" align="right" width="10" bgcolor="#336699"><IMG src="images/tr_corner_10.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD><TD width="510" bgcolor="#e3e3e3">
<FONT size="2" style="font-family: Arial, Helvetica, sans-serif">
<div>
<p>
Table for
<SELECT NAME="restaurant">
<OPTION>1
<OPTION>2
<OPTION>3
<OPTION>4
<OPTION>5
<OPTION>6
<OPTION>7
<OPTION>8
<OPTION>9
<OPTION>10
</SELECT>
people.
</p>
</FONT></TD>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="bottom" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/bl_corner_10.gif" alt="" border="0"></TD><TD width="510" bgcolor="#e3e3e3">&nbsp;</TD><TD valign="bottom" align="right" width="10" bgcolor="#e3e3e3"><IMG src="images/br_corner_10.gif" alt="" border="0"></TD>
</TR>
</TABLE>

<!-- theatre booking panel -->
<TABLE width="530" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD colspan="3" height="20" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD valign="top" align="left" width="10" bgcolor="#336699"><IMG src="images/tl_corner_10.gif" alt="" border="0"></TD><TD valign="middle" align="left" width="510" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Theatre Service - Booking Form</B></FONT></TD><TD valign="top" align="right" width="10" bgcolor="#336699"><IMG src="images/tr_corner_10.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD><TD width="510" bgcolor="#e3e3e3">
<FONT size="2" style="font-family: Arial, Helvetica, sans-serif">
<div>
<p>
Book
<SELECT NAME="theatrecount">
<OPTION>1
<OPTION>2
<OPTION>3
<OPTION>4
<OPTION>5
<OPTION>6
<OPTION>7
<OPTION>8
<OPTION>9
<OPTION>10
</SELECT>
seats in the
<select name="theatrearea">
<option value="0">Circle
<option value="1">Stalls
<option value="2">Balcony
</select>
</p>
</FONT></TD>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="bottom" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/bl_corner_10.gif" alt="" border="0"></TD><TD width="510" bgcolor="#e3e3e3">&nbsp;</TD><TD valign="bottom" align="right" width="10" bgcolor="#e3e3e3"><IMG src="images/br_corner_10.gif" alt="" border="0"></TD>
</TR>
</TABLE>

<!-- taxi booking panel -->
<TABLE width="530" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD colspan="3" height="20" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD valign="top" align="left" width="10" bgcolor="#336699"><IMG src="images/tl_corner_10.gif" alt="" border="0"></TD><TD valign="middle" align="left" width="510" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Taxi Service - Booking Form</B></FONT></TD><TD valign="top" align="right" width="10" bgcolor="#336699"><IMG src="images/tr_corner_10.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD><TD width="510" bgcolor="#e3e3e3">
<FONT size="2" style="font-family: Arial, Helvetica, sans-serif">
<div>
<p>
Book a taxi?
<SELECT NAME="taxi">
<option value="0">No</option>
<option value="1">Yes</option>
</SELECT>
</p>
</FONT></TD>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="bottom" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/bl_corner_10.gif" alt="" border="0"></TD><TD width="510" bgcolor="#e3e3e3">&nbsp;</TD><TD valign="bottom" align="right" width="10" bgcolor="#e3e3e3"><IMG src="images/br_corner_10.gif" alt="" border="0"></TD>
</TR>
</TABLE>

<!-- submit / reset panel -->
<TABLE width="530" cellpadding="0" cellspacing="0" border="0">
<TR>
<TD colspan="3" height="20" bgcolor="#ffffff">&nbsp;</TD>
</TR>
<TR>
<TD valign="top" align="left" width="10" bgcolor="#336699"><IMG src="images/tl_corner_10.gif" alt="" border="0"></TD><TD valign="middle" align="left" width="510" bgcolor="#336699"><FONT size="2" color="#ffffff" style="font-family: Arial, Helvetica, sans-serif"><B>Booking Controls</B></FONT></TD><TD valign="top" align="right" width="10" bgcolor="#336699"><IMG src="images/tr_corner_10.gif" alt="" border="0"></TD>
</TR>
<TR>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD><TD width="510" bgcolor="#e3e3e3">
<FONT size="2" style="font-family: Arial, Helvetica, sans-serif">
<p>
<input type="submit" name="submit" value="Submit Booking Requests" />
&nbsp;&nbsp;
<input type="reset" name="reset" value="Reset Form Values" />
</p>
</FONT></TD>
<TD width="10" bgcolor="#e3e3e3">&nbsp;</TD>
</TR>
<TR>
<TD valign="bottom" align="left" width="10" bgcolor="#e3e3e3"><IMG src="images/bl_corner_10.gif" alt="" border="0"></TD><TD width="510" bgcolor="#e3e3e3">&nbsp;</TD><TD valign="bottom" align="right" width="10" bgcolor="#e3e3e3"><IMG src="images/br_corner_10.gif" alt="" border="0"></TD>
</TR>
</TABLE>

</form>

</table>

</body>

</html>

