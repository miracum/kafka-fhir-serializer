/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package org.miracum.kafka.serializers;

import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class KafkaFhirSerdeTest {
    @Test
    void serialize_withEmptyPatient_returnsNonEmptyBytes() {
        var sut = new KafkaFhirSerde();

        var bytes = sut.serializer().serialize(null, new Patient());

        assertTrue(bytes.length > 0);
    }
}
