# Kafka® FHIR® Serializer

[![Apache-2.0 license](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/miracum/kafka-fhir-serializer/badge)](https://scorecard.dev/viewer/?uri=github.com/miracum/kafka-fhir-serializer)

Kafka De-/Serializer for FHIR® resources in JSON format.

## Installation

### Gradle

<!-- x-release-please-start-version -->

```groovy
// https://mvnrepository.com/artifact/org.miracum/kafka-fhir-serializer
implementation "org.miracum:kafka-fhir-serializer:2.0.4"
```

<!-- x-release-please-end -->

### Maven

<!-- x-release-please-start-version -->

```xml
<!-- https://mvnrepository.com/artifact/org.miracum/kafka-fhir-serializer -->
<dependency>
    <groupId>org.miracum</groupId>
    <artifactId>kafka-fhir-serializer</artifactId>
    <version>2.0.4</version>
</dependency>
```

<!-- x-release-please-end -->

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

The project uses <https://github.com/googleapis/release-please> for versioning.

1. work on fixes and features in a seperate fork or branch
1. commit the changes
1. create a pull request against the master branch
1. once reviewed and merged, a new PR should appear to release the changes as a new version
1. merge this PR to create the release on both GitHub and publish to Maven central

See <https://central.sonatype.org/publish/publish-portal-guide/> for a general overview.
