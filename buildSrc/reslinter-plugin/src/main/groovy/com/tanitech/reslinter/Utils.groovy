package com.tanitech.reslinter

import groovy.util.slurpersupport.GPathResult
import org.gradle.api.Project

class Utils {

    static getProjectsWithSource(Project project) {
        project.allprojects.findAll({ it.file("src").exists() })
    }

    static Config readPluginConfig(String configFilePath) {
        def config = new Config()
        if (configFilePath) {
            GPathResult rootNode = new XmlSlurper().parseText(new File(configFilePath).text)
            rootNode["setting"].each { GPathResult node ->
                if (node["item"].any()) {
                    config[node["@name"] as String] = node["item"].collect { GPathResult n -> n.text() }
                } else {
                    config[node["@name"] as String] = node.text()
                }
            }
        }
        return config
    }

    static boolean removeLineWithPattern(File inputFile, String pattern) throws IOException {
        if (!inputFile.text.contains(pattern)) {
            return true
        }
        File tempFile = new File(inputFile.parent, "${inputFile.name}.tmp");

        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

        String currentLine
        while ((currentLine = reader.readLine()) != null) {
            String trimmedLine = currentLine.trim()
            if (trimmedLine.contains(pattern)) continue
            writer.write(currentLine + System.getProperty("line.separator"));
        }
        writer.close();
        reader.close();
        return tempFile.renameTo(inputFile);
    }
}
