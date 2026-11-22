#!/bin/sh
set -eu

TLS_JAVA_OPTS=""
CA_FILE="${LOCAL_TLS_CA_FILE:-}"
if [ -n "$CA_FILE" ] && [ -f "$CA_FILE" ]; then
  TRUSTSTORE=/tmp/bluenet-cacerts
  TRUSTSTORE_PASSWORD="${LOCAL_TLS_TRUSTSTORE_PASSWORD:-changeit}"
  cp "$JAVA_HOME/lib/security/cacerts" "$TRUSTSTORE"
  "$JAVA_HOME/bin/keytool" -importcert -noprompt \
    -alias bluenet-local \
    -file "$CA_FILE" \
    -keystore "$TRUSTSTORE" \
    -storepass "$TRUSTSTORE_PASSWORD" >/dev/null 2>&1
  TLS_JAVA_OPTS="-Djavax.net.ssl.trustStore=$TRUSTSTORE -Djavax.net.ssl.trustStorePassword=$TRUSTSTORE_PASSWORD"
fi

exec java $JAVA_OPTS $TLS_JAVA_OPTS -Dfile.encoding=UTF-8 -jar /app/backend.jar
