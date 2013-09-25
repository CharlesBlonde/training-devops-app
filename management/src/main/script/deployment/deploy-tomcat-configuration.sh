#!/usr/bin/env bash

if [[ $# == 0 || $# > 2 ]];
then
   echo "ERROR: illegal number of arguments ($#), expected at least 1 parameter "
   echo "1st param : app identifier ['localhost', 'integ', 'prod']"
   echo "2nd param : artifact version (version like '1.0.0' or '1.1.0-SNAPSHOT' or keyword 'LATEST'). Optional, default value is 'LATEST'"
   exit 1
fi

CATALINA_BASE=/home/ec2-user/tomcat
GROUP_ID=fr.xebia.training.devops.app
HEALT_CHECK_URL="http://localhost:8080/healthcheck.jsp"

# one of ["localhost", "integ", "prod"]
ENV_IDENTIFIER=$1
ARTIFACT_ID="devops-app-tomcat-$ENV_IDENTIFIER"

VERSION=${2:-"LATEST"}

TMP_DIR=/tmp/deploy-$ARTIFACT_ID

if [ -d $TMP_DIR ];
then
   rm -rf "$TMP_DIR"
fi
mkdir $TMP_DIR


# download new war version
echo "Download '$GROUP_ID:$ARTIFACT_ID:$VERSION':tgz ..."
#mvn org.apache.maven.plugins:maven-dependency-plugin:2.5:get -DremoteRepositories=atelier-xebia-snapshot::default::https://repository-atelier-xebia.forge.cloudbees.com/snapshot/,atelier-xebia-release::default::https://repository-atelier-xebia.forge.cloudbees.com/release/ -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION:tar.gz:distribution -Ddest=$TMP_DIR/$ARTIFACT_ID-$VERSION.tgz
mvn org.apache.maven.plugins:maven-dependency-plugin:2.5:get -DremoteRepositories=xebia-france-snapshot::default::https://repository-xebia-france.forge.cloudbees.com/snapshot/,xebia-france-release::default::https://repository-xebia-france.forge.cloudbees.com/release/ -Dartifact=$GROUP_ID:$ARTIFACT_ID:$VERSION:tar.gz:distribution -Ddest=$TMP_DIR/$ARTIFACT_ID-$VERSION.tgz

if [ "$?" !=  0 ];
then
   echo "mvn download failed with return code $?"
   exit -1
fi

tar -xvf $TMP_DIR/$ARTIFACT_ID-$VERSION.tgz -C $TMP_DIR
if [ "$?" !=  0 ];
then
   echo "error opening '$TMP_DIR/$ARTIFACT_ID-$VERSION.tgz' with return code $?"
   exit -1
fi

WORK_DIR=$TMP_DIR/$ARTIFACT_ID-$VERSION

if [ ! -d "$CATALINA_BASE/backup/" ];
then
   mkdir "$CATALINA_BASE/backup/"
fi

BACKUP_ARCHIVE="$CATALINA_BASE/backup/$ARTIFACT_ID-`date '+%Y%m%d-%H%M%S'`.tar.gz"

echo "Creat tomcat backup $BACKUP_ARCHIVE ..."
tar -cvzf $BACKUP_ARCHIVE \
    --exclude "$CATALINA_BASE/backup/*" \
    --exclude "$CATALINA_BASE/logs/*" \
    --exclude "$CATALINA_BASE/temp/*" \
    --exclude "$CATALINA_BASE/webapps/*" \
    --exclude "$CATALINA_BASE/work/*" \
    $CATALINA_BASE


echo "Shutdown tomcat server '$CATALINA_BASE/bin/catalina.sh stop' ..."
$CATALINA_BASE/bin/catalina.sh stop
sleep 5
echo "should replace 'catalina.sh stop' + sleep 5 by linux 'service tomcat-xxx stop' and an underlying 'kill -9'"

echo "Deploy new configuration ..."
rsync -avz --del \
   --exclude webapps \
   --exclude logs \
   --exclude backup \
   $WORK_DIR/ \
   $CATALINA_BASE

# recreate working dirs
if [ ! -d "$CATALINA_BASE/temp/" ];
then
   mkdir "$CATALINA_BASE/temp/"
fi

if [ ! -d "$CATALINA_BASE/work/" ];
then
   mkdir "$CATALINA_BASE/work/"
fi

# start tomcat
echo "Start Tomcat server '$CATALINA_BASE/bin/catalina.sh start' ..."
$CATALINA_BASE/bin/catalina.sh start

for ((  i = 12 ;  i > 0;  i--  ))
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
