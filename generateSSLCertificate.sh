#!/bin/bash

# Define variables for file names, passwords, etc.
KEYSTORE_PASSWORD="changeit"
ALIAS="dummyClientAlias"
KEYSTORE_FILE="dummyClient.jks"
PFX_FILE="WebServiceClientCert.pfx"
KEY_PASSWORD="changeit"
VALIDITY_DAYS=3650

rm -rf *.pfx *.jks

# Generate a self-signed certificate using keytool
keytool -genkeypair -keyalg RSA -keysize 2048 -alias $ALIAS -keystore $KEYSTORE_FILE -storepass $KEYSTORE_PASSWORD -keypass $KEY_PASSWORD -validity $VALIDITY_DAYS -dname "CN=Dummy Client, OU=Test, O=Test Corp, L=Test City, S=Test State, C=US" -noprompt

# Convert the generated JKS keystore to PFX format
keytool -importkeystore -srckeystore $KEYSTORE_FILE -srcstorepass $KEYSTORE_PASSWORD -destkeystore $PFX_FILE -deststoretype PKCS12 -deststorepass $KEYSTORE_PASSWORD -noprompt

echo "Self-signed certificate generated and converted to PFX format."
