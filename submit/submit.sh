#!/bin/bash

/wait-for-step.sh
/execute-step.sh

if [ ! -z "/spark/applications/SparkJob-0.1.0.jar" ]; then
    echo "Submit application /spark/applications/SparkJob-0.1.0.jar with main class org.moczalla.joins.distributedjoin.SparkMain to Spark master spark://node-main:7077"
    echo "Passing arguments "
    /spark/bin/spark-submit         --class org.moczalla.joins.distributedjoin.SparkMain         --master spark://node-main:7077         --packages "org.apache.spark:spark-sql-kafka-0-10_2.13:3.3.0"                  /spark/applications/SparkJob-0.1.0.jar 
else
    if [ ! -z "${SPARK_APP_PYTHON_LOCATION}" ]; then
        echo "Submit application ${SPARK_APP_PYTHON_LOCATION} to Spark master spark://node-main:7077"
        echo "Passing arguments "
        PYSPARK_PYTHON=python3  /spark/bin/spark-submit             --master spark://node-main:7077             --packages "org.apache.spark:spark-sql-kafka-0-10_2.13:3.3.0"                          ${SPARK_APP_PYTHON_LOCATION} 
    else
        echo "Not recognized application."
    fi
fi

/finish-step.sh
