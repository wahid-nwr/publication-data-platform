package io.wahid.publication.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CustomDateSerializer extends JsonSerializer<LocalDate> {

//    private final SimpleDateFormat formatter = DateTimeFormatter.ISO_LOCAL_DATE;

    /*public CustomDateSerializer() {
        this(null);
    }*/

    /*public CustomDateSerializer(Class t) {
        super(t);
    }*/
/*

    @Override
    public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException {
        gen.writeString(formatter.format(value));
    }
*/

    private static final String DTF = "dd MMM yyyy";
    @Override
    public void serialize(final LocalDate value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
        System.out.println(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
        gen.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE));
    }
}
