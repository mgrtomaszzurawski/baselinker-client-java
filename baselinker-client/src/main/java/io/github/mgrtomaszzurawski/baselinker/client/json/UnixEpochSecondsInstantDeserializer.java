package io.github.mgrtomaszzurawski.baselinker.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

/**
 * Deserializes a BaseLinker timestamp (Unix seconds as number or numeric string) into
 * an {@link Instant}. The BaseLinker API never uses ISO-8601 strings for dates.
 */
public final class UnixEpochSecondsInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }
        long seconds = parser.getValueAsLong();
        return Instant.ofEpochSecond(seconds);
    }
}
