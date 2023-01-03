package org.miracum.kafka.serializers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class KafkaFhirSerdeTest {

  public KafkaFhirSerdeTest() {
    // empty
  }

  @Test
  public void serializeWithEmptyPatientReturnsNonEmptyBytes() {
    final var sut = new KafkaFhirSerde();

    final var bytes = sut.serializer().serialize(null, new Patient());

    assertTrue(bytes.length > 0);

    sut.close();
  }
}
