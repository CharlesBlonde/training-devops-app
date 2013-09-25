#!/usr/bin/env bash

if [[ $# == 0 || $# > 2 ]];
then
   echo "ERROR: illegal number of arguments ($#), expected 0 or 1 parameter "
   echo "1st param : artifact version (version like '1.0.0' or '1.1.0-SNAPSHOT' or keyword 'LATEST'). Optional, default value is 'LATEST'"
   exit 1
fi

CATALINA_BASE=/home/ec2-user/tomcat
GROUP_ID=fr.xebia.training.devops.app
ARTIFACT_ID="devops-app-webapp"
HEALT_CHECK_URL="http://localhost:8080/healthcheck.jsp"

VERSION=${1:-"LATEST"}

TMP_DIR=/tmp/deploy-$ARTIFACT_ID

if [ -d $TMP_DIR ];
then
   rm -rf "$TMP_DIR"
fi
mkdir $TMP_DIR


# download new war version
echo "Download '$GROUP_ID:$ARTIFACT_ID:$VERSION':war ..."
#mvn org.apache.maven.plugins:maven-dependency-plugin:2.5:get -DremoteRepositories=atelier-xebia-snapshot::default::https://repository-atelier-xebia.forge.cloudbees.com/snapshot/,atelier-xebia-release::default::https://repository-atelier-xebia.forge.cloudbees.com/release/ -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION:war -Ddest=$TMP_DIR/$ARTIFACT_ID-$VERSION.war
mvn org.apache.maven.plugins:maven-dependency-plugin:2.5:get -DremoteRepositories=xebia-france-snapshot::default::https://repository-xebia-france.forge.cloudbees.com/snapshot/,atelier-xebia-release::default::https://repository-xebia-france.forge.cloudbees.com/release/ -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION:war -Ddest=$TMP_DIR/$ARTIFACT_ID-$VERSION.war

if [ "$?" !=  0 ];
then
   echo "mvn download failed with return code $?"
   exit -1
fi

echo "Shutdown tomcat server '$CATALINA_BASE/bin/catalina.sh stop' ..."

# shutdown tomcat
$CATALINA_BASE/bin/catalina.sh stop
sleep 5
echo "should replace 'catalina.sh stop' + sleep 5 by linux 'service tomcat-xxx stop' and an underlying 'kill -9'"

echo "Uninstall webapp from tomcat server '$CATALINA_BASE' ..."

# cleanup
if [ -f $CATALINA_BASE/webapps/ROOT.war ];
then
   rm -f "$CATALINA_BASE/webapps/ROOT.war"
fi
if [ -d $CATALINA_BASE/webapps/ROOT ];
then
   rm -rf "$CATALINA_BASE/webapps/ROOT"
fi

if [ -d $CATALINA_BASE/work/Catalina/ ]; then
   rm -rf $CATALINA_BASE/work/Catalina/
fi

echo "Install webapp '$TMP_DIR/$ARTIFACT_ID-$VERSION.war' on tomcat server '$CATALINA_BASE/webapps/' ..."

if [ ! -d $CATALINA_BASE/webapps/ ];
then
   mkdir "$CATALINA_BASE/webapps/"
fi

cp $TMP_DIR/$ARTIFACT_ID-$VERSION.war $CATALINA_BASE/webapps/ROOT.war

# start tomcat
echo "Start Tomcat server '$CATALINA_BASE/bin/catalina.sh start' ..."
$CATALINA_BASE/bin/catalina.sh start

for ((  i = 5 ;  i > 0;  i--  ))
do
	echo "Wait up to $(expr $i \* 5) seconds for application startup ..."
	sleep 5

	# test
	echo "Test $ARTIFACT_ID:$VERSION:war on '$HEALT_CHECK_URL'..."
	HEALTH_CHECK_HTTP_CODE=$(curl --connect-timeout 5 --silent --show-error -w "%{http_code}" -o /dev/null $HEALT_CHECK_URL)
	if [ $HEALTH_CHECK_HTTP_CODE == 200 ];
	then
        echo "SUCCESS: $HOSTNAME - '$GROUP_ID:$ARTIFACT_ID:$VERSION' deployed and available on tomcat server '$CATALINA_BASE'"
        exit 0
	else
		# try a bit more
		echo ""
	fi
done

echo "FAILURE: '$GROUP_ID:$ARTIFACT_ID:$VERSION' deployed on tomcat server but health check '$HEALT_CHECK_URL' is KO (returned '$HEALTH_CHECK_HTTP_CODE')"
exit 1