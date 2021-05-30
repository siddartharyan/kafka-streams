package com.app;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Arrays;
import java.util.Properties;

public class StreamstarterApp {

    public static void main(String[] args) {
        Properties props=new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG,"word-count-input");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,Serdes.String().getClass());

        KStreamBuilder builder=new KStreamBuilder();

        //stream

        KStream<String,String> wordCountInput=builder.stream("word-coun-output");

        //map the values to lower case

        KTable<String,Long> output=wordCountInput.mapValues(value->value.toLowerCase())

        //flatmap split by space
        .flatMapValues(value-> Arrays.asList(value.split(" ")))

        //select key
        .selectKey((ignoredKey,word)->word)

        //group by key
        .groupByKey()
                .count("count");

        output.to(Serdes.String(),Serdes.Long(),"word-count");

        KafkaStreams stream=new KafkaStreams(builder,props);

        stream.start();

        //print topology

        System.out.println(stream.toString());

        //shut down hook to correctly close the application

        Runtime.getRuntime().addShutdownHook(new Thread(stream::close));

    }
}
