package io.wahid.publication.dto;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum ResourceDesc {
    AUTHOR_RESOURCE("data" + File.separator + "autoren.csv", StandardCharsets.ISO_8859_1),
    BOOK_RESOURCE("data" + File.separator + "buecher.csv", Charset.forName("windows-1252")),
    MAGAZINE_RESOURCE("data" + File.separator + "zeitschriften.csv", StandardCharsets.ISO_8859_1);

    private final String filePath;
    private final Charset charset;

    ResourceDesc(String filePath, Charset charset) {
        this.filePath = filePath;
        this.charset = charset;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public Charset getCharset() {
        return this.charset;
    }
}
