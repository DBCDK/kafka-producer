package dk.dbc.kafka.producer;

import com.salesforce.kafka.test.KafkaTestServer;
import com.salesforce.kafka.test.junit.SharedKafkaTestResource;
import dk.dbc.kafka.consumer.SimpleConsumer;
import org.apache.kafka.common.KafkaException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestProducer  {
    Logger log = LoggerFactory.getLogger(TestProducer.class);

    private String kafkaConnectString;

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Before
    public void init(){
        KafkaTestServer kafkaTestServer = sharedKafkaTestResource.getKafkaTestServer();
        kafkaConnectString = kafkaTestServer.getKafkaConnectString();
    }

    @Test
    public void testProducerSimple(){
        String brokers = kafkaConnectString;
        String topicName = "my-test-topic";
        String consumerGroupname = "my-group";

        Producer producer = Producer.builder()
            .withServers(brokers)
            .withTopic(topicName)
            .build();
        producer.send("key1", "message1");
        producer.send("key2", "message2");

        SimpleConsumer consumer = SimpleConsumer.builder()
            .withServers(brokers)
            .withTopic(topicName)
            .withGroupId(consumerGroupname)
            .build();

        List<SimpleConsumer.Message> messages = consumer.poll();

        SimpleConsumer.Message message;
        message = messages.get(0);
        assertEquals("key1",message.getKey());
        assertEquals("message1", message.getValue());

        message = messages.get(1);
        assertEquals(topicName,message.getTopic());
        assertEquals("key2",message.getKey());
        assertEquals("message2",message.getValue());

        assertEquals(kafkaConnectString,consumer.getBootstrapServers());
        assertEquals(topicName,consumer.getTopic());
        assertEquals(consumerGroupname,consumer.getGroupId());

        assertEquals(kafkaConnectString,producer.getBootstrapServers());
        assertEquals(topicName,producer.getTopic());
    }

    @Test(expected = KafkaException.class)
    public void specifyingWrongServerProducesException(){
        Producer producer = Producer.builder()
            .withServers("wrong-broker")
            .withTopic("my-test-topic")
            .build();
    }

    @Test(expected =  RuntimeException.class)
    public void creatingProducerWithNoServerGeneratesError(){
        Producer.builder().withTopic("my-test-topic").build();
    }
    @Test(expected =  RuntimeException.class)
    public void creatingProducerWithNoTopicGeneratesError(){
        Producer.builder().withServers(kafkaConnectString).build();
    }
}
