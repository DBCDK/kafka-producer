package dk.dbc.kafka.consumer;

import dk.dbc.kafka.exception.KafkaException;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This is a very simple consumer. It's only purpose is to check the results of using the Producer in
 * this package.
 * With this in mind, please note that:
 * * it will not try to commit offsets manually enable.auto.commit is true
 * * it will "seek" to the beginning of the partition, whenever a partition is assigned
 *
 * Basically the user will have little-to-no control over how data is read.
 *
 */
public class SimpleConsumer implements ConsumerRebalanceListener, AutoCloseable {
    private Logger log = LoggerFactory.getLogger(SimpleConsumer.class);
    private org.apache.kafka.clients.consumer.Consumer<String,String> theConsumer=null;
    private String bootstrapServers;
    private String topic;
    private String groupId;
    private long timeout=1000;

    public List<Message> poll() {
        log.info("Performing poll for group: {} on topic: {}", groupId, topic);
        ConsumerRecords<String, String> records = theConsumer.poll(timeout);
        log.info("Got {} records for group: {} on topic: {}", records.count(), groupId, topic);
        List<Message> result = new ArrayList<>(records.count());
        records.forEach(m -> result.add(new Message(m.topic(),m.key(),m.value())));
        return result;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> collection) {
        log.info("Partitions revoked: {}", collection );
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> collection) {
        log.info("Patitions assigned: {}", collection);
        collection.forEach(p -> theConsumer.seekToBeginning(collection));
    }

    public static class Message {
        private String topic;
        private String key;
        private String value;


        private Message(String topic, String key, String value) {
            this.topic = topic;
            this.key = key;
            this.value = value;
        }

        public String getTopic() {
            return topic;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "topic='" + topic + '\'' +
                    ", key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }
    private SimpleConsumer() {
    }

    public static class Builder {
        Logger log = LoggerFactory.getLogger(Builder.class);
        private String servers=null;
        private String topic;
        private String groupId = "default";
        private long timeout;

        public Builder withGroupId(String groupId){
            this.groupId = groupId;
            return this;
        }
        public Builder withServers(String servers) {
            this.servers = servers;
            return this;
        }

        public Builder withTopic(String topic){
            this.topic=topic;
            return this;
        }
        public Builder withTimeout(long timeout){
            this.timeout = timeout;
            return this;
        }

        private org.apache.kafka.clients.consumer.KafkaConsumer<String,String> createConsumer(){

            Properties props = new Properties();
            props.put("bootstrap.servers", servers);
            props.put("enable.auto.commit", "true");
            props.put("auto.commit.interval.ms", "1000");
            props.put("session.timeout.ms", "30000");
            props.put("group.id", groupId);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            return new KafkaConsumer<>(props);
        }

        public SimpleConsumer build(){
            if (topic==null){
                throw new KafkaException("A consumer is not allowed to have 0 topic");
            }
            if (servers==null){
                throw new KafkaException("A consumer must have at least one broker (server) to connect to.");
            }
            SimpleConsumer c = new SimpleConsumer();
            c.bootstrapServers = this.servers;
            c.topic = this.topic;
            c.theConsumer = createConsumer();
            c.groupId = (this.groupId==null) ? this.topic+System.currentTimeMillis() : this.groupId;
            c.timeout = this.timeout;
            log.info("Consumer-group {} subscribing to topic {}",c.groupId,c.topic);
            c.theConsumer.subscribe(Collections.singletonList(topic),c);
            log.info("Performing initial poll");
            ConsumerRecords<String, String> records = c.theConsumer.poll(0);
            log.info("Initial poll returned {} records", records.count());
            return c;
        }
    }
    public static Builder builder(){
        return new SimpleConsumer.Builder();
    }

    public void close() {
        if (theConsumer!=null) {
            theConsumer.close();
        }
    }

    public String getBootstrapServers() {
        return bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public String getGroupId() {
        return groupId;
    }

    public long getTimeout() {
        return timeout;
    }
}
