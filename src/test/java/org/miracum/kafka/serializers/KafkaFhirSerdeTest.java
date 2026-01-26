package org.miracum.kafka.serializers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.uhn.fhir.context.FhirContext;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.writers.ApprovalTextWriter;
import org.hl7.fhir.r4.model.CodeType;
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

    assertEquals(
        json,
        """
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

  @Test
  void serialize_followedByDeserialize_returnsSamePatient() {
    final var sut = new KafkaFhirSerde();

    var fhirParser = FhirContext.forR4().newJsonParser().setPrettyPrint(true);

    var originalPatient = new Patient();
    originalPatient.setId("test-patient");
    originalPatient.addExtension("test", new CodeType("test"));

    final var bytes = sut.serializer().serialize(null, originalPatient);
    final var deserializedPatient = (Patient) sut.deserializer().deserialize(null, bytes);

    var deserializedPatientJson = fhirParser.encodeResourceToString(deserializedPatient);

    Approvals.verify(
        new ApprovalTextWriter(
            deserializedPatientJson, new Options().forFile().withExtension(".json")));
    sut.close();
  }

  @Test
  void configure_withCustomFhirContext_passesToSerializerAndDeserializer() {
    final var sut = new KafkaFhirSerde();

    var fhirContext = FhirContext.forR4();
    var config = new HashMap<String, Object>();
    config.put(KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY, fhirContext);
    sut.configure(config, false);

    sut.close();
  }

  @Test
  void serializeAndDeserialize_withNullValue_shouldReturnNull() {
    final var sut = new KafkaFhirSerde();

    var serialized = sut.serializer().serialize(null, null);

    assertEquals(null, serialized);

    var deserialized = sut.deserializer().deserialize(null, null);

    assertEquals(null, deserialized);

    sut.close();
  }
}
