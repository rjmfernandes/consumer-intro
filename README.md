# Kafka Consumers Intro

Introduction to Kafka Consumers.

## Cleanup

```bash
docker compose down -v
```

## Setup

### Start Docker Compose

```bash
docker compose up -d
```

### Check Control Center

Open http://localhost:9021 and check cluster is healthy

### List topics

You will need to have on PATH the confluent binaries:
https://docs.confluent.io/confluent-cli/current/install.html#cp-installation-package

```bash
kafka-topics --bootstrap-server localhost:19092 --list
```

You should get internal topics:

```text
__consumer_offsets
_confluent-command
_confluent-telemetry-metrics
_confluent_balancer_api_state
```

### Create test topic

```bash
kafka-topics --bootstrap-server localhost:19092 --topic test-topic --create --partitions 12 --replication-factor 3
```

You can describe the topic to check how partitions got distributed:

```bash
kafka-topics --bootstrap-server localhost:19092 --topic test-topic --describe
```

You will get something like this:

```text
Topic: test-topic	TopicId: EsLelCnEQq2JLB5roqJL4A	 PartitionCount: 12	 ReplicationFactor: 3	Configs:
	Topic: test-topic	Partition: 0	Leader: 1	 Replicas: 1,3,2	 Isr: 1,3,2	 Offline:
	Topic: test-topic	Partition: 1	Leader: 2	 Replicas: 2,1,3	 Isr: 2,1,3	 Offline:
	Topic: test-topic	Partition: 2	Leader: 3	 Replicas: 3,2,1	 Isr: 3,2,1	 Offline:
	Topic: test-topic	Partition: 3	Leader: 1	 Replicas: 1,2,3	 Isr: 1,2,3	 Offline:
	Topic: test-topic	Partition: 4	Leader: 2	 Replicas: 2,3,1	 Isr: 2,3,1	 Offline:
	Topic: test-topic	Partition: 5	Leader: 3	 Replicas: 3,1,2	 Isr: 3,1,2	 Offline:
	Topic: test-topic	Partition: 6	Leader: 1	 Replicas: 1,3,2	 Isr: 1,3,2	 Offline:
	Topic: test-topic	Partition: 7	Leader: 2	 Replicas: 2,1,3	 Isr: 2,1,3	 Offline:
	Topic: test-topic	Partition: 8	Leader: 3	 Replicas: 3,2,1	 Isr: 3,2,1	 Offline:
	Topic: test-topic	Partition: 9	Leader: 1	 Replicas: 1,2,3	 Isr: 1,2,3	 Offline:
	Topic: test-topic	Partition: 10	Leader: 2	 Replicas: 2,3,1	 Isr: 2,3,1	 Offline:
	Topic: test-topic	Partition: 11	Leader: 3	 Replicas: 3,1,2	 Isr: 3,1,2	 Offline:
```

### Start Producer

To produce some test messages:

```bash
kafka-producer-perf-test --topic test-topic --num-records 600000 --record-size 100 --throughput 10000 --producer-props bootstrap.servers=localhost:19092
```

## Command Consumer

We could consume the messages from the command line:

```bash
kafka-console-consumer --bootstrap-server localhost:19092 --topic test-topic --from-beginning --property print.timestamp=true --property print.value=true
```

And while consumer is running we can also check in another shell the consumer groups:

```bash
kafka-consumer-groups --list --bootstrap-server localhost:19092
```

You should see a console-consumer-XXXXX was automatically created for your kafka-console-consumer:

```text
ConfluentTelemetryReporterSampler-1669283023291673816
_confluent-controlcenter-7-5-2-1-command
console-consumer-18016
```

## Basic Java Consumer Client

Now let's run our basic java consumer client io.confluent.csta.consumer.basic.BasicConsumer:

```java

        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        KafkaConsumer<String, String> consumer = new KafkaConsumer(properties);

        try {
            consumer.subscribe(Arrays.asList(TOPIC));

            final Thread mainThread = Thread.currentThread();

            // adding the shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    log.info("Detected a shutdown, let's exit by calling consumer.wakeup()...");
                    consumer.wakeup();

                    // join the main thread to allow the execution of the code in the main thread
                    try {
                        mainThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });

            while (true) {
                log.info("Polling records...");
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                log.info("Records processing...");
                for (ConsumerRecord<String, String> record : records) {
                    log.info(
                            "Message with Value: " + record.value() +
                                    "\n\tPartition: " + record.partition() +
                                    "\n\tOffset: " + record.offset()
                    );
                }
            }
        } catch (WakeupException e) {
            log.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        } catch (Exception e) {
            log.error("Unexpected exception", e);
        } finally {
            log.info("The consumer is now gracefully closed.");
            consumer.close();
        }

```

