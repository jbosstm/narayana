<html>

<head>
    <title>DTF for JBossTS</title>
</head>

<body>

<%@ page import="java.util.ArrayList, java.util.Arrays, java.util.Enumeration, java.net.URL" %>
<%@ page import="com.arjuna.mwlabs.testframework.dtfweb.*, com.arjuna.mwlabs.testframework.coordinator2.*, com.arjuna.mwlabs.testframework.coordinator2.scheduler.*" %>

<%
    // TODO: keep only the testId numbers here, lookup the corresponding names from the db?

    String[] jts_basicTestIds = new String[] { "66", "70", "71" };
    String[] jts_basicTestNames = new String[] { "Current Tests 01", "JTA Tests 01", "OTS Server Tests" };

    String[] jts_rrTestIds = new String[] { "84", "85", "86", "87", "88", "89" };
    String[] jts_rrTestNames = new String[] { "Raw Resources Tests 01-1", "Raw Resources Tests 01-2", "Raw Resources Tests 01-3",
                                        "Raw Resources Tests 02-1", "Raw Resources Tests 02-2", "Raw Resources Tests 02-3" };

    String[] jts_rstrTestIds = new String[] { "90", "91", "92", "93", "94", "95" };
    String[] jts_rstrTestNames = new String[] { "Raw Subtransaction Aware Resources Tests 01-1", "Raw Subtransaction Aware Resources Tests 01-2",
                                        "Raw Subtransaction Aware Resources Tests 01-3", "Raw Subtransaction Aware Resources Tests 02-1",
                                        "Raw Subtransaction Aware Resources Tests 02-2", "Raw Subtransaction Aware Resources Tests 02-3" };

    String[] jts_crTestIds = new String[] { "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "146" };
    String[] jts_crTestNames = new String[] { "Crash Recovery Tests 01", "Crash Recovery Tests 02-1", "Crash Recovery Tests 02-2",
                                        "Crash Recovery Tests 03", "Crash Recovery Tests 04", "Crash Recovery Tests 05-1",
                                        "Crash Recovery Tests 05-2", "Crash Recovery Tests 06", "Crash Recovery Tests 07",
                                        "Crash Recovery Tests 08", "Crash Recovery Tests 12" };

    String[] jts_jdbcTestIds = new String[] { "117", "138", "119", "147", "200", "204", "201", "205", "202", "206", "203", "207" };
    String[] jts_jdbcTestNames = new String[] { "JDBC Resources Tests 01 - Oracle Thin JNDI", "JDBC Resources Tests 02 - Oracle Thin JNDI",
                                        "JDBC Resources Tests 01 - MSSQLServer JNDI", "JDBC Resources Tests 02 - MSSQLServer JNDI",
                                        "JDBC Resources Tests 01 - IBM DB2 JNDI", "JDBC Resources Tests 02 - IBM DB2 JNDI",
                                        "JDBC Resources Tests 01 - PostgreSQL JNDI", "JDBC Resources Tests 02 - PostgreSQL JNDI",
                                        "JDBC Resources Tests 01 - MySQL JNDI", "JDBC Resources Tests 02 - MySQL JNDI",
                                        "JDBC Resources Tests 01 - Sybase JNDI", "JDBC Resources Tests 02 - Sybase JNDI" };


    // TODO 42 applies to JTS too?
    // TODO 43 - TxOJ should be here too?
    String[] jta_basicTestIds = new String[] { "42", "70" };
    String[] jta_basicTestNames = new String[] { "Transaction Core", "JTA Tests 01" };

    String[] jta_jdbcTestIds = new String[] { "16", "15", "208", "209", "210", "211" };
    String[] jta_jdbcTestNames = new String[] { "JDBC Local Tests 01 - Oracle JNDI", "JDBC Local Tests 01 - MSSQL Server JNDI",
                                        "JDBC Local Tests 01 - IBM DB2 JNDI", "JDBC Local Tests 01 - PostgreSQL JNDI",
                                        "JDBC Local Tests 01 - MySQL JNDI", "JDBC Local Tests 01 - Sybase JNDI" };


    String[] OS = new String[] { "RHEL4-32", "RHEL4-64", "RHEL5-32", "RHEL5-64", "WIN2003-64", "SOL10-SPARC", "HPUX11-IA64" };

    ArrayList allJTSTestIdsInRunOrder = new ArrayList();
    allJTSTestIdsInRunOrder.addAll(Arrays.asList(jts_basicTestIds));
    allJTSTestIdsInRunOrder.addAll(Arrays.asList(jts_rrTestIds));
    allJTSTestIdsInRunOrder.addAll(Arrays.asList(jts_rstrTestIds));
    allJTSTestIdsInRunOrder.addAll(Arrays.asList(jts_crTestIds));
    allJTSTestIdsInRunOrder.addAll(Arrays.asList(jts_jdbcTestIds));

    ArrayList allJTATestIdsInRunOrder = new ArrayList();
    allJTATestIdsInRunOrder.addAll(Arrays.asList(jta_basicTestIds));
    allJTATestIdsInRunOrder.addAll(Arrays.asList(jta_jdbcTestIds));


    String distributionList = "jonathan.halliday@redhat.com";

    int count = 0;
    for(int i = 0; i < allJTSTestIdsInRunOrder.size(); i++) {

        String softwareVersion = "JBossTS_JTS_JacORB_QA";

        for(int j = 0; j < OS.length; j++) {
            String value = request.getParameter("runJTSTest#"+ allJTSTestIdsInRunOrder.get(i)+"@"+OS[j]);
            if("on".equals(value)) {

                StoredTestDefs std = StoredTestDefs.getStoredTestDefs(Long.parseLong((String) allJTSTestIdsInRunOrder.get(i)));
                if(std == null) {
                    %> No testdef for <%= allJTSTestIdsInRunOrder.get(i) %> <%
                    continue;
                }

                StoredTestSelections testSelections = std.getTestSelection( "AJQ_"+OS[j] );

                URL testDefsURL = new URL(testSelections.getTestDefinitions().getURL());
                URL testSelectionsURL = new URL(testSelections.getURL());

                DeployInformation deployInfo = null;

                ScheduleWhenPossible scheduleEntry = new ScheduleWhenPossible(
                        testDefsURL,
                        testSelectionsURL,
                        distributionList,
                        softwareVersion,
                        deployInfo);

                testSelections.run(scheduleEntry);

                count++;
            }
        }
    }

    for(int i = 0; i < allJTATestIdsInRunOrder.size(); i++) {

        String softwareVersion = "JBossTS_JTA_QA";

        for(int j = 0; j < OS.length; j++) {
            String value = request.getParameter("runJTATest#"+ allJTATestIdsInRunOrder.get(i)+"@"+OS[j]);
            if("on".equals(value)) {

                StoredTestDefs std = StoredTestDefs.getStoredTestDefs(Long.parseLong((String) allJTATestIdsInRunOrder.get(i)));
                if(std == null) {
                    %> No testdef for <%= allJTATestIdsInRunOrder.get(i) %> <%
                    continue;
                }

                StoredTestSelections testSelections = std.getTestSelection( "JTAQ_"+OS[j] );

                URL testDefsURL = new URL(testSelections.getTestDefinitions().getURL());
                URL testSelectionsURL = new URL(testSelections.getURL());

                DeployInformation deployInfo = null;

                ScheduleWhenPossible scheduleEntry = new ScheduleWhenPossible(
                        testDefsURL,
                        testSelectionsURL,
                        distributionList,
                        softwareVersion,
                        deployInfo);

                testSelections.run(scheduleEntry);

                count++;
            }
        }
    }

    if(count > 0) {
        %> Scheduled <%= count %> runs <%
    }


/*    Long testId = Long.parseLong(request.getParameter("testId"));
    String selection = request.getParameter("selection");

    String distributionList = "jonathan.halliday@redhat.com";
    String softwareVersion = "JBossTS_JTS_JacORB_QA";

    StoredTestDefs std = StoredTestDefs.getStoredTestDefs(testId);
    StoredTestSelections testSelections = std.getTestSelection( selection );

	URL testDefsURL = new URL(testSelections.getTestDefinitions().getURL());
	URL testSelectionsURL = new URL(testSelections.getURL());

    DeployInformation deployInfo = null;

    ScheduleWhenPossible scheduleEntry = new ScheduleWhenPossible(
            testDefsURL,
            testSelectionsURL,
            distributionList,
            softwareVersion,
            deployInfo);
*/
    //testSelections.run(scheduleEntry);


%>

<a href="default.jsp">DTF Home</a>
-
<a href="default.jsp?page=schedule">Schedule</a>

<table border="1">
<form action="jbossts.jsp" method="POST">

    <tr>
        <td><b>DTF Tests for JBossTS JTS (JBossTS_JTS_JacORB_QA)</b></td>
    </tr>

    <tr>
        <td></td>
        <% for(int j = 0; j < OS.length; j++) { %>
            <td><%= OS[j] %></td>
        <% } %>
    </tr>

    <tr><td></td></tr>
    <tr>
        <td>Basic Tests</td>
    </tr>
    <% for(int i = 0; i < jts_basicTestIds.length; i++) { %>
        <tr>
            <td><%= jts_basicTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
                <td><input type="checkbox" name="runJTSTest#<%= jts_basicTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>

    <tr><td></td></tr>
    <tr>
        <td>Raw Resource Tests</td>
    </tr>
    <% for(int i = 0; i < jts_rrTestIds.length; i++) { %>
        <tr>
            <td><%= jts_rrTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
            <td><input type="checkbox" name="runJTSTest#<%= jts_rrTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>

    <tr><td></td></tr>
    <tr>
        <td>Raw Subtransaction Aware Resource Tests</td>
    </tr>
    <% for(int i = 0; i < jts_rstrTestIds.length; i++) { %>
        <tr>
            <td><%= jts_rstrTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
            <td><input type="checkbox" name="runJTSTest#<%= jts_rstrTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>

    <tr><td></td></tr>
    <tr>
        <td>Crash Recovery Tests</td>
    </tr>
    <% for(int i = 0; i < jts_crTestIds.length; i++) { %>
        <tr>
            <td><%= jts_crTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
            <td><input type="checkbox" name="runJTSTest#<%= jts_crTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>

    <tr><td></td></tr>
    <tr>
        <td>JDBC Tests</td>
    </tr>
    <% for(int i = 0; i < jts_jdbcTestIds.length; i++) { %>
        <tr>
            <td><%= jts_jdbcTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
            <td><input type="checkbox" name="runJTSTest#<%= jts_jdbcTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>


    <tr><td></td></tr>
    <tr>
        <td><b>DTF Tests for JBossTS JTA (JBossTS_JTA_QA)</b></td>
    </tr>
    <tr>
        <td></td>
        <% for(int j = 0; j < OS.length; j++) { %>
            <td><%= OS[j] %></td>
        <% } %>
    </tr>

    <tr><td></td></tr>
    <tr>
        <td>Basic Tests</td>
    </tr>
    <% for(int i = 0; i < jta_basicTestIds.length; i++) { %>
        <tr>
            <td><%= jta_basicTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
            <td><input type="checkbox" name="runJTATest#<%= jta_basicTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>

    <tr><td></td></tr>
    <tr>
        <td>JDBC Tests</td>
    </tr>
    <% for(int i = 0; i < jta_jdbcTestIds.length; i++) { %>
        <tr>
            <td><%= jta_jdbcTestNames[i] %></td>
            <% for(int j = 0; j < OS.length; j++) { %>
            <td><input type="checkbox" name="runJTATest#<%= jta_jdbcTestIds[i] %>@<%= OS[j] %>"/></td>
            <% } %>
        </tr>
    <% } %>


    <tr><td></td></tr>
    <tr>
        <td><input type="submit" value="schedule selected tests"/></td>
    </tr>

</form>
</table>

</body>
</html>