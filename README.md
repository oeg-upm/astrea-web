# Astrea-web
This repository is a web service that publishes the services of [Astrea]([https://github.com/oeg-upm/Astrea](https://github.com/oeg-upm/Astrea)), providing a user-friendly interface to generate SHACL shapes from one or more ontologies, and also a REST API to be used by third-party services.

## Deploying Astrea-web
In order to deploy the Astrea-web service users have two options:
* Using docker, just [run using the docker-compose](https://docs.docker.com/compose/gettingstarted/) the following receipe.
``````
version: '2'
services:
  astrea-service:
    image: acimmino/astrea-service:1.0.1
    ports:
    - 8080:8080
``````
* Alternatively download this repository and compile the project, then run the compiled jar. Find bellow the commands to follow.
``````
sudo git clone https://github.com/oeg-upm/astrea-web.git
cd astrea-web
mvn clean package -DskipTests
java -jar ./target/astrea-web.jar --server.port=8080
``````

## Using the Rest API
In order to use our REST API, check our [online documentation](https://astrea.linkeddata.es/swagger-ui.html)
