#!/bin/sh

##############################################################################
# Gradle startup script for UNIX
##############################################################################

# Attempt to set APP_HOME

APP_HOME=$( cd "${APP_HOME:-./}" && pwd -P ) || exit

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

exec "$JAVACMD" "$@"
