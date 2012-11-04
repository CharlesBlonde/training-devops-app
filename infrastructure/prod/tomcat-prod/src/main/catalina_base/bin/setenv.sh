CATALINA_BASE=/opt/tomcat-prod-ready-app
CATALINA_HOME=/opt/apache-tomcat-6.0.35
CATALINA_PID=$CATALINA_BASE/tomcat.pid

export CATALINA_BASE CATALINA_HOME CATALINA_PID;

CATALINA_OPTS="-Xmx324m -XX:MaxPermSize=128m"

CATALINA_JMX_OPTS="\
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=1099 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.authenticate=false"

## -Dcatalina_base is used by logback
CATALINA_OPTS="$CATALINA_OPTS $CATALINA_JMX_OPTS -Dcatalina_base=$CATALINA_BASE"

export CATALINA_OPTS;

