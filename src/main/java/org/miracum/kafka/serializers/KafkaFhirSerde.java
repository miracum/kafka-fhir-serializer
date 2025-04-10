package org.miracum.kafka.serializers;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * A Kafka Serializer/Deserializer (Serde) implementation for FHIR resources. This class provides
 * functionality to serialize and deserialize FHIR resources (IBaseResource) for use with Apache
 * Kafka.
 *
 * <p>The Serde can be configured using the following configuration parameter:
 *
 * <ul>
 *   <li>{@value #CONFIG_FHIR_CONTEXT_KEY}: The FHIR context configuration key
 * </ul>
 *
 * <p>This implementation manages both the serialization and deserialization of FHIR resources
 * through {@link KafkaFhirSerializer} and {@link KafkaFhirDeserializer} respectively.
 *
 * @see KafkaFhirSerializer
 * @see KafkaFhirDeserializer
 * @see IBaseResource
 * @see Serde
 */
public class KafkaFhirSerde implements Serde<IBaseResource> {
  /**
   * Configuration key for the FHIR context. This key is used to retrieve the FHIR context from the
   * configuration map.
   */
  public static final String CONFIG_FHIR_CONTEXT_KEY = "fhir.context";

  private final KafkaFhirSerializer serializer = new KafkaFhirSerializer();
  private final KafkaFhirDeserializer deserializer = new KafkaFhirDeserializer();

  /**
   * Default constructor for the KafkaFhirSerde class. This constructor initializes a new instance
   * of the KafkaFhirSerde without any specific configuration or parameters.
   */
  public KafkaFhirSerde() {
    // unused
  }

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    this.serializer.configure(configs, isKey);
    this.deserializer.configure(configs, isKey);
  }

  @Override
  public void close() {
    this.serializer.close();
    this.deserializer.close();
  }

  @Override
  public Serializer<IBaseResource> serializer() {
    return this.serializer;
  }

  @Override
  public Deserializer<IBaseResource> deserializer() {
    return this.deserializer;
  }
}
