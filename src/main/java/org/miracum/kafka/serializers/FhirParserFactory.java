package org.miracum.kafka.serializers;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;
import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_PARSER_STRICT_KEY;
import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_VERSION_KEY;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.IParserErrorHandler;
import ca.uhn.fhir.parser.StrictErrorHandler;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import org.apache.kafka.common.config.ConfigException;

/**
 * Resolves the FHIR context and parser settings shared by {@link KafkaFhirSerializer} and {@link
 * KafkaFhirDeserializer} and creates accordingly configured JSON parsers.
 */
final class FhirParserFactory {
  // the handler is stateless, so a single instance can be shared by all parsers.
  private static final IParserErrorHandler STRICT_ERROR_HANDLER = new StrictErrorHandler();

  // volatile, since Kafka clients may configure and use a (de)serializer from different threads.
  private volatile FhirContext fhirContext;
  private volatile boolean strict = true;

  FhirParserFactory() {
    // the FHIR context is resolved in configure() or on first use.
  }

  /**
   * Applies the settings found in the given client configuration.
   *
   * @param configs the Kafka client configuration
   * @throws ConfigException if one of the settings has an unsupported type or value
   */
  void configure(final Map<String, ?> configs) {
    final var configuredContext = resolveContext(configs);
    if (configuredContext != null) {
      fhirContext = configuredContext;
    }

    final var strictValue = configs.get(CONFIG_FHIR_PARSER_STRICT_KEY);
    if (strictValue != null) {
      strict = parseStrict(strictValue);
    }

    // fail when the client is constructed instead of when the first record is processed,
    // e.g. if the structures for the default FHIR version aren't on the classpath.
    getFhirContext();
  }

  /**
   * Creates a new JSON parser. Parsers are cheap to create but not thread-safe, so a new one is
   * used for every record.
   *
   * @return a JSON parser for the configured FHIR context
   */
  IParser newJsonParser() {
    final var parser = getFhirContext().newJsonParser();
    if (strict) {
      parser.setParserErrorHandler(STRICT_ERROR_HANDLER);
    }
    return parser;
  }

  private FhirContext getFhirContext() {
    var context = fhirContext;
    if (context == null) {
      // Deliberately not created in a static initializer: the R4 structures are only required
      // if no other context or version is configured. The cached context is also shared with
      // any other code using FhirContext.forR4Cached(), so it's only ever created once.
      context = FhirContext.forR4Cached();
      fhirContext = context;
    }
    return context;
  }

  private static FhirContext resolveContext(final Map<String, ?> configs) {
    final var context = configs.get(CONFIG_FHIR_CONTEXT_KEY);
    if (context instanceof FhirContext configuredContext) {
      return configuredContext;
    }

    if (context != null) {
      throw new ConfigException(
          CONFIG_FHIR_CONTEXT_KEY,
          context,
          "must be an instance of "
              + FhirContext.class.getName()
              + ". Use '"
              + CONFIG_FHIR_VERSION_KEY
              + "' to select a FHIR version by name instead.");
    }

    final var version = configs.get(CONFIG_FHIR_VERSION_KEY);
    if (version == null) {
      return null;
    }

    return FhirContext.forCached(parseVersion(version));
  }

  private static FhirVersionEnum parseVersion(final Object value) {
    if (value instanceof FhirVersionEnum version) {
      return version;
    }

    if (value instanceof String name) {
      try {
        return FhirVersionEnum.valueOf(name.trim().toUpperCase(Locale.ROOT));
      } catch (final IllegalArgumentException e) {
        // reported below
      }
    }

    throw new ConfigException(
        CONFIG_FHIR_VERSION_KEY,
        value,
        "must be one of " + Arrays.toString(FhirVersionEnum.values()));
  }

  private static boolean parseStrict(final Object value) {
    if (value instanceof Boolean enabled) {
      return enabled;
    }

    if (value instanceof String text) {
      if ("true".equalsIgnoreCase(text.trim())) {
        return true;
      }
      if ("false".equalsIgnoreCase(text.trim())) {
        return false;
      }
    }

    throw new ConfigException(CONFIG_FHIR_PARSER_STRICT_KEY, value, "must be true or false");
  }
}
