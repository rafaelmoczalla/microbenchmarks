FROM node:latest

ENV SPARK_MASTER_NAME node-main
ENV SPARK_MASTER_PORT 7077

COPY SparkJob-0.1.0.jar /spark/applications/SparkJob-0.1.0.jar

COPY submit.sh /

CMD ["/bin/bash", "/submit.sh"]
