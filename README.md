# Kafka Consumers Intro

Introduction to Kafka Consumers.

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

Now let's run our basic java consumer client io.confluent.csta.consumer.basic.BasicConsumer.

If you stop and try to run again you should see no records are polled.

Check consumer groups and you should see:

```text
_confluent-controlcenter-7-5-2-1-command
ConfluentTelemetryReporterSampler-1669283023291673816
io.confluent.csta.consumer.basic.BasicConsumer
```

You can reset the offsets by executing :

```bash
kafka-consumer-groups --bootstrap-server localhost:19092 --delete-offsets  --group io.confluent.csta.consumer.basic.BasicConsumer --topic test-topic
```

If you execute again you should see records being polled.

## File Writer Java Consumer Client

Now let's run our File Writer java consumer client io.confluent.csta.consumer.filewriter.KafkaConsumerApp.

It will generate a file named consumer-records.out. If you are executing from inside an IDE you may need to reload from disk to see the file generated.

## Cleanup

```bash
docker compose down -v
```

