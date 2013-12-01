CATALINA_BASE=/home/ec2-user/tomcat
CATALINA_HOME=/opt/tomcat/tomcat6
CATALINA_PID=$CATALINA_BASE/tomcat.pid
APPDYNAMICS_DIR=/opt/appdynamics

export CATALINA_BASE CATALINA_HOME CATALINA_PID;

CATALINA_OPTS="-Xmx256m -XX:MaxPermSize=128m"

#CATALINA_NEW_RELIC_OPTS="-Dnewrelic.environment=production -javaagent:$CATALINA_BASE/lib/newrelic-2.10.0.jar"
CATALINA_APPDYNAMICS=""
if [ -d ${APPDYNAMICS_DIR} ]
then
    JVMNAME=`hostname |tr '[:lower:]' '[:upper:]'`
    CATALINA_APPDYNAMICS="-javaagent:/opt/appdynamics/javaagent.jar -Dappdynamics.agent.runtime.dir=${CATALINA_BASE}/appdynamics -Dappdynamics.agent.applicationName=TRAINING-DEVOPS -Dappdynamics.agent.tierName=WEB -Dappdynamics.agent.nodeName=$JVMNAME -Dappdynamics.controller.hostName=xebia.saas.appdynamics.com -Dappdynamics.controller.port=80 -Dappdynamics.agent.accountName=xebia -Dappdynamics.agent.accountAccessKey=9333a7f71868 "
fi

EXTERNAL_IP=`curl -s http://169.254.169.254/latest/meta-data/public-ipv4`
CATALINA_JMX_OPTS="-Djava.rmi.server.hostname=${EXTERNAL_IP} \
    -Dcom.sun.management.jmxremote \
    -Dcom.sun.management.jmxremote.port=1099 \
    -Dcom.sun.management.jmxremote.ssl=false \
    -Dcom.sun.management.jmxremote.authenticate=false"

## -Dcatalina_base is used by logback
CATALINA_OPTS="$CATALINA_OPTS $CATALINA_JMX_OPTS $CATALINA_APPDYNAMICS -Dcatalina_base=$CATALINA_BASE"

export CATALINA_OPTS;

