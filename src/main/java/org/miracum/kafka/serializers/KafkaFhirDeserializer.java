package org.miracum.kafka.serializers;

import ca.uhn.fhir.parser.DataFormatException;
import java.io.ByteArrayInputStream;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * A Kafka deserializer that parses UTF-8 JSON into HAPI FHIR resources.
 *
 * <p>FHIR R4 is used by default, which requires {@code hapi-fhir-structures-r4} on the classpath. A
 * different FHIR version or context, and the strictness of the parser, can be set using the
 * configuration keys defined in {@link KafkaFhirSerde}.
 *
 * <p>Instances are safe for use by multiple threads once configured.
 *
 * @see KafkaFhirSerde#CONFIG_FHIR_CONTEXT_KEY
 * @see KafkaFhirSerde#CONFIG_FHIR_VERSION_KEY
 * @see KafkaFhirSerde#CONFIG_FHIR_PARSER_STRICT_KEY
 */
public class KafkaFhirDeserializer implements Deserializer<IBaseResource> {
  private final FhirParserFactory parserFactory = new FhirParserFactory();

  /** Creates a deserializer using the default settings until {@link #configure} is called. */
  public KafkaFhirDeserializer() {
    // Kafka instantiates deserializers reflectively using the no-arg constructor.
  }

  /**
   * Configures the FHIR context and parser used by this deserializer.
   *
   * @throws org.apache.kafka.common.config.ConfigException if a setting has an unsupported type or
   *     value
   */
  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    parserFactory.configure(configs);
  }

  /**
   * Parses the given UTF-8 JSON into a FHIR resource.
   *
   * @return the parsed resource, or {@code null} if {@code data} is {@code null}
   * @throws SerializationException if the data isn't a valid FHIR resource
   */
  @Override
  public IBaseResource deserialize(final String topic, final byte[] data) {
    if (data == null) {
      return null;
    }

    try {
      return parserFactory.newJsonParser().parseResource(new ByteArrayInputStream(data));
    } catch (final DataFormatException e) {
      throw new SerializationException(
          "Failed to deserialize FHIR resource from topic " + topic, e);
    }
  }
}
