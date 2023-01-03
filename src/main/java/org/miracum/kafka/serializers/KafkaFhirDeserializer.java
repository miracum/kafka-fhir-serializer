package org.miracum.kafka.serializers;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import java.io.ByteArrayInputStream;
import java.util.Map;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class KafkaFhirDeserializer implements Deserializer<IBaseResource> {
  private IParser fhirParser = FhirContext.forR4().newJsonParser();

  @Override
  public void configure(final Map<String, ?> configs, final boolean isKey) {
    if (configs.containsKey(CONFIG_FHIR_CONTEXT_KEY)) {
      final var context = (FhirContext) configs.get(CONFIG_FHIR_CONTEXT_KEY);
      this.fhirParser = context.newJsonParser();
    }
  }

  @Override
  public IBaseResource deserialize(final String topic, final byte[] data) {
    if (data == null) {
      return null;
    }

    return fhirParser.parseResource(new ByteArrayInputStream(data));
  }

  @Override
  public IBaseResource deserialize(final String topic, final Headers headers, final byte[] data) {
    return deserialize(topic, data);
  }

  @Override
  public void close() {}
}
