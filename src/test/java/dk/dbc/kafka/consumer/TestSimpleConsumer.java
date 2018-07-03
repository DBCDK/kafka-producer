package dk.dbc.kafka.consumer;

import com.salesforce.kafka.test.KafkaTestServer;
import com.salesforce.kafka.test.junit.SharedKafkaTestResource;
import dk.dbc.kafka.producer.TestProducer;
import org.junit.*;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 Note: Simple use case is tested in TestProducer
 @see dk.dbc.kafka.producer.TestProducer#testProducerSimple()
 */
public class TestSimpleConsumer {
    Logger log = LoggerFactory.getLogger(TestProducer.class);

    String kafkaConnectString;
    String group;
    String topic;

    @ClassRule
    public static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource();

    @Rule
    public TestName name = new TestName();

    @Before
    public void init(){
        KafkaTestServer kafkaTestServer = sharedKafkaTestResource.getKafkaTestServer();
        kafkaConnectString = kafkaTestServer.getKafkaConnectString();
        group="group";
        topic="topic";
    }

    @Test(expected = RuntimeException.class)
    public void createConsumerWithNoServerIsAnError(){
        SimpleConsumer.builder().withTopic("xxx").withGroupId("group").build();
    }

    @Test(expected = RuntimeException.class)
    public void createConsumerWithNoTopicIsAnError(){
        SimpleConsumer.builder().withServers(kafkaConnectString).withGroupId(group).build();
    }

    public void createConsumerWithNoGroupIsNotAnError(){
        SimpleConsumer.builder().withServers(kafkaConnectString).withTopic(topic).build();
    }

}
