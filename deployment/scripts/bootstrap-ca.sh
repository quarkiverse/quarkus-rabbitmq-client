#!/bin/bash
CLIENT_NAME=client
HOSTNAME=rabbitmq
TARGET_DIR=${1:-openssl}

echo "[CA]: Bootstrapping CA directories ..."
rm -rf ${TARGET_DIR}
mkdir -p ${TARGET_DIR}/ca/certs
mkdir -p ${TARGET_DIR}/ca/private
chmod 0750 ${TARGET_DIR}/ca/private
echo 01 > ${TARGET_DIR}/ca/serial
touch ${TARGET_DIR}/ca/index.txt
mkdir -p ${TARGET_DIR}/server
mkdir -p ${TARGET_DIR}/client

echo "[CA]: Setting up openssl configuration ..."
cat <<EOT > ${TARGET_DIR}/ca/openssl.cnf
[ ca ]
default_ca = ${HOSTNAME}

[ ${HOSTNAME} ]
dir = ${TARGET_DIR}/ca
certificate = \$dir/cacert.pem
database = \$dir/index.txt
new_certs_dir = \$dir/certs
private_key = \$dir/private/cakey.pem
serial = \$dir/serial

default_crl_days = 7
default_days = 365
default_md = sha256

policy = ${HOSTNAME}_policy
x509_extensions = certificate_extensions
copy_extensions = copy

[ ${HOSTNAME}_policy ]
commonName = supplied
stateOrProvinceName = optional
countryName = optional
emailAddress = optional
organizationName = optional
organizationalUnitName = optional
domainComponent = optional

[ certificate_extensions ]
basicConstraints = CA:false

[ req ]
default_bits = 2048
default_keyfile = ${TARGET_DIR}/ca/private/cakey.pem
default_md = sha256
prompt = yes
distinguished_name = root_ca_distinguished_name
x509_extensions = root_ca_extensions

[ root_ca_distinguished_name ]
commonName = ${HOSTNAME}

[ root_ca_extensions ]
basicConstraints = CA:true
keyUsage = keyCertSign, cRLSign

[ ${CLIENT_NAME}_ca_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature,keyEncipherment
extendedKeyUsage = 1.3.6.1.5.5.7.3.2

[ server_ca_extensions ]
basicConstraints = CA:false
keyUsage = digitalSignature,keyEncipherment
extendedKeyUsage = 1.3.6.1.5.5.7.3.1
EOT

echo $(pwd)
echo "[CA]: Generating CA certificate ..."
openssl req -x509 -config ${TARGET_DIR}/ca/openssl.cnf -newkey rsa:2048 -days 365 -out ${TARGET_DIR}/ca/cacert.pem -outform PEM -subj /CN=${HOSTNAME}/ -nodes
keytool -import -alias rabbitmqca -keystore ${TARGET_DIR}/ca/cacerts.jks -file ${TARGET_DIR}/ca/cacert.pem -storepass letmein -storetype JKS -noprompt

echo "[SERVER]: Generating server keys and certficate ..."
openssl genrsa -out ${TARGET_DIR}/server/key.pem 2048
openssl req -new -key ${TARGET_DIR}/server/key.pem -out ${TARGET_DIR}/server/req.pem -outform PEM -subj /CN=${HOSTNAME}/O=server/ -nodes \
  -reqexts SAN \
  -config <(cat ${TARGET_DIR}/ca/openssl.cnf \
    <(printf "\n[SAN]\nsubjectAltName=DNS:localhost"))

openssl ca -config ${TARGET_DIR}/ca/openssl.cnf -in ${TARGET_DIR}/server/req.pem -out ${TARGET_DIR}/server/cert.pem -notext -batch -extensions server_ca_extensions
openssl pkcs12 -export -out ${TARGET_DIR}/server/keycert.p12 -in ${TARGET_DIR}/server/cert.pem -inkey ${TARGET_DIR}/server/key.pem -passout pass:letmein

echo "[CLIENT]: Generating client keys and certificate ..."
openssl genrsa -out ${TARGET_DIR}/client/key.pem 2048
openssl req -new -key ${TARGET_DIR}/client/key.pem -out ${TARGET_DIR}/client/req.pem -outform PEM -subj /CN=${CLIENT_NAME}/O=client/ -nodes
openssl ca -config ${TARGET_DIR}/ca/openssl.cnf -in ${TARGET_DIR}/client/req.pem -out ${TARGET_DIR}/client/cert.pem -notext -batch -extensions client_ca_extensions
openssl pkcs12 -export -out ${TARGET_DIR}/client/keycert.p12 -in ${TARGET_DIR}/client/cert.pem -inkey ${TARGET_DIR}/client/key.pem -passout pass:letmein
keytool -importkeystore -deststorepass letmein -destkeypass letmein -destkeystore ${TARGET_DIR}/client/client.jks -srckeystore ${TARGET_DIR}/client/keycert.p12 \
  -srcstoretype PKCS12 -srcstorepass letmein -alias 1

echo "Done ..."
