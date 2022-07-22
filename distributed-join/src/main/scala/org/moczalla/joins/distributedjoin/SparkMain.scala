package org.moczalla.joins.distributedjoin


import scala.util.parsing.json.JSON

import org.apache.spark._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.Dataset
import org.apache.spark.streaming._
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord

object SparkMain {
    def isOption(s : String) = (s(0) == '-')

    def options(map : Map[Symbol, Any], list: List[String]) : Map[Symbol, Any] = {
        list match {
            case Nil =>
                map
            case "--maxExecutionTime" :: value :: tail =>
                options(map ++ Map(Symbol("maxExecutionTime") -> value.toInt), tail)
            case "--nrOfEvents" :: value :: tail =>
                options(map ++ Map(Symbol("nrOfEvents") -> value.toInt), tail)
            case "--eventSleep" :: value :: tail =>
                options(map ++ Map(Symbol("eventSleep") -> value.toInt), tail)
            case string :: opt2 :: tail if isOption(opt2) =>
                options(map ++ Map(Symbol("infile") -> string), list.tail)
            case string :: Nil =>
                options(map ++ Map(Symbol("infile") -> string), list.tail)
            case option :: tail =>
                println("Unknown option " + option)
                System.exit(1)
                map
        }
    }

    case class Record(
        hostname: String,
        stream: String,
        value: Int,
        timestamp: Long
    )

    @throws(classOf[Exception])
    def main(args: Array[String]): Unit = {
        // Options
        var maxExecutionTime: Int = 30000
        var nrOfEvents: Int = 1000
        var eventSleep: Int = 10

        val opts = options(Map(), args.toList)

        val hostname = sys.env.get("HOSTNAME") match {
            case Some(s: String) => s
            case None => "localhost"
        }

        val bootstrapServersPort = sys.env.get("BOOTSTRAP_SERVERS_PORT") match {
            case Some(s: String) => s.toInt
            case None => 9092
        }

        val sources = sys.env.get("SOURCE_LIST") match {
            case Some(s: String) => s.split(" +")
            case None => println("Environment variable SOURCE_LIST not set. What are the streams?"); System.exit(1); Array("")
        }

        val streams = sys.env.get("STREAM_LIST") match {
            case Some(s: String) => s.split(" +")
            case None => println("Environment variable STREAM_LIST not set. What are the streams?"); System.exit(1); Array("")
        }

        if (opts.contains(Symbol("maxExecutionTime")))
            maxExecutionTime = opts(Symbol("maxExecutionTime")).asInstanceOf[Int]

        if (opts.contains(Symbol("eventSleep")))
            eventSleep = opts(Symbol("eventSleep")).asInstanceOf[Int]

        if (opts.contains(Symbol("nrOfEvents")))
            nrOfEvents = opts(Symbol("nrOfEvents")).asInstanceOf[Int]

        var bootstrapServers = ""

        for (source <- sources) {
            bootstrapServers += source + ":" + bootstrapServersPort + ","
        }

        if (0 < sources.length)
            bootstrapServers = bootstrapServers.dropRight(1)

        val spark = SparkSession.builder().appName("JoinExample").getOrCreate()
        spark.sparkContext.setLogLevel("WARN")

        try {
            var srcs = Array[DStream[(Int, Record)]]()

            val kafkaParams = Map[String, Object](
                "bootstrap.servers" -> bootstrapServers,
                "key.deserializer" -> classOf[StringDeserializer],
                "value.deserializer" -> classOf[StringDeserializer],
                "group.id" -> sys.env.get("HOSTNAME").toString()
            )

            val batchInterval = Seconds(5)
            val batchesToRun = 2000

            val ssc = new StreamingContext(spark.sparkContext, batchInterval)

            for (stream <- streams) {
                srcs = srcs :+ KafkaUtils.createDirectStream[String, String](
                    ssc,
                    PreferConsistent,
                    Subscribe[String, String](Array(stream), kafkaParams)
                ).map(record => (record.value, record.timestamp))
                .flatMap({case (record: String, timestamp: Long) =>
                    JSON.parseFull(record).map(rawMap => {
                        val map = rawMap.asInstanceOf[Map[String, Any]]
                        (map.get("key").get.toString.toDouble.toInt, Record(
                            map.get("hostname").get.toString,
                            map.get("stream").get.toString,
                            map.get("value").get.toString.toDouble.toInt,
                            timestamp
                        ))
                    })
                })//.cache()
            }

            if (0 < srcs.length) {
                if (srcs.length < 2)
                    srcs(0).print
                else
                    srcs(0).window(Seconds(60), Seconds(10))
                        .join(srcs(1).window(Seconds(60), Seconds(10))).map(record => {
                            val key = record._1
                            val r0 = record._2._1
                            val r1 = record._2._2

                            (key, r0.hostname, r1.hostname, r0.stream, r1.stream, r0.value, r1.value, r0.timestamp, r1.timestamp)
                        }).print
            }

            ssc.start
            ssc.awaitTermination
        } catch {
            case e: Exception => println("Something went wrong with exception: " + e)
        }

        System.exit(0)
    }
}
