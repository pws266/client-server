# Script for starting client from "jar/java.advanced.jar" - archive.
#
# Parameters:
# -config files/config.xml - path and file name of configuration file with host name and server port number;
# -client - starting application in client mode
#
# Application will create "log" - folder for logging exceptions.

java -jar jar/java.advanced.jar -config files/config.xml -client
