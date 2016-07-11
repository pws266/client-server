# Script for starting server testing from "jar/java.advanced.test.jar" archive.
# Performs testing in multi-client mode with specified number of known
# tokens(commands) per each client.
#
# Parameters:
# -config files/config.xml - path and file name of configuration file with host
#                            name and server port number;
# -usr - number of clients connecting to server for testing
# -cmd - number of known tokens(commands) sending by each client in byte stream.
#
# Application will create "log" - folder for logging exceptions.

usage() {
    echo "----------------"
    echo "Server payload testing"
    echo "Usage: ./run-test.sh $USR_NUMBER $CMD_NUMBER"
    echo "       - USR_NUMBER - clients number;"
    echo "       - CMD_NUMBER - number of commands sending by each client;"
    echo " "
    echo "Example: ./run-test.sh 200 1000"
    echo "----------------"
}

USR_NUMBER=$1
CMD_NUMBER=$2

if [ ${#} -ne 2 ]; then
    usage
    exit 1
fi

java -jar jar/java.advanced.test.jar -config files/config.xml \
                                     -usr ${USR_NUMBER} \
                                     -cmd ${CMD_NUMBER}
