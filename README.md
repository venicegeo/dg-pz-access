To run the Access service locally, perhaps through Eclipse or through CLI, navigate to the project directory and run:

`mvn clean install -U spring-boot:run`

The `application.properties` file controls URL information for the components it connects to - mainly GeoServer. If you are intending to debug a local instance of GeoServer, then simply changing the appropriate vcap.services.pz-geoserver.* configuration values.

NOTE: This Maven build depends on having access to the `Piazza-Group` repository as defined in the `pom.xml` file. If your Maven configuration does not specify credentials to this Repository, this Maven build will fail. 
