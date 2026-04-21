package io.github.mgrtomaszzurawski.baselinker.client.json;

import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Instant;

/**
 * Jackson module registering BaseLinker-specific (de)serializers. Currently handles
 * {@link Instant} as Unix epoch seconds.
 */
public final class BaselinkerJacksonModule extends SimpleModule {

    private static final String MODULE_NAME = "BaselinkerJacksonModule";

    public BaselinkerJacksonModule() {
        super(MODULE_NAME);
        addSerializer(Instant.class, new UnixEpochSecondsInstantSerializer());
        addDeserializer(Instant.class, new UnixEpochSecondsInstantDeserializer());
    }
}
