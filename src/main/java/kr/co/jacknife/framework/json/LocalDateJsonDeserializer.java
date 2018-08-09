package kr.co.jacknife.framework.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LocalDateJsonDeserializer extends JsonDeserializer<LocalDate>
{

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        String yyyyMMdd = p.getText();
        LocalDate date = LocalDate.parse(yyyyMMdd, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return date;
    }

}
