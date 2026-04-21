package io.github.mgrtomaszzurawski.baselinker.client.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;

/**
 * Serializes an {@link Instant} as BaseLinker's Unix epoch seconds (integer).
 */
public final class UnixEpochSecondsInstantSerializer extends JsonSerializer<Instant> {

    @Override
    public void serialize(Instant value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeNumber(value.getEpochSecond());
    }
}
