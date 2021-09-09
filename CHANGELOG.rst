==========
Changelog
==========

This project adheres to `Semantic Versioning <https://semver.org/>`_.

1.6.0 (2021-09-09)
------------------

**Added**

* Introduce method to fetch list of file objects inside a stored dataset

**Fixed**

**Dependencies**

**Deprecated**


1.5.0 (2020-10-12)
------------------

**Added**

* add missing method for sample sheet creation
* add missing method for checking if a user is an admin
* add missing method for fetching of property assignments for experiments, samples and datasets
* make fetching of children and parent samples more stable
* checks for project and experiment existence now don't use search service in the hope of avoiding indexing delays
* throw exceptions if unimplemented methods are called
* improve code readability
* add some tests and mock class for tests

**Fixed**

**Dependencies**

**Deprecated**


1.4.1 (2020-09-24)
------------------

**Added**

* Use new parent pom
* Fix Travis settings
* Add function to fetch properties of arbitrary entity types
* Return null, if experiment cannot be fetched via identifier
* Revise code to fetch parent and children samples
* Test various functions

**Fixed**

**Dependencies**

**Deprecated**

1.4.0 (2020-09-24)
------------------

**Added**

**Fixed**

* Introduce ingest method again, since it was removed and broke applications when using the openbis client > 1.1.4

**Dependencies**

**Deprecated**


1.3.1 (2020-09-23)
------------------

**Added**

* Upgrade parent POM to 3.0.0

**Fixed**

**Dependencies**

**Deprecated**


1.3.0 (2020-05-14)
------------------

**Added**

* First release using openBIS API version 3.
* Provides a logger instance now for the OpenBisClient class

**Fixed**

* Fixes the `getUserSpaces()` method

**Dependencies**

* The constructor uses a new API URL, e.g. http://localhost:8888/openbis/openbis
* Querying spaces or experiments on behalf of a different user requires an instance admin user. If other credentials are used (e.g. instance observer), this is logged and no spaces or experiments are returned.

**Deprecated**
