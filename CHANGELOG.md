# Release Version Changelog

## 1.6.0-SNAPSHOT

* Introduce method to fetch list of file objects inside a stored dataset

## 1.4.0
* Introduce `ingest` method again, since it was removed and broke
  applications when using the openbis client > 1.1.4

## 1.3.1
* Upgrade parent POM to 3.0.0

## 1.3.0
* Provides a logger instance now for the OpenBisClient class
* Fixes the `getUserSpaces()` method
* Utilises the openBIS V3 API for space search
