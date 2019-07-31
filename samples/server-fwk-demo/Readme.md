# Details on the Sample NCY stack application
## How to build
### Tools Needed
 - OpenJDK - openjdk version "1.8.0_111"
 - maven - Apache Maven 3.6.0
### Steps to build
Assuming that you have checkedout code intoa  directory named **obbaa-netconf-stack**, run the following command in the obbaa-netconf-stack directory
`mvn clean install -DskipTests`
#### Building the docker image
Once the code is built, you need to build the docker image to run the ncy stack sample. You can build the docker image by executing the following commands in the directory **obbaa-netconf-stack/samples/server-fwk-demo/package/sample-dist**
`mvn docker:build`
## Running the NCY stack server fwk Sample
You will need the following tools to run the sample
 - Docker engine - minimum version needed 17.09.0-ce
 - Docker compose  - minimum version needed 1.19.0
 ### Running the sample NCY docker container
 To run the sample container execte the following command in the directory **obbaa-netconf-stack/samples/server-fwk-demo/package/sample-dist**
`docker-compose up -d`
### Logging into karaf console
Execute the following command to log into the karaf console of the sample container
`docker exec -it sample-ncy-stack bin/client`
### Viewing Logs
To tail the logs of the application execute the following command
`docker exec -it sample-ncy-stack tl -f data/log/karaf.log`
### Sample RPCs
There are some sample RPCs available in the code in the following location
`obbaa-netconf-stack/samples/server-fwk-demo/package/sample-rpcs`
The netconf port exposed is 9294, the username to use is admin and the password is password



