# Script for starting server from "jar/java.advanced.jar" - archive.
#
# Parameters:
# -config files/config.xml - path and file name of configuration file with host name and server port number;
# -server - starting application in server mode
#
# Application will create "log" - folder for logging exceptions.

java -jar jar/java.advanced.jar -config files/config.xml -server
