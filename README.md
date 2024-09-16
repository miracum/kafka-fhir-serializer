# Kafka® FHIR® Serializer

[![Apache-2.0 license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/miracum/kafka-fhir-serializer/badge)](https://scorecard.dev/viewer/?uri=github.com/miracum/kafka-fhir-serializer)

Kafka De-/Serializer for FHIR® resources in JSON format.

## Installation

### Maven

```xml
<!-- https://mvnrepository.com/artifact/org.miracum/kafka-fhir-serializer -->
<dependency>
    <groupId>org.miracum</groupId>
    <artifactId>kafka-fhir-serializer</artifactId>
    <version>1.0.7</version>
</dependency>
```

### Gradle

```groovy
// https://mvnrepository.com/artifact/org.miracum/kafka-fhir-serializer
implementation "org.miracum:kafka-fhir-serializer:1.0.7"
```

## Usage

You can find a real-life usage of this library over at <https://github.com/miracum/fhir-gateway> and
<https://github.com/miracum/kafka-fhir-to-server>.

In Spring Boot:

```yaml
spring:
  kafka:
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.miracum.kafka.serializers.KafkaFhirSerializer
```

## Development

### Release

See <https://central.sonatype.org/publish/publish-guide/> for a general overview.

1. update version in `build.gradle` to a release version (ie. without the `-SNAPSHOT`) and update the version in the `README.md` as well
1. create a GitHub release
1. the CI should now run and publish a new package
1. follow the instructions over at <https://central.sonatype.org/publish/release/> to promote the package from staging to the release repo
1. increment the version number in `build.gradle` to a new snapshot version (i.e. 2.0.0-SNAPSHOT) and continue development
