@echo off
rem Script for starting client from "jar/java.advanced.jar" - archive.

rem 

rem Parameters:

rem -config files/config.xml - path and file name of configuration file with host name and server port number;

rem -client - starting application in client mode

rem 

rem Application will create "log" - folder for logging exceptions.



java -jar jar/java.advanced.jar -config files/config.xml -client
