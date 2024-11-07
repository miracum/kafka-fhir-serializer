package org.miracum.kafka.serializers;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

import ca.uhn.fhir.context.FhirContext;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class KafkaFhirSerializer implements Serializer<IBaseResource> {
  private static final FhirContext defaultFhirContext;

  // static initialization blocks are executed by the JVM in a thread-safe manner.
  static {
    defaultFhirContext = FhirContext.forR4();
  }

  private FhirContext fhirContext = defaultFhirContext;

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    if (configs.containsKey(CONFIG_FHIR_CONTEXT_KEY)) {
      fhirContext = (FhirContext) configs.get(CONFIG_FHIR_CONTEXT_KEY);
    }
  }

  @Override
  public byte[] serialize(final String topic, final IBaseResource data) {
    Objects.requireNonNull(data);

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
