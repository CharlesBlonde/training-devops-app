#!/usr/bin/env groovy

import groovy.json.*
import java.io.*
import groovy.text.GStringTemplateEngine

def outputFile, outputwritersFile, dashboardTemplateFile, outputFileName // , logger

// gmaven injects a $project variable
if (binding.variables.containsKey("project")) {
    outputDir = "$project.build.directory/jmxtrans/"

    outputwritersFile = new FileReader(project.properties.outputWritersFile)
    dashboardTemplateFile = new FileReader(project.properties.dashboardTemplateFile)
    outputFile = new File(outputDir, project.properties.generatedDashboardFile)
    logger = log
} else {
    // standalone - debug mode
    outputDir = "../../../target/jmxtrans"
    outputwritersFile = new FileReader("../../../../infrastructure/prod/graphite-outputwriters.json")
    dashboardTemplateFile = new FileReader("../../../../webapp/src/main/jmxtrans/jmxtrans-dashboard.template")
    outputFile = new File(outputDir, "prod-ready-app-dashboard.html")

    logger = new SysoutLogger()
}

logger.debug("Base folder: " + new File(".").getAbsolutePath())

new File(outputDir).mkdirs()

def slurper = new JsonSlurper()

def srcOutputWriters = slurper.parse(outputwritersFile).outputWriters

logger.debug("srcOutputWriters: $srcOutputWriters")

def graphiteBaseUrl = srcOutputWriters[0].settings.baseUrl


def engine = new GStringTemplateEngine()

def binding = ["graphite": ["baseUrl": graphiteBaseUrl]]

def template = engine.createTemplate(dashboardTemplateFile).make(binding)
// println template.toString()

outputFile.withWriter('unicode') { w ->
    w << template.toString()
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