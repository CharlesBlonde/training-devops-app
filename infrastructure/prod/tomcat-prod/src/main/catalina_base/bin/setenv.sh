CATALINA_BASE=/home/ec2-user/tomcat
CATALINA_HOME=/opt/tomcat/tomcat6
CATALINA_PID=$CATALINA_BASE/tomcat.pid

export CATALINA_BASE CATALINA_HOME CATALINA_PID;

CATALINA_OPTS="-Xmx256m -XX:MaxPermSize=128m"

#CATALINA_NEW_RELIC_OPTS="-Dnewrelic.environment=production -javaagent:$CATALINA_BASE/lib/newrelic-2.10.0.jar"
CATALINA_APPDYNAMICS="-javaagent:/opt/appdynamics/javaagent.jar -Dappdynamics.agent.applicationName=TRAINING-DEVOPS -Dappdynamics.agent.tierName=WEB -Dappdynamics.agent.nodeName=JVM-PROD -Dappdynamics.controller.hostName=xebia-lab.saas.appdynamics.com -Dappdynamics.controller.port=80 -Dappdynamics.agent.accountName=xebia-lab -Dappdynamics.agent.accountAccessKey=7fb9c37ae8e6 "

CATALINA_JMX_OPTS="\
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=1099 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.authenticate=false"

## -Dcatalina_base is used by logback
CATALINA_OPTS="$CATALINA_OPTS $CATALINA_JMX_OPTS $CATALINA_APPDYNAMICS -Dcatalina_base=$CATALINA_BASE"

export CATALINA_OPTS;

