package org.moczalla.stream;


import java.util.Properties;

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord};
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig

object Source {
    def isOption(s : String) = (s(0) == '-')

    def options(map : Map[Symbol, Any], list: List[String]) : Map[Symbol, Any] = {
        list match {
            case Nil =>
                map
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
        var nrOfEvents: Int = 1000
        var eventSleep: Int = 10

        val opts = options(Map(), args.toList)
        val hostname = sys.env.get("HOSTNAME") match {
            case None => "localhost"
            case Some(s: String) => s
        }

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

            val producer = new KafkaProducer[String, String](kafkaProducerProps)

            for (i <- 1 to nrOfEvents) {
                producer.send(new ProducerRecord[String, String]("stream-1", "" + (i % 4), hostname + ": value " + i)).get()
                Thread.sleep(eventSleep)
            }

            // ToDo: Write stuff into second stream as well
            // ToDo: Make the write generic with a STREAM_LIST input
        } catch {
            case e: Exception => println("Something went wrong in source on " + hostname + " with exception: " + e)
        }

        System.exit(0);
    }
}
