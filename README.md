# kafka-producer

## Usage

Usage:
To configure a producer:
<pre>
  Producer producer = Producer.builder()
      .withServers(brokers) // DBC kafka broker names
      .withTopic(topic) // an agreed upon kafka-topic
      .build();
</pre>
To send
<pre>
  producer.send("key1", "message"); // kafka key-value pairs, where value will most likely be JSON.
</pre>
Note that:
 <ul>
     <li>Trying to connect to an not-existing server, is an error and will throw a KafkaExecption (RuntimeException)</li>
     <li>Trying to send to a not-existing topic (in production) will result in a RuntimeException as well. Since Kafka is configured to only accept defined topics.</li>
 </ul>

## Utility

A SimpleConsumer is available. It's only use is to check your setup (please don't use this in production).

