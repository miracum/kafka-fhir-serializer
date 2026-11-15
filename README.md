# Kafka® FHIR® Serializer

[![Apache-2.0 license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/miracum/kafka-fhir-serializer/badge)](https://scorecard.dev/viewer/?uri=github.com/miracum/kafka-fhir-serializer)

Kafka De-/Serializer for FHIR® resources in JSON format.

## Installation

### Gradle

<!-- x-release-please-start-version -->

```groovy
// https://mvnrepository.com/artifact/org.miracum/kafka-fhir-serializer
implementation "org.miracum:kafka-fhir-serializer:3.0.0"
```

<!-- x-release-please-end -->

### Maven

<!-- x-release-please-start-version -->

```xml
<!-- https://mvnrepository.com/artifact/org.miracum/kafka-fhir-serializer -->
<dependency>
    <groupId>org.miracum</groupId>
    <artifactId>kafka-fhir-serializer</artifactId>
    <version>3.0.0</version>
</dependency>
```

<!-- x-release-please-end -->

## Usage

Requires Java 21 or later.

The library provides `KafkaFhirSerializer`, `KafkaFhirDeserializer`, and `KafkaFhirSerde` (for Kafka Streams) in the
`org.miracum.kafka.serializers` package. Resources are encoded as UTF-8 JSON and deserialized as HAPI FHIR
`IBaseResource` objects.

FHIR R4 is used by default. The HAPI FHIR structures aren't included as a dependency, so add the ones for the
FHIR version you are using to your project, for example `ca.uhn.hapi.fhir:hapi-fhir-structures-r4`.

In Spring Boot:

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.miracum.kafka.serializers.KafkaFhirSerializer
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.miracum.kafka.serializers.KafkaFhirDeserializer
```

You can find a real-life usage of this library over at <https://github.com/miracum/fhir-gateway> and
<https://github.com/miracum/kafka-fhir-to-server>.

### Configuration

The serializer, deserializer, and serde are configured using the Kafka client configuration:

- `fhir.version`: the name of the FHIR version to use, such as `R4`, `R4B`, or `R5`. Defaults to `R4`.
- `fhir.context`: the `FhirContext` instance to use. Takes precedence over `fhir.version`.
- `fhir.parser.strict`: whether invalid resources are rejected with an error. Defaults to `true`.

For example, to use FHIR R5 and lenient parsing in Spring Boot:

```yaml
spring:
  kafka:
    consumer:
      value-deserializer: org.miracum.kafka.serializers.KafkaFhirDeserializer
      properties:
        fhir.version: R5
        fhir.parser.strict: false
```

`fhir.context` has to be a `FhirContext` object, so it can only be set programmatically. Use it to share an already
customized context:

```java
var config = new HashMap<String, Object>();
config.put(KafkaFhirSerde.CONFIG_FHIR_CONTEXT_KEY, fhirContext);

var serde = new KafkaFhirSerde();
serde.configure(config, false);
```

If `fhir.parser.strict` is `false`, the parser error handler of the FHIR context is used, which is lenient unless it was
changed.

Resources that can't be serialized or deserialized cause a Kafka `SerializationException`, with HAPI's
`DataFormatException` as its cause.

## Development

The project uses <https://github.com/googleapis/release-please> for versioning.

1. work on fixes and features in a separate fork or branch
1. commit the changes
1. create a pull request against the master branch
1. once reviewed and merged, a new PR should appear to release the changes as a new version
1. merge this PR to create the release on both GitHub and publish to Maven central

See <https://central.sonatype.org/publish/publish-portal-guide/> for a general overview.
