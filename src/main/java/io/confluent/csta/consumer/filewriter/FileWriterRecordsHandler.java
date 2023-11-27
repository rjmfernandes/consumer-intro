package io.confluent.csta.consumer.filewriter;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class FileWriterRecordsHandler implements ConsumerRecordsHandler<String,String> {
    private static final Logger log = LoggerFactory.getLogger(FileWriterRecordsHandler.class);
    private Path path;

    public FileWriterRecordsHandler(Path path) {
        this.path=path;
    }

    @Override
    public void process(final ConsumerRecords<String,String> consumerRecords) {
        final List<String> valueList = new ArrayList<>();
        consumerRecords.forEach(record ->  valueList.add(record.value()) );
        if (!valueList.isEmpty()) {
            try {
                Files.write(path, valueList, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
