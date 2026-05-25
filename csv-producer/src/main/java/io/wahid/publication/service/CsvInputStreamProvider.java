package io.wahid.publication.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import io.wahid.publication.CsvMain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

public class CsvInputStreamProvider {

    private static final Storage storage = StorageOptions.getDefaultInstance().getService();

    private CsvInputStreamProvider() {
    }

    public static InputStream open(String path, boolean fromVolume) throws IOException {
        // 1. GCS Mode: path starts with gs://
        if (path.startsWith("gs://")) {
            return openFromGcs(path);
        }

        // 2. Volume Mode
        if (fromVolume) {
            return new FileInputStream(path);
        }

        // 3. Classpath fallback
        InputStream is = CsvMain.class.getClassLoader().getResourceAsStream(path);

        if (is == null) {
            throw new IOException("Classpath resource not found: " + path);
        }
        return is;
    }

    private static InputStream openFromGcs(String gsPath) throws IOException {
        GoogleCredentials creds = GoogleCredentials.getApplicationDefault();

        System.out.println("CREDS CLASS = " + creds.getClass().getName());

        System.out.println("GOOGLE_APPLICATION_CREDENTIALS = "
                + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
        // Parse gs://bucket/path/file.csv
        String noPrefix = gsPath.substring("gs://".length());
        String bucket = noPrefix.substring(0, noPrefix.indexOf('/'));
        String object = noPrefix.substring(noPrefix.indexOf('/') + 1);

        ReadChannel channel = storage.reader(bucket, object);
        return Channels.newInputStream(channel);  // Stream large files efficiently
    }

    public static void main(String[] arg) throws Exception {
        open("data/autoren.csv", false);
    }
}
