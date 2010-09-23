CATALINA_OPTS_JMX=" \
   -Dcom.sun.management.jmxremote.port=6969 \
   -Dcom.sun.management.jmxremote.ssl=false \
   -Dcom.sun.management.jmxremote.authenticate=false"
	

CATALINA_OPTS="
   -server $CATALINA_OPTS \
   -Dorg.apache.juli.logging.diagnostics.dest=STDOUT \
   $CATALINA_OPTS_JMX \
   $CATALINA_OPTS_JVM"

export CATALINA_OPTS;