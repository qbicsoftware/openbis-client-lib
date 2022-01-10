# Release Version Changelog

## 1.5.3
* Bump `org.apache.logging:log4j:log4j-api` `2.17.0` -> `2.17.1`
* Bump `org.apache.logging:log4j:log4j-core` `2.17.0` -> `2.17.1`
* Fixes CVE-2021-44832

## 1.5.2
* Bump `org.apache.logging:log4j:log4j-api` `2.15.0` -> `2.17.0`
* Bump `org.apache.logging:log4j:log4j-core` `2.15.0` -> `2.17.0`
* Fixes CVE-2021-45105

## 1.5.1
* Bump `org.apache.logging:log4j:log4j-api` `2.13.2` -> `2.15.0`
* Bump `org.apache.logging:log4j:log4j-core` `2.13.2` -> `2.15.0`
* Fixes CVE-2021-4422

## 1.4.0
* Introduce `ingest` method again, since it was removed and broke
  applications when using the openbis client > 1.1.4

## 1.3.1
* Upgrade parent POM to 3.0.0

## 1.3.0
* Provides a logger instance now for the OpenBisClient class
* Fixes the `getUserSpaces()` method
* Utilises the openBIS V3 API for space search
