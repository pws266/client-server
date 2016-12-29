# client-server
Solution corresponding to Java Advanced statement task. It is implemented as
server maintained multiple clients via connections realized each in separated
thread. Payload testing class and simple exchange protocol are also realized.
Logging is also supported for exceptions.

Added corrections according review #1 issues. Added server manual stopping,
another method of messages sending/reception, changed method of command tokens
processing. Implements some java 8 tricks.

Added corrections according review #2 issues. Payload testing class is
refactored to unit test. The parameters of payload test (e.g. users number and
commands number) could be changed and assigned in Ant configuration file
"build.xml". The methods in all classes are separated on the sets of short
simple methods. The exceptions is catched in the points of code where the method
generationg this exception is invoked.

Folders structure:
- files - folder containig configuration *.xml - file for client and server with
          server port number and host name;
- log - folder with *.log - files;
- jar - forlder with built *.jar - archives:
      - java.advanced.jar - archive with classes for client/server execution;
- task - folder with source files, project files for IntelliJ IDEA and building
         files for Apache Ant build tool:
       - ant - building script and property file for Ant:
       Usage: "ant clean" - cleans temporary content obtained via last build;
              "ant mkdirs" - creates appropriate folders for build;
              "ant copyrc" - copies project resources to specified folder for
                             *.jar archive building;
              "ant compile" - compiles source and resource files into
                              *.class - files;
              "ant compile-test" - compiles appropriate source files into the
                                   payload unit test *.class - files;
              "ant make-main-jar" - packs appropriate *.class - files into
                                    executable *.jar archive performing
                                    client/server starting;
              "ant run-server" - executes application corresponding to
                                 client/server in server mode;
              "ant run-client" - executes application corresponding to
                                 client/server in client mode;
              "ant payload-test" - executes payload unit test for correct
                                   server functionality verification in
                                   multiuser mode. In this target you could
                                   change test parameters by "build.xml" - file
                                   edition:
                  - cfgFileName - assigns *.xml client/server configuration
                                  file name;
                  - userNumber - number of users connecting to server in test;
                  - commandsNumber - number of commands from each user
                                     processing by server including user name
                                     and "quit" commands.
       - .idea - folder with Intellij IDEA project content;
       - src - source(*.java) and resource(.property) files folder;
       - lib - libraries (*.jar - archives) required for payload unit test
               deploying from Ant;
       - test - source(*.java) files folder for unit tests;
       - task.iml - Intellij IDEA project file;
- README.md - brief project structure and files/folders purpose description;
- run-client.sh - executes application in client mode;
- run-client.bat - executes application in client mode under Windows;
- run-server.sh - executes application in server mode;
- run-server.bat - executes application in server mode under Windows.

