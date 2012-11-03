CATALINA_BASE=/opt/tomcat-prod-ready-app
CATALINA_HOME=/opt/apache-tomcat-6.0.35
CATALINA_PID=$CATALINA_BASE/tomcat.pid

export CATALINA_BASE CATALINA_HOME CATALINA_PID;

JAVA_OPTS="-Xmx256m -XX:MaxPermSize=128m"

JAVA_JMX_OPTS="\
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=1099 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.authenticate=false"

## -Dcatalina_base is used by logback
JAVA_OPTS="$JAVA_OPTS $JAVA_JMX_OPTS -Dcatalina_base=$CATALINA_BASE"

export JAVA_OPTS;

