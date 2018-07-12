package dk.dbc.kafka.producer;

import dk.dbc.kafka.exception.KafkaException;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * Kafka-producer to be integrated into other projects.
 *
 * Usage:
 * To configure a producer:
 * <pre>
 *   Producer producer = Producer.builder()
 *       .withServers(brokers) // DBC kafka broker names
 *       .withTopic(topic) // an agreed upon kafka-topic
 *       .build();
 * </pre>
 * To send
 * <pre>
 *   producer.send("key1", "message"); // kafka key-value pairs, where value will most likely be JSON.
 * </pre>
 *
 * Note that:
 *  <ul>
 *      <li>Trying to connect to an not-existing server, is an error and will throw a KafkaExecption (RuntimeException)</li>
 *      <li>Trying to send to a not-existing topic (in production) will result in a RuntimeException as well. Since Kafka is configured to only accept defined topics.</li>
 *  </ul>
 */
public class Producer implements AutoCloseable {

    private org.apache.kafka.clients.producer.Producer<String,String> theProducer = null;

    private String bootstrapServers;
    private String topic;

    private Producer() {
    }

    public void send(String key, String value) {
        theProducer.send( pack(  key, value ));
    }

    @Override
    public void close() {
        if (theProducer!=null) {
            theProducer.close();
        }
    }

    public static Builder builder(){
        return new Builder();
    }
    public static class Builder {

        private String servers;
        private String topic;
        private String acks = "all";
        private int retries = 0;
        private int batchsize = 16384;
        private int linger = 1;
        private int bufferMemory = 33554432;

        public Builder withServers(String s) {
            this.servers = s;
            return this;
        }

        public Builder withTopic(String s){
            this.topic = s;
            return this;
        }
        public Builder withAcks(String acks){
            this.acks = acks;
            return this;
        }
        public Builder withRetries(int retries){
            this.retries = retries;
            return this;
        }
        public Builder withBatchsize(int batchsize){
            this.batchsize = batchsize;
            return this;
        }
        public Builder withLinger(int linger){
            this.linger = linger;
            return this;
        }
        public Builder withBufferMemory(int bufferMemory){
            this.bufferMemory = bufferMemory;
            return this;
        }
        private org.apache.kafka.clients.producer.Producer<String,String> createProducer(){

            Properties props = new Properties();
            props.put("bootstrap.servers", servers);
            props.put("acks", acks);
            props.put("retries", retries);
            props.put("batch.size", batchsize);
            props.put("linger.ms", linger);
            props.put("buffer.memory", bufferMemory);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

            return new KafkaProducer<>(props);
        }

        public Producer build(){
            if (this.servers==null){
                throw new KafkaException("A producer must have at least one broker (server) to connect to.");
            }
            if (this.topic==null){
                throw new KafkaException("A producer must define a topic to send to.");
            }
            Producer p = new Producer();
            p.bootstrapServers = this.servers;
            p.topic = this.topic;
            p.theProducer = createProducer();
            return p;
        }
    }

    private ProducerRecord<String,String> pack(String key, String value){
        return new ProducerRecord<>(topic,key,value);
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

}
