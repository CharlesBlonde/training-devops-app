#!/usr/bin/env groovy

import groovy.json.*
import java.io.*

def baseFolder, outputFile, serversFile, mbeanQueriesFile, outputFileName // , logger

// gmaven injects a $project variable
if (binding.variables.containsKey("project")) {
    baseFolder = "$project.basedir/src/main/template/"
    outputDir = "$project.build.directory/jmxtrans/"

    serversFile = new FileReader(project.properties.serversFile)
    mbeanQueriesFile = new FileReader(project.properties.mbeanQueriesFile)
    outputFile = new File(outputDir, project.properties.generatedJmxtransFile)
    logger = log
} else {
    // standalone - debug mode
    baseFolder = "../template/"
    outputDir = "../../../target/jmxtrans"
    serversFile = new FileReader("../../../../infrastructure/prod/servers.json")
    mbeanQueriesFile = new FileReader("../../../../webapp/src/main/jmxtrans/mbean-queries.json")
    outputFile = new File(outputDir, "prod-ready-app.jmxtrans.json")

    logger = new SysoutLogger()
}
def outputwritersFile = new FileReader("$baseFolder/outputwriters.json")

logger.debug("baseFolder : " + new File(baseFolder).getAbsolutePath())

new File(outputDir).mkdirs()

def slurper = new JsonSlurper()

def srcServers = slurper.parse(serversFile).servers
logger.debug("srcServers: $srcServers")

def srcQueries = slurper.parse(mbeanQueriesFile).queries
logger.debug("srcQueries: $srcQueries")

def srcOutputWriters = slurper.parse(outputwritersFile).outputWriters

logger.debug("srcOutputWriters: $srcOutputWriters")

def jmxTransServers = []

srcServers.each { server ->
    logger.debug("  -- server: " + server)
    logger.debug("  --- host: " + server.host)

    def jmxTransQueries = []

    srcQueries.each { query ->
        logger.debug("  # query: $query")
        def jmxTransQuery = {
            obj query.obj
            resultAlias query.resultAlias
            attr query.attr
            outputWriters srcOutputWriters
        }
        jmxTransQueries.add(jmxTransQuery)

    }
    def jmxTransServer = {
        host server.host
        port server.port
        alias server.alias
        queries jmxTransQueries

    }
    jmxTransServers.add(jmxTransServer)
}



def builder = new JsonBuilder({servers jmxTransServers})

outputFile.withWriter('unicode') { w ->
    w << builder.toPrettyString()
}

logger.info("Generated $outputFile.name")
logger.debug("Generated $outputFile.canonicalPath")

class SysoutLogger {
    def debug(msg) {
        println("DEBUG: $msg")
    }

    def info(msg) {
        println("INFO $msg")
    }
}