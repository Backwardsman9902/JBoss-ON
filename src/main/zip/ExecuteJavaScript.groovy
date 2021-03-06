/*
 * Licensed Materials - Property of IBM Corp.
 * IBM UrbanCode Build
 * IBM UrbanCode Deploy
 * IBM UrbanCode Release
 * IBM AnthillPro
 * (c) Copyright IBM Corporation 2002, 2015. All Rights Reserved.
 *
 * U.S. Government Users Restricted Rights - Use, duplication or disclosure restricted by
 * GSA ADP Schedule Contract with IBM Corp.
 */

import com.urbancode.air.AirPluginTool
import com.urbancode.commons.fileutils.filelister.FileListerBuilder
import com.urbancode.commons.fileutils.filelister.FileLister
import com.urbancode.air.plugin.jon.helper.JONCmdHelper

def apTool = new AirPluginTool(this.args[0], this.args[1])
def props = apTool.getStepProperties()

final def isWindows = apTool.isWindows

def username = props['username'] != null ? props['username'] : ""
def password = props['password'] != null ? props['password'] : ""
def hostname = props['hostname']
def port = props['serverPort']
def cliPath = new File(props['cliPath'])
def cli = isWindows ? "rhq-cli.bat" : "rhq-cli.sh"
def cliFile = new File(cliPath, cli)

if (!cliFile.isFile()) {
    throw new Exception("Could not find file in directory ${cliFile.absolutePath}")
}

def cmdArgs = [cliFile.absolutePath]
cmdArgs << "-u"
cmdArgs << "${username}"
cmdArgs << "-p"
cmdArgs << "${password}"
cmdArgs << "-s"
cmdArgs << "${hostname}"
cmdArgs << "-t"
cmdArgs << "${port}"

def createFileList() {
    def fileList = []
    def workDir = new File(".").canonicalFile
    def scripts = props['scriptFiles']
    FileListerBuilder flb = new FileListerBuilder(workDir)
    flb.include(scripts.tokenize("\n") as String[])
    FileLister fl = flb.build()
    def rawList = fl.list()
    for (file in rawList) {
        def newFile = new File(file.path())
        if (newFile.isFile()) {
            println("found ${file.path()}")
            fileList << newFile
        }
    }
    return fileList
}

def fileList = createFileList()
println("Executing ${fileList.size()} + files.")
for (file in fileList) {
    cmdArgs << "-f ${file.absolutePath}"
}

def ch = new JONCmdHelper()
ch.runJONCommand("Executing Script Files", cmdArgs)