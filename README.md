# client-server
Solution corresponding to Java Advanced statement task. It is implemented as
server maintained multiple clients via connections realized each in separated
thread. Payload testing class and simple exchange protocol are also realized.
Logging is also supported for exceptions.

Added corrections according review #1 issues. Added server manual stopping,
another method of messages sending/reception, changed method of command tokens
processing. Implements some java 8 tricks.

Folders structure:
- files - folder containig configuration *.xml - file for client and server with
          server port number and host name;
- log - folder with *.log - files;
- jar - forlder with built *.jar - archives:
      - java.advanced.jar - archive with classes for client/server execution;
      - java.advanced.test.jar - archive for testing execution;
- task - folder with source files, project files for IntelliJ IDEA and building
         files for Apache Ant build tool:
       - ant - building script and property file for Ant:
       Usage: "ant clean" - cleans temporary content obtained via last build;
              "ant mkdirs" - creates appropriate folders for build;
              "ant copyrc" - copies project resources to specified folder for
                             *.jar archive building;
              "ant compile" - compiles source and resource files into
                              *.class - files;
              "ant make-main-jar" - packs appropriate *.class - files into
                                    executable *.jar archive performing
                                    client/server starting;
              "ant make-test-jar" - packs appropriate *.class - files into
                                    executable *.jar archive performing
                                    server payload testing starting;
              "ant run-server" - executes application corresponding to
                                 client/server in server mode;
              "ant run-client" - executes application corresponding to
                                 client/server in client mode;
              "ant run-test" - - starts application performing server payload
                                 testing;
       - .idea - folder with Intellij IDEA project content;
       - src - source(*.java) and resource(.property) files folder;
       - task.iml - Intellij IDEA project file;
- README.md - brief project structure and files/folders purpose description;
- run-client.sh - executes application in client mode;
- run-client.bat - executes application in client mode under Windows;
- run-server.sh - executes application in server mode;
- run-server.bat - executes application in server mode under Windows;
- run-test.sh - executes server payload testing;
- run-test.bat - executes server payload testing under Windows.
