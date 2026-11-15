package org.miracum.kafka.serializers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.parser.DataFormatException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.errors.SerializationException;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.writers.ApprovalTextWriter;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class KafkaFhirSerdeTest {
  private static final String TOPIC = "fhir.test";

  // accepted by the lenient parser but rejected by the strict one
  private static final byte[] INVALID_PATIENT_JSON =
      """
      {
        "resourceType": "Patient",
        "id": "hello-patient",
        "extension": [{
            "url": null
        }]
      }"""
          .getBytes(StandardCharsets.UTF_8);

  public KafkaFhirSerdeTest() {
    // empty
  }

  @Test
  void serialize_withEmptyPatient_returnsEmptyPatientJson() {
    try (var sut = new KafkaFhirSerde()) {
      final var bytes = sut.serializer().serialize(TOPIC, new Patient());

      assertEquals(
          """
          {"resourceType":"Patient"}""",
          new String(bytes, StandardCharsets.UTF_8));
    }
  }

  @Test
  void deserialize_withGivenPatientResourceAsJson_returnsPatientObject() {
    var jsonBytes =
        """
        {
          "resourceType": "Patient",
          "id": "hello-patient"
        }"""
            .getBytes(StandardCharsets.UTF_8);

    try (var sut = new KafkaFhirSerde()) {
      final var patient = (Patient) sut.deserializer().deserialize(TOPIC, jsonBytes);

      assertEquals("Patient/hello-patient", patient.getId());
    }
  }

  @Test
  void serialize_followedByDeserialize_returnsSamePatient() {
    var fhirParser = FhirContext.forR4Cached().newJsonParser().setPrettyPrint(true);

    var originalPatient = new Patient();
    originalPatient.setId("test-patient");
    originalPatient.addExtension("test", new CodeType("test"));

    try (var sut = new KafkaFhirSerde()) {
      final var bytes = sut.serializer().serialize(TOPIC, originalPatient);
      final var deserializedPatient = (Patient) sut.deserializer().deserialize(TOPIC, bytes);

      var deserializedPatientJson = fhirParser.encodeResourceToString(deserializedPatient);

      Approvals.verify(
          new ApprovalTextWriter(
              deserializedPatientJson, new Options().forFile().withExtension(".json")));
    }
  }

  @Test
  void serializeAndDeserialize_withNullValue_shouldReturnNull() {
    try (var sut = new KafkaFhirSerde()) {
      assertNull(sut.serializer().serialize(TOPIC, null));
      assertNull(sut.deserializer().deserialize(TOPIC, null));
    }
  }

  @Test
  void serialize_withResourceOfOtherFhirVersionThanConfigured_shouldThrowException() {
    // guards the tests below: an R5 resource is only supported if R5 is actually configured.
    try (var sut = new KafkaFhirSerde()) {
      final var r5Patient = new org.hl7.fhir.r5.model.Patient();

      assertThrows(RuntimeException.class, () -> sut.serializer().serialize(TOPIC, r5Patient));
    }
  }

  @Test
  void configure_withCustomFhirContext_isUsedBySerializerAndDeserializer() {
    try (var sut = new KafkaFhirSerde()) {
      sut.configure(
          Map.of(KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY, FhirContext.forR5Cached()), false);

      assertRoundTripsR5Patient(sut);
    }
  }

  @Test
  void configure_withFhirVersionName_isUsedBySerializerAndDeserializer() {
    try (var sut = new KafkaFhirSerde()) {
      // lower-case and as a string, as it would be set in a properties file
      sut.configure(Map.of(KafkaFhirSerde.CONFIG_FHIR_VERSION_KEY, "r5"), false);

      assertRoundTripsR5Patient(sut);
    }
  }

  @Test
  void configure_withFhirVersionEnum_isUsedBySerializerAndDeserializer() {
    try (var sut = new KafkaFhirSerde()) {
      sut.configure(Map.of(KafkaFhirSerde.CONFIG_FHIR_VERSION_KEY, FhirVersionEnum.R5), false);

      assertRoundTripsR5Patient(sut);
    }
  }

  @Test
  void configure_withFhirContextAndVersion_prefersFhirContext() {
    try (var sut = new KafkaFhirSerde()) {
      sut.configure(
          Map.of(
              KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY,
              FhirContext.forR5Cached(),
              KafkaFhirSerde.CONFIG_FHIR_VERSION_KEY,
              "R4"),
          false);

      assertRoundTripsR5Patient(sut);
    }
  }

  @Test
  void configure_withoutFhirSettings_usesR4() {
    try (var sut = new KafkaFhirSerde()) {
      sut.configure(Map.of("bootstrap.servers", "localhost:9092"), false);

      final var bytes = sut.serializer().serialize(TOPIC, new Patient().setId("r4-patient"));

      assertInstanceOf(Patient.class, sut.deserializer().deserialize(TOPIC, bytes));
    }
  }

  @Test
  void configure_withFhirContextOfWrongType_shouldThrowConfigException() {
    try (var sut = new KafkaFhirSerde()) {
      final var config = Map.of(KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY, "R4");

      assertThrows(ConfigException.class, () -> sut.configure(config, false));
    }
  }

  @Test
  void configure_withUnknownFhirVersion_shouldThrowConfigException() {
    try (var sut = new KafkaFhirSerde()) {
      final var unknownName = Map.of(KafkaFhirSerde.CONFIG_FHIR_VERSION_KEY, "R99");
      final var wrongType = Map.of(KafkaFhirSerde.CONFIG_FHIR_VERSION_KEY, 4);

      assertThrows(ConfigException.class, () -> sut.configure(unknownName, false));
      assertThrows(ConfigException.class, () -> sut.configure(wrongType, false));
    }
  }

  @Test
  void configure_withInvalidStrictSetting_shouldThrowConfigException() {
    try (var sut = new KafkaFhirSerde()) {
      final var notBoolean = Map.of(KafkaFhirSerde.CONFIG_FHIR_PARSER_STRICT_KEY, "yes");
      final var wrongType = Map.of(KafkaFhirSerde.CONFIG_FHIR_PARSER_STRICT_KEY, 1);

      assertThrows(ConfigException.class, () -> sut.configure(notBoolean, false));
      assertThrows(ConfigException.class, () -> sut.configure(wrongType, false));
    }
  }

  @Test
  void deserialize_withStrictParsingFailingResource_shouldThrowException() {
    try (var sut = new KafkaFhirSerde()) {
      final var exception =
          assertThrows(
              SerializationException.class,
              () -> sut.deserializer().deserialize(TOPIC, INVALID_PATIENT_JSON));

      assertInstanceOf(DataFormatException.class, exception.getCause());
    }
  }

  @Test
  void serialize_withStrictEncodingFailingResource_shouldThrowException() {
    var patient = new Patient();
    // an extension requires a url
    patient.addExtension(new Extension().setValue(new CodeType("test")));

    try (var sut = new KafkaFhirSerde()) {
      final var exception =
          assertThrows(
              SerializationException.class, () -> sut.serializer().serialize(TOPIC, patient));

      assertInstanceOf(DataFormatException.class, exception.getCause());
    }
  }

  @Test
  void deserialize_withStrictParsingExplicitlyEnabled_shouldThrowException() {
    try (var sut = new KafkaFhirSerde()) {
      sut.configure(Map.of(KafkaFhirSerde.CONFIG_FHIR_PARSER_STRICT_KEY, "TRUE"), false);

      assertThrows(
          SerializationException.class,
          () -> sut.deserializer().deserialize(TOPIC, INVALID_PATIENT_JSON));
    }
  }

  @Test
  void deserialize_withStrictParsingDisabled_returnsLenientlyParsedResource() {
    // both as a boolean and as a string, as it would be set in a properties file
    for (final Object disabled : new Object[] {false, "false"}) {
      try (var sut = new KafkaFhirSerde()) {
        sut.configure(Map.of(KafkaFhirSerde.CONFIG_FHIR_PARSER_STRICT_KEY, disabled), false);

        final var patient = (Patient) sut.deserializer().deserialize(TOPIC, INVALID_PATIENT_JSON);

        assertEquals("Patient/hello-patient", patient.getId());
      }
    }
  }

  private static void assertRoundTripsR5Patient(final KafkaFhirSerde sut) {
    final var r5Patient = new org.hl7.fhir.r5.model.Patient();
    r5Patient.setId("r5-patient");

    final var bytes = sut.serializer().serialize(TOPIC, r5Patient);

    assertEquals(
        """
        {"resourceType":"Patient","id":"r5-patient"}""",
        new String(bytes, StandardCharsets.UTF_8));

    final var deserialized = sut.deserializer().deserialize(TOPIC, bytes);

    assertInstanceOf(org.hl7.fhir.r5.model.Patient.class, deserialized);
  }
}
