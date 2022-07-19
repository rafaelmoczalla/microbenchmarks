FROM node:latest

ENV SPARK_MASTER_NAME node-main
ENV SPARK_MASTER_PORT 7077

COPY SparkJob-0.1.0.jar /spark/applications/SparkJob-0.1.0.jar

ENV SPARK_APPLICATION_JAR_LOCATION="/spark/applications/SparkJob-0.1.0.jar"
ENV SPARK_APPLICATION_MAIN_CLASS="org.moczalla.joins.distributedjoin.SparkMain"
ENV SPARK_APPLICATION_ARGS=""

COPY submit.sh /

CMD ["/bin/bash", "/submit.sh"]
