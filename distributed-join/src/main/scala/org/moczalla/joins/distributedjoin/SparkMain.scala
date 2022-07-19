package org.moczalla.joins.distributedjoin;


import org.apache.spark._
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming._

object SparkMain {
    @throws(classOf[Exception])
    def main(args: Array[String]): Unit = {
        val spark = SparkSession.builder().appName("JoinExample").getOrCreate();
        spark.sparkContext.setLogLevel("WARN");

        try {
            val s1 = spark.readStream.format("kafka")
                .option("kafka.bootstrap.servers", "source-1:9092,source-2:9092,source-3:9092") // TODO: read value from sys.env.get("SOURCE-HOSTS");
                .option("subscribe", "stream") // TODO: Init multiple streams based on sys.env.get("STREAMS");
                .option("startingOffsets", "earliest") // Actually not sure about that part as we get a first hugh bunch of data
                .load();

            val ss1 = s1.selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)");

            ss1.writeStream
                .format("console")
                .outputMode("append")
                .start()
                .awaitTermination();
        } catch {
            case e: Exception => println("Something went wrong with exception: " + e)
        }

        System.exit(0);
    }
}
