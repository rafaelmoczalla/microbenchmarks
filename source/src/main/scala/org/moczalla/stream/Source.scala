package org.moczalla.stream;


import java.util.Properties;
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord};
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig

object Source {
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

        try {
            val kafkaProducerProps: Properties = {
                val props = new Properties()
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sys.env.get("HOSTNAME").toString() + ":9092")
                props.put(ProducerConfig.CLIENT_ID_CONFIG, sys.env.get("HOSTNAME").toString())
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName())
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName())
                props
            }

            var threads = Array[Thread]()

            val es = Executors.newCachedThreadPool

            for (stream <- streams) {
                es.execute(new Thread(new Runnable {
                    private var props: Properties = _
                    private var stream: String = _

                    def init(props: Properties, stream: String): Runnable = {
                        this.props = props
                        this.stream = stream
                        this
                    }

                    override def run: Unit = {
                        val producer = new KafkaProducer[String, String](props)

                        for (i <- 1 to nrOfEvents) {
                            producer.send(new ProducerRecord[String, String](stream, "" + (i % 4), "{ \"key\":" + (i % 4) + ", \"hostname\": \"" + hostname + "\", \"stream\": \"" + stream + "\", \"value\": " + i + "}")).get()
                            Thread.sleep(eventSleep)
                        }
                    }
                }.init(kafkaProducerProps, stream)))
            }

            es.shutdown // Waits until all threads finish & tells the executor to not accept any other runnables & to shutdown when all threads finished
            es.awaitTermination(maxExecutionTime, TimeUnit.MILLISECONDS)
        } catch {
            case e: Exception => println("Something went wrong in source on " + hostname + " with exception: " + e)
            System.exit(1)
        }

        System.exit(0)
    }
}
