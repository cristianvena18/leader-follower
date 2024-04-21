#!/bin/sh

mydir=/app
truststore=${MONGO_DB_TRUST_STORE}
storepassword=${MONGO_DB_TRUST_STORE_PASS}
keystorecert=${MONGO_DB_KEY_STORE_JKS}
usetruststore=${USE_MONGO_DB_TRUST_STORE}

if [ "$usetruststore" = "true" ]
then
  if [ -z $keystorecert ]
  then
    echo "Parseando archivo certificado PEM"
    curl -sS "https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem" > ${mydir}/rds-combined-ca-bundle.pem
        awk 'split_after == 1 {n++;split_after=0} /-----END CERTIFICATE-----/ {split_after=1}{print > "rds-ca-" n ".pem"}' < ${mydir}/rds-combined-ca-bundle.pem

    for CERT in rds-ca-*; do
      alias=$(openssl x509 -noout -text -in $CERT | perl -ne 'next unless /Subject:/; s/.*(CN=|CN = )//; print')
      echo "Importing $alias"
      keytool -import -file ${CERT} -alias "${alias}" -storepass ${storepassword} -keystore ${truststore} -noprompt
      rm $CERT
    done

    rm ${mydir}/rds-combined-ca-bundle.pem

    echo "Trust store content is: "

    keytool -list -v -keystore "$truststore" -storepass ${storepassword} | grep Alias | cut -d " " -f3- | while read alias
    do
       expiry=`keytool -list -v -keystore "$truststore" -storepass ${storepassword} -alias "${alias}" | grep Valid | perl -ne 'if(/until: (.*?)\n/) { print "$1\n"; }'`
       echo " Certificate ${alias} expires in '$expiry'"
    done
  else
    echo "Using already generated JKS TRUST STORE KEYS file"
    curl -sS "${keystorecert}" > ${truststore}
  fi
else
  echo "NOT USING TRUST-STORE FOR DB"
fi




export JAVA_OPTS="-Dcom.instana.agent.jvm.name=${INSTANA_SERV_NAME}"

RUN_COMMAND="java $JAVA_OPTS -jar app.jar"

${RUN_COMMAND}
