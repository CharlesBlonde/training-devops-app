<%@ page import="javax.management.*"%>
<%@ page import="java.io.*,java.util.*"%>
<html>
<head>
<title>Jakarta Commons DBCP Data Sources</title>
</head>
<body>
<h1>Jakarta Commons DBCP Data Sources</h1>
<table border='1'>
   <tr>
      <th>ObjectName</th>
      <th>NumActive</th>
      <th>NumIdle</th>
   </tr>
   <%
       // get the mbean server
       MBeanServer mbeanServer = MBeanServerFactory.findMBeanServer(null).get(0);
       // get the mbeans
       Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(new ObjectName("javax.sql:type=DataSource,*"), null);
       // render the mbeans
       for (ObjectInstance objectInstance : objectInstances) {
           ObjectName objectName = objectInstance.getObjectName();
   %>
   <tr>
      <td><%=objectName%></td>
      <td><%=mbeanServer.getAttribute(objectName, "NumActive")%></td>
      <td><%=mbeanServer.getAttribute(objectName, "NumIdle")%></td>
   </tr>
   <%
       }
   %>
</table>
</body>
</html>