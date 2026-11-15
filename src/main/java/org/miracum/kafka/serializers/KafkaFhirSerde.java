package org.miracum.kafka.serializers;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * A Kafka {@link Serde} for HAPI FHIR resources encoded as JSON, combining {@link
 * KafkaFhirSerializer} and {@link KafkaFhirDeserializer}.
 *
 * <p>The serde, as well as the serializer and deserializer on their own, support the following
 * configuration keys:
 *
 * <ul>
 *   <li>{@value #CONFIG_FHIR_CONTEXT_KEY}: the {@code FhirContext} instance to use
 *   <li>{@value #CONFIG_FHIR_VERSION_KEY}: the name of the FHIR version to use, if no context is
 *       set
 *   <li>{@value #CONFIG_FHIR_PARSER_STRICT_KEY}: whether invalid resources are rejected
 * </ul>
 *
 * <p>If neither a context nor a version is configured, FHIR R4 is used.
 */
public class KafkaFhirSerde implements Serde<IBaseResource> {
  /**
   * Configuration key for the {@code ca.uhn.fhir.context.FhirContext} instance to use. Since the
   * value has to be an object, this can only be set programmatically. Takes precedence over {@value
   * #CONFIG_FHIR_VERSION_KEY}.
   */
  public static final String CONFIG_FHIR_CONTEXT_KEY = "fhir.context";

  /**
   * Configuration key for the FHIR version to use. The value is either the case-insensitive name of
   * a {@code ca.uhn.fhir.context.FhirVersionEnum} constant, such as {@code R4}, {@code R4B} or
   * {@code R5}, or the constant itself. The matching {@code hapi-fhir-structures-*} library has to
   * be on the classpath. Defaults to {@code R4}.
   */
  public static final String CONFIG_FHIR_VERSION_KEY = "fhir.version";

  /**
   * Configuration key for whether the parser rejects invalid resources, as a boolean or the string
   * {@code true} or {@code false}. If {@code false}, the parser error handler of the FHIR context
   * is used, which is lenient unless it was changed. Defaults to {@code true}.
   */
  public static final String CONFIG_FHIR_PARSER_STRICT_KEY = "fhir.parser.strict";

  private final KafkaFhirSerializer serializer = new KafkaFhirSerializer();
  private final KafkaFhirDeserializer deserializer = new KafkaFhirDeserializer();

  /** Creates a serde using the default settings until {@link #configure} is called. */
  public KafkaFhirSerde() {
    // Kafka instantiates serdes reflectively using the no-arg constructor.
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
