package org.miracum.kafka.serializers;

import java.util.Map;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class KafkaFhirSerde implements Serde<IBaseResource> {
  public static final String CONFIG_FHIR_CONTEXT_KEY = "fhir.context";

  private final KafkaFhirSerializer serializer = new KafkaFhirSerializer();
  private final KafkaFhirDeserializer deserializer = new KafkaFhirDeserializer();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
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
