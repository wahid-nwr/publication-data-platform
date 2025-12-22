package io.wahid.publication.util;

import io.wahid.publication.dto.AuthorDto;
import io.wahid.publication.mapper.AuthorMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CsvUtilTest {

    AuthorMapper authorMapper = new AuthorMapper();

    @DisplayName("Should parse the csv successfully and generate author list")
    @ParameterizedTest
    @CsvSource({
            "data/test-authors.csv, UTF-8"
    })
    void parseCsv_withValidCsv_returnsListOfAuthors(String filePath, String charset) throws IOException {
        List<AuthorDto> authors = new ArrayList<>();
        CsvUtil.parseCsv(filePath, Charset.forName(charset), AuthorDto.class, null,
                stream -> stream.forEach(authors::add));
        assertEquals(2, authors.size());
        assertEquals("Jane", authors.get(0).getFirstName());
        assertEquals("Doe", authors.get(0).getLastName());
    }

    @DisplayName("Non existent file should return empty list")
    @ParameterizedTest
    @CsvSource({
            "nonexistent.csv, UTF-8",
    })
    void parseCsv_withNonExistentCsv_returnsEmptyList(String filePath, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        Exception exception = assertThrows(
                IOException.class,
                () -> CsvUtil.parseCsv(filePath, charset, AuthorDto.class, null, null, null));
        assertEquals("Could not read resource from file " + filePath, exception.getMessage());
    }

    @DisplayName("Should throw exception parsing the malformed resource")
    @ParameterizedTest
    @CsvSource({
            "data/malformed.csv, UTF-8",
    })
    void parseCsv_withMalformedCsv_throwsException(String filePath, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        Exception exception = assertThrows(
                NullPointerException.class,
                () -> CsvUtil.parseCsv(filePath, charset, AuthorDto.class, null, null, null));

        assertNull(exception.getMessage());
    }

    @DisplayName("Should throw exception parsing the malformed resource")
    @ParameterizedTest
    @CsvSource({
            "data/test-authors-truncated.csv, UTF-8",
    })
    void parseCsv_withTruncatedCsv_throwsException(String filePath, String charsetName) throws Exception {
        try (InputStream mockInputStream = mock(InputStream.class)) {
            when(mockInputStream.read()).thenThrow(new IOException("Simulated IO"));
        }

        Exception exception = assertThrows(
                IOException.class,
                () -> CsvUtil.parseCsv(filePath, Charset.forName(charsetName),
                        AuthorDto.class, null, null, null));

        assertEquals("Could not read resource from file data/test-authors-truncated.csv", exception.getMessage());
    }

    @DisplayName("Should throw exception parsing the file for different charset")
    @ParameterizedTest
    @CsvSource({
            "data/utf16-authors.csv, UTF-8",
    })
    void parseCsv_withDifferentCharsetCsv_throwsException(String filePath, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        Exception exception = assertThrows(
                RuntimeException.class,
                () -> CsvUtil.parseCsv(filePath, charset, AuthorDto.class, null, null, null));
        assertNull(exception.getMessage());
    }
}
