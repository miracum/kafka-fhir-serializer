package org.miracum.kafka.serializers;

import ca.uhn.fhir.parser.DataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * A Kafka serializer that encodes HAPI FHIR resources as UTF-8 JSON.
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
public class KafkaFhirSerializer implements Serializer<IBaseResource> {
  private final FhirParserFactory parserFactory = new FhirParserFactory();

  /** Creates a serializer using the default settings until {@link #configure} is called. */
  public KafkaFhirSerializer() {
    // Kafka instantiates serializers reflectively using the no-arg constructor.
  }

  /**
   * Configures the FHIR context and parser used by this serializer.
   *
   * @throws org.apache.kafka.common.config.ConfigException if a setting has an unsupported type or
   *     value
   */
  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    parserFactory.configure(configs);
  }

  /**
   * Encodes the given resource as UTF-8 JSON.
   *
   * @return the encoded resource, or {@code null} if {@code data} is {@code null}
   * @throws SerializationException if the resource can't be encoded
   */
  @Override
  public byte[] serialize(final String topic, final IBaseResource data) {
    if (data == null) {
      return null;
    }

    try {
      return parserFactory
          .newJsonParser()
          .encodeResourceToString(data)
          .getBytes(StandardCharsets.UTF_8);
    } catch (final DataFormatException e) {
      throw new SerializationException("Failed to serialize FHIR resource for topic " + topic, e);
    }
  }
}
