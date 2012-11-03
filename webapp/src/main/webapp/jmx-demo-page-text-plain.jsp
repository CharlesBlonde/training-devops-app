<%@ page contentType="text/plain;charset=ISO-8859-1" import="javax.management.*,java.io.*,java.util.*"%><%
    final String SEPARATOR = "\t";

    out.println("ObjectName" + SEPARATOR + "NumActive" + SEPARATOR + "NumIdle");

    // get the mbean server
    MBeanServer mbeanServer = MBeanServerFactory.findMBeanServer(null).get(0);

    // get the mbeans
    Set<ObjectInstance> objectInstances = mbeanServer.queryMBeans(new ObjectName("javax.sql:type=DataSource,*"), null);

    // render the mbeans
    for (ObjectInstance objectInstance : objectInstances) {
        ObjectName objectName = objectInstance.getObjectName();
        Object numActive = mbeanServer.getAttribute(objectName, "NumActive");
        Object numIdle = mbeanServer.getAttribute(objectName, "NumIdle");
        out.println(objectName + SEPARATOR + numActive + SEPARATOR + numIdle);
    }
%>