package org.miracum.kafka.serializers;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

import ca.uhn.fhir.context.FhirContext;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * A Kafka serializer for FHIR (Fast Healthcare Interoperability Resources) resources. This
 * serializer is designed to convert FHIR resources into JSON-encoded byte arrays for use with
 * Kafka. It supports FHIR R4 by default but allows customization of the FHIR context through
 * configuration.
 *
 * <p>Usage:
 *
 * <ul>
 *   <li>By default, the serializer uses the R4 FHIR context.
 *   <li>To customize the FHIR context, provide a custom context using the configuration key {@code
 *       CONFIG_FHIR_CONTEXT_KEY}.
 * </ul>
 *
 * <p>Thread Safety:
 *
 * <ul>
 *   <li>The default FHIR context is initialized in a thread-safe manner using a static
 *       initialization block.
 *   <li>However, the serializer instance itself is not thread-safe if the FHIR context is modified
 *       after initialization.
 * </ul>
 *
 * <p>Methods:
 *
 * <ul>
 *   <li>{@link #configure(Map, boolean)}: Configures the serializer, allowing the FHIR context to
 *       be customized.
 *   <li>{@link #serialize(String, IBaseResource)}: Serializes a FHIR resource into a JSON-encoded
 *       byte array.
 *   <li>{@link #serialize(String, Headers, IBaseResource)}: Overloaded method to serialize a FHIR
 *       resource with Kafka headers (delegates to the primary serialize method).
 *   <li>{@link #close()}: Closes the serializer (no-op in this implementation).
 * </ul>
 *
 * <p>Dependencies:
 *
 * <ul>
 *   <li>Requires the HAPI FHIR library for FHIR context and JSON serialization.
 * </ul>
 *
 * @see org.apache.kafka.common.serialization.Serializer
 */
public class KafkaFhirSerializer implements Serializer<IBaseResource> {
  private static final FhirContext defaultFhirContext;

  // static initialization blocks are executed by the JVM in a thread-safe manner.
  static {
    defaultFhirContext = FhirContext.forR4();
  }

  private FhirContext fhirContext = defaultFhirContext;

  /**
   * Default constructor for KafkaFhirSerializer. This constructor is currently unused but is
   * provided for potential future use or to satisfy certain serialization/deserialization
   * frameworks that require a no-argument constructor.
   */
  public KafkaFhirSerializer() {
    // unused
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    if (configs.containsKey(CONFIG_FHIR_CONTEXT_KEY)) {
      fhirContext = (FhirContext) configs.get(CONFIG_FHIR_CONTEXT_KEY);
    }
  }

  @Override
  public byte[] serialize(final String topic, final IBaseResource data) {
    if (data == null) {
      return null;
    }

    return fhirContext
        .newJsonParser()
        .encodeResourceToString(data)
        .getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public byte[] serialize(final String topic, final Headers headers, final IBaseResource data) {
    return serialize(topic, data);
  }

  @Override
  public void close() {
    // empty
  }
}
