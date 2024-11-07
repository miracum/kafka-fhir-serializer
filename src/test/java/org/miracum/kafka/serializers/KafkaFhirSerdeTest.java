package org.miracum.kafka.serializers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class KafkaFhirSerdeTest {

  public KafkaFhirSerdeTest() {
    // empty
  }

  @Test
  void serialize_withEmptyPatient_returnsEmptyPatientJson() {
    final var sut = new KafkaFhirSerde();

    final var bytes = sut.serializer().serialize(null, new Patient());

    var json = new String(bytes, StandardCharsets.UTF_8);

    assertEquals(json, """
        {"resourceType":"Patient"}""");

    sut.close();
  }

  @Test
  void deserialize_withGivenPatientResourceAsJson_returnsPatientObject() {
    final var sut = new KafkaFhirSerde();

    var jsonBytes =
        """
        {
          "resourceType": "Patient",
          "id": "hello-patient"
        }"""
            .getBytes(StandardCharsets.UTF_8);

    final var patient = (Patient) sut.deserializer().deserialize(null, jsonBytes);

    assertEquals("Patient/hello-patient", patient.getId());

    sut.close();
  }
}
