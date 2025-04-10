package org.miracum.kafka.serializers;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

import ca.uhn.fhir.context.FhirContext;
import java.io.ByteArrayInputStream;
import java.util.Map;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * A Kafka deserializer for FHIR resources that converts JSON-encoded byte arrays into HAPI FHIR
 * resource objects.
 *
 * <p>This deserializer supports FHIR R4 resources by default but can be configured to use a
 * different FHIR context through the configuration map provided during setup.
 *
 * <p>The deserializer implements the following Kafka Deserializer interface methods:
 *
 * <ul>
 *   <li>{@link #configure(Map, boolean)} - Configures the deserializer with a custom FHIR context
 *       if provided
 *   <li>{@link #deserialize(String, byte[])} - Converts byte array into FHIR resource
 *   <li>{@link #deserialize(String, Headers, byte[])} - Overloaded method that includes headers
 *   <li>{@link #close()} - Cleanup method (no-op in this implementation)
 * </ul>
 *
 * @see org.apache.kafka.common.serialization.Deserializer
 * @see org.hl7.fhir.instance.model.api.IBaseResource
 * @see ca.uhn.fhir.context.FhirContext
 */
public class KafkaFhirDeserializer implements Deserializer<IBaseResource> {
  private static final FhirContext defaultFhirContext;

  // static initialization blocks are executed by the JVM in a thread-safe manner.
  static {
    defaultFhirContext = FhirContext.forR4();
  }

  private FhirContext fhirContext = defaultFhirContext;

  /**
   * Default constructor for the KafkaFhirDeserializer class. This constructor initializes a new
   * instance of the KafkaFhirDeserializer without any specific configuration or parameters.
   */
  public KafkaFhirDeserializer() {
    // unused
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    if (configs.containsKey(CONFIG_FHIR_CONTEXT_KEY)) {
      fhirContext = (FhirContext) configs.get(CONFIG_FHIR_CONTEXT_KEY);
    }
  }

  @Override
  public IBaseResource deserialize(final String topic, final byte[] data) {
    if (data == null) {
      return null;
    }

    return fhirContext.newJsonParser().parseResource(new ByteArrayInputStream(data));
  }

  @Override
  public IBaseResource deserialize(final String topic, final Headers headers, final byte[] data) {
    return deserialize(topic, data);
  }

  @Override
  public void close() {
    // empty
  }
}
