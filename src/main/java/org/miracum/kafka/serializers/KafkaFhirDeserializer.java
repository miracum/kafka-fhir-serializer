package org.miracum.kafka.serializers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

public class KafkaFhirDeserializer implements Deserializer<IBaseResource> {
    private IParser fhirParser = FhirContext.forR4().newJsonParser();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (configs.containsKey(CONFIG_FHIR_CONTEXT_KEY)) {
            var context = (FhirContext) configs.get(CONFIG_FHIR_CONTEXT_KEY);
            this.fhirParser = context.newJsonParser();
        }
    }

    @Override
    public IBaseResource deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        return fhirParser.parseResource(new ByteArrayInputStream(data));
    }

    @Override
    public IBaseResource deserialize(String topic, Headers headers, byte[] data) {
        return deserialize(topic, data);
    }

    @Override
    public void close() {

    }
}
