package io.wahid.publication.util;

import com.opencsv.bean.BeanVerifier;
import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CsvUtil {

    private static final Integer BATCH_SIZE = AppConfig.getIntProperty("batch.size");
    private static final char COLUMN_SEPARATOR = AppConfig.getCharProperty("column.separator");
    private static final String LINE_SEPARATOR = System.lineSeparator();

    private CsvUtil() {
    }

    /**
     * Parses a semicolon-separated CSV file into a list of objects of the given type
     *
     * @param filePath The path of the resource
     * @param charset  The charset to parse for
     * @param type     Object type to populate
     * @param in       The input stream to parse the data from
     * @param verifier BeanVerifier to validate Dto while parsing
     * @param consumer The consumer to process the parsed beans
     * @param <P>      Generic type parameter P for Dto
     */
    public static <P> void parseCsv(String filePath, Charset charset, Class<P> type, InputStream in, BeanVerifier<P> verifier,
                                    Consumer<Stream<P>> consumer) throws IOException {
        if (in == null) {
            throw new IOException(String.format("Could not read resource from file %s", filePath));
        }

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
                        CsvToBeanBuilder<P> csvToBean = getCsvToBeanBuilder(type, chunkReader);
                        if (Objects.nonNull(verifier)) {
                            csvToBean.withVerifier(verifier);
                        }
                        consumer.accept(csvToBean.build().stream());
                    }
                    total += chunk.size();
                }
                System.out.println("total from csv " + filePath + ":" + total);
            }
        }
    }

    public static <P> void parseCsv(String filePath, Charset charset, Class<P> type, BeanVerifier<P> verifier,
                                    Consumer<Stream<P>> consumer) throws IOException {
        try (InputStream in = CsvUtil.class.getClassLoader().getResourceAsStream(filePath)) {
            parseCsv(filePath, charset, type, in, verifier, consumer);
        }
    }

    private static <P> CsvToBeanBuilder<P> getCsvToBeanBuilder(Class<P> type, BufferedReader reader) {
        return new CsvToBeanBuilder<P>(reader)
                .withType(type)
                .withSeparator(COLUMN_SEPARATOR)
                .withIgnoreLeadingWhiteSpace(true)
                .withVerifyReader(true)
                .withThrowExceptions(true);
    }

    /**
     * @param reader The original reader of the file
     * @return Iterable list of Strings
     */
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
                List<String> chunk = new ArrayList<>(CsvUtil.BATCH_SIZE);
                String line;
                while (chunk.size() < CsvUtil.BATCH_SIZE && (line = reader.readLine()) != null) {
                    chunk.add(line);
                }
                return chunk;
            }
        };
    }
}
