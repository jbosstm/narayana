<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html>

<!--
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 * (C) 2007, 2009 @author JBoss Inc
 -->

<head>
    <title>JBossTS Transaction Bridge Demo</title>
</head>

<body style="margin-left: 10pt">

    <h1>JBossTS Transaction Bridge Demo</h1>

    <form method="GET" action="basicclient">

        <% if(null != request.getAttribute("result")) { %>
        <!-- tx result panel -->
        <p>Transaction Result: <%= request.getAttribute("result") %></p>
        <% } // end if %>

        <p>Number of seats to book:
            <select name="seats">
                <option>1</option>
                <option>2</option>
                <option>3</option>
                <option>4</option>
                <option>5</option>
                <option>6</option>
                <option>7</option>
                <option>8</option>
                <option>9</option>
                <option>10</option>
            </select>
        </p>

        <p>Parent Transaction Type:
            <select name="txType">
                <option value="AtomicTransaction">WS-AT</option>
                <option value="JTA">JTA (requires XTS demo services)</option>
            </select>
        </p>

        <p>
            <input type="submit" name="submit" value="Submit Booking" />
        </p>

    </form>

</body>

</html>

