package io.wahid.publication.service;

import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvToBeanBuilder;
import io.wahid.publication.mapper.DomainObjectMapper;
import io.wahid.publication.util.KafkaUtil;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class CsvToDtoProducer<D, M, E> {

    private static final Logger LOGGER = Logger.getLogger(CsvToDtoProducer.class.getName());
    private static final int BATCH_SIZE = 1000;
    private static final char COLUMN_SEPARATOR = ';';
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private final KafkaProducer<String, E> producer;
    private final String topic;

    public CsvToDtoProducer(String bootstrapServers, String topic) {
        this.topic = topic;
        Properties props = KafkaUtil.getProducerProperties(bootstrapServers);
        this.producer = new KafkaProducer<>(props);
    }

    private static Iterable<List<String>> chunkBufferedReader(BufferedReader reader) {
        return () -> new Iterator<>() {
            List<String> nextChunk = null;

            @Override
            public boolean hasNext() {
                try {
                    if (nextChunk == null) {
                        nextChunk = readChunk();
                    }
                    return !nextChunk.isEmpty();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public List<String> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more chunks available");
                }
                List<String> result = nextChunk;
                nextChunk = null;
                return result;
            }

            private List<String> readChunk() throws IOException {
                List<String> chunk = new ArrayList<>(BATCH_SIZE);
                String line;
                while (chunk.size() < BATCH_SIZE && (line = reader.readLine()) != null) {
                    chunk.add(line);
                }
                return chunk;
            }
        };
    }

    private static <T> CsvToBeanBuilder<T> getCsvToBeanBuilder(Class<T> type, BufferedReader reader) {
        return new CsvToBeanBuilder<T>(reader)
                .withType(type)
                .withSeparator(COLUMN_SEPARATOR)
                .withIgnoreLeadingWhiteSpace(true)
                .withVerifyReader(true)
                .withThrowExceptions(true);
    }

    public void parseCsv(String filePath, Charset charset, Class<D> type, InputStream in, BeanVerifier<D> verifier,
                         DomainObjectMapper<D, M, E> mapper) throws IOException {

        if (in == null) {
            throw new IOException(String.format("Could not read resource from file %s", filePath));
        }

        Consumer<Stream<D>> consumer = batchConsumer(mapper);
        int total = 0;
        try (InputStreamReader isr = new InputStreamReader(in, charset)) {

            try (BufferedReader reader = new BufferedReader(isr)) {
                // We don't need the header line as we are mapping beans with column position, also making chunks with only rows
                // So, we want to consume the header row here
                if (Objects.isNull(reader.readLine())) {
                    throw new IOException(String.format("File does not contain header %s", filePath));
                }

                for (List<String> chunk : chunkBufferedReader(reader)) {
                    try (BufferedReader chunkReader = new BufferedReader(new StringReader(String.join(LINE_SEPARATOR, chunk)))) {
                        CsvToBeanBuilder<D> csvToBean = getCsvToBeanBuilder(type, chunkReader);
                        if (Objects.nonNull(verifier)) {
                            csvToBean.withVerifier(verifier);
                        }
                        consumer.accept(csvToBean.build().stream());
                    }
                    total += chunk.size();
                }
                LOGGER.log(Level.INFO, "total from csv {0} : {1}", new Object[]{filePath, total});
            }
        }
    }

    private Consumer<Stream<D>> batchConsumer(DomainObjectMapper<D, M, E> mapper) {
        return stream -> {
            AtomicLong counter = new AtomicLong(0);
            stream.forEach(dto -> {
                E event = mapper.toEvent(dto);
                ProducerRecord<String, E> producerRecord = new ProducerRecord<>(topic, UUID.randomUUID().toString(), event);
                producer.send(producerRecord, (metadata, ex) -> {
                    if (ex != null) {
                        LOGGER.log(Level.WARNING, "Failed to send dto creation event: {0}", ex.getMessage());
                    } else {
                        long count = counter.incrementAndGet();
                        if (count % 5 == 0) {
                            LOGGER.log(Level.INFO, "Sent {0} dto creation events", count);
                        }
                    }
                });
            });
            producer.flush();
        };
    }

    public void close() {
        producer.close();
    }
}