@echo off
rem Script for starting server testing from "jar/java.advanced.test.jar" archive.

rem Performs testing in multi-client mode with specified number of known

rem tokens(commands) per each client.

rem

rem Parameters:

rem -config files/config.xml - path and file name of configuration file with host

rem                            name and server port number;

rem -usr - number of clients connecting to server for testing

rem -cmd - number of known tokens(commands) sending by each client in byte stream.

rem

rem Application will create "log" - folder for logging exceptions.



rem getting command line arguments number

set argC=0
for %%i in (%*) do set /a argC+=1

if %argC% neq 2 (
    @echo ----------------
    @echo Server payload testing
    @echo Usage: run-test.bat USR_NUMBER CMD_NUMBER
    @echo        - USR_NUMBER - clients number;
    @echo        - CMD_NUMBER - number of commands sending by each client;
    @echo.
    @echo Example: run-test.bat 200 1000
    @echo ----------------

    exit /b
)

set USR_NUMBER=%1

set CMD_NUMBER=%2





java -jar jar/java.advanced.test.jar -config files/config.xml -usr %USR_NUMBER% -cmd %CMD_NUMBER%
