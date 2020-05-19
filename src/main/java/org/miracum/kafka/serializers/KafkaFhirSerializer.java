package org.miracum.kafka.serializers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Serializer;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.miracum.kafka.serializers.KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY;

public class KafkaFhirSerializer implements Serializer<IBaseResource> {
    private IParser fhirParser = FhirContext.forR4().newJsonParser();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        if (configs.containsKey(CONFIG_FHIR_CONTEXT_KEY)) {
            var context = (FhirContext) configs.get(CONFIG_FHIR_CONTEXT_KEY);
            this.fhirParser = context.newJsonParser();
        }
    }

    @Override
    public byte[] serialize(String topic, IBaseResource data) {
        if (data == null) {
            return null;
        }

        return fhirParser
                .encodeResourceToString(data)
                .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] serialize(String topic, Headers headers, IBaseResource data) {
        return serialize(topic, data);
    }

    @Override
    public void close() {

    }
}

