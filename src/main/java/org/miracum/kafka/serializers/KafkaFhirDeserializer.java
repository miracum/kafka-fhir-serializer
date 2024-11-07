package org.miracum.kafka.serializers;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

import ca.uhn.fhir.context.FhirContext;
import java.io.ByteArrayInputStream;
import java.util.Map;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class KafkaFhirDeserializer implements Deserializer<IBaseResource> {
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
