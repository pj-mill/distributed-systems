package distributed.systems;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App 
{
    private static final Logger logger = LogManager.getLogger(App.class);

    // Define the Kafka topic we're going to send messages to
    private static final String TOPIC = "events";

    // List of Kafka addresses our producers will use to establish the initial connection to the entire Kafka
    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092,127.0.0.1:9093,127.0.0.1:9094";

    public static void main(String[] args) {
        Producer<Long, String> kafkaProducer = createKafkaProducer(BOOTSTRAP_SERVERS);

        try {
            produceMessages(10, kafkaProducer);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            kafkaProducer.flush();
            kafkaProducer.close();
        }
    }

    public static void produceMessages(int numberOfMessages, Producer<Long, String> kafkaProducer) throws ExecutionException, InterruptedException {
        // define the partition we're going to send messages to
        //int partition = 0;

        // create a loop which will have as many iterations as the number of messages
        for (int i = 0; i < numberOfMessages; i++) {
            long key = i;
            String value = String.format("event %d", i);

            //long timeStamp = System.currentTimeMillis();

            // Create a producer record (specifying partition)
            // ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, partition, timeStamp, key, value);

            // Create a producer record (without specifying partition)
            ProducerRecord<Long, String> record = new ProducerRecord<>(TOPIC, key, value);

            // send the record to the Kafka topic
            // RecordMetadata tells us where that record has landed in our distributed Kafka topic
            try {
                 kafkaProducer.send(record, new Callback() {
                    @Override
                    public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                        if (e != null) {
                            System.out.println("Error producing to topic " + recordMetadata.topic());
                            e.printStackTrace();
                        }
                        else {
                            logger.info(String.format("Record with (key: %s, value: %s), was sent to (partition: %d, offset: %d",
                            record.key(), record.value(), recordMetadata.partition(), recordMetadata.offset()));
                        }
                    }
                }).get();
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public static Producer<Long, String> createKafkaProducer(String bootstrapServers) {
        Properties properties = new Properties();

        // List of Kafka addresses our producers will use to establish the initial connection to the entire Kafka
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // client ID config which is just a human readable name for our producer which is used mainly for logging purposes Kafka        
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "events-producer");

        // we need to tell the Kafka library how to serialize that object to a binary format that can be    
        // sent over the network so since we are going to create a producer of keys of        
        // type long and values of type string we're going to set the third property        
        // which is the key serializer class to long serializer        
        // and similarly we will set the value serializer class to the name of the        
        // string serializer class and that's it our configuration properties are ready
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        return new KafkaProducer<>(properties);
    }    
}
