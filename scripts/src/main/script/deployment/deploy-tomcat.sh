#!/usr/bin/env bash

if [[ $# == 0 || $# > 2 ]];
then
   echo "ERROR: illegal number of arguments ($#), expected at least 1 parameter "
   echo "2nd param : artifact version (version like '1.0.0' or '1.1.0-SNAPSHOT' or keyword 'LATEST'). Optional, default value is 'LATEST'"
   exit 1
fi

# 'sso', 'www', 'beta' or 'api'
APP_IDENTIFIER=$1
if [ "$APP_IDENTIFIER" == "sso" ]; then
   # tomcat
   LINUX_SERVICE=tomcat-sso
   CATALINA_BASE=/data/tomcat-sso
   # artifact
   GROUP_ID=com.snbl.sso
   ARTIFACT_ID="snbl-sso-server"
   HEALT_CHECK_URL="http://localhost:8082/healthcheck.jsp"
elif [ "$APP_IDENTIFIER" == "www" ]; then
   # tomcat
   LINUX_SERVICE=tomcat-www
   CATALINA_BASE=/data/tomcat-www
   # artifact
   GROUP_ID=com.snbl
   ARTIFACT_ID="snbl-front"
   HEALT_CHECK_URL="http://localhost:8080/healthcheck.jsp"
elif [ "$APP_IDENTIFIER" == "beta" ]; then
   # tomcat
   LINUX_SERVICE=tomcat-beta
   CATALINA_BASE=/data/tomcat-beta
   # artifact
   GROUP_ID=com.snbl
   ARTIFACT_ID="snbl-front"
   HEALT_CHECK_URL="http://localhost:8083/healthcheck.jsp"
elif [ "$APP_IDENTIFIER" == "api" ]; then
   # tomcat
   LINUX_SERVICE=tomcat-api
   CATALINA_BASE=/data/tomcat-api
   # artifact
   GROUP_ID=com.snbl
   ARTIFACT_ID="snbl-imm"
   HEALT_CHECK_URL="http://localhost:8081/healthcheck.jsp"
else
   echo "Invalid identifier $APP_IDENTIFIER, expected ['sso', 'www', 'api']"
   exit 1
fi

VERSION=${2:-"LATEST"}

TMP_DIR=/tmp/deploy-$APP_IDENTIFIER

if [ -d $TMP_DIR ];
then
   rm -rf "$TMP_DIR"
fi
mkdir $TMP_DIR


# download new war version
echo "Download '$GROUP_ID:$ARTIFACT_ID:$VERSION':war ..."
mvn org.apache.maven.plugins:maven-dependency-plugin:2.5:get -DremoteRepositories=snowball-snapshot::default::https://repository-snowball.forge.cloudbees.com/snapshot/,snowball-release::default::https://repository-snowball.forge.cloudbees.com/release/ -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION:war -Ddest=$TMP_DIR/$ARTIFACT_ID-$VERSION.war

if [ "$?" !=  0 ];
then
   echo "mvn download failed with return code $?"
   exit -1
fi

echo "Shutdown tomcat server '$LINUX_SERVICE' ..."

# shutdown tomcat
sudo service $LINUX_SERVICE stop

echo "Uninstall webapp from tomcat server '$LINUX_SERVICE' ..."

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

echo "Install webapp '$TMP_DIR/$ARTIFACT_ID-$VERSION.war' on tomcat server '$LINUX_SERVICE' ..."

cp $TMP_DIR/$ARTIFACT_ID-$VERSION.war $CATALINA_BASE/webapps/ROOT.war

# start tomcat
echo "Start Tomcat server '$LINUX_SERVICE' ..."
sudo service $LINUX_SERVICE start

for ((  i = 5 ;  i > 0;  i--  ))
do
	echo "Wait $(expr $i \* 5) seconds for application startup ..."
	sleep 5

	# test
	echo "Test $ARTIFACT_ID:$VERSION:war on '$HEALT_CHECK_URL'..."
	HEALTH_CHECK_HTTP_CODE=$(curl --connect-timeout 10 --retry 10 --silent --show-error -w "%{http_code}" -o /dev/null $HEALT_CHECK_URL)
	if [ $HEALTH_CHECK_HTTP_CODE != 200 ];
	then
		if [ ${i} == 0 ];
		then
			echo "FAILURE: '$GROUP_ID:$ARTIFACT_ID:$VERSION' deployed on tomcat server but health check '$HEALT_CHECK_URL' is KO (returned '$HEALTH_CHECK_HTTP_CODE')"
	   		exit 1
	   	fi
	else
		break
	fi
done

echo "SUCCESS: $HOSTNAME - '$GROUP_ID:$ARTIFACT_ID:$VERSION' deployed and available on tomcat server '$LINUX_SERVICE'"
