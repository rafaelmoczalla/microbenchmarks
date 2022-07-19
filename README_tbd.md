# Hadoop MapReduce Join Examples
These project contains two map reduce join examples implemented with Hadoop MapReduce. The first example is a broadcast join and the second example is a repartition join.

Author: [Rafael Moczalla](Rafael.Moczalla@hpi.de)

Create Date: 06 July 2022

Last Update: 06 July 2022

Tested on Ubuntu 22.04 LTS.

## Prerequisites
1. Install git, a java JDK and Docker.
    ```bash
    sudo apt install gradle default-jdk-headless docker-ce
    ```

2. Install Docker Compose.
    ```bash
    sudo curl -SL https://github.com/docker/compose/releases/download/v2.6.1/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
    ```

3. Download the project and change directory to the project folder.
    ```bash
    git clone https://github.com/rafaelmoczalla/MapReduceJoinExamples.git
    cd MapReduceJoinExamples
    ```

## Usage
To run the examples you first need to have a running Hadoop MapReduce cluster where you can submit the map reduce job. Then you build the project and afterwards you submit one of both join examples as a job to the map reduce cluster.

### Start Local Hadoop Cluster with Docker
We use the Docker Hadoop cluster setup provided by Big Data Europe. To start the local cluster open a new terminal in the `./docker-hadoop` folder and start the cluster with Docker Compose as follows.
```bash
cd docker-hadoop
docker-compose up
```

### Build
The project is build with Gradle. Initially you need to build the whole project with
```bash
gradle build
```
to build the utils library package.

The source code of the broadcast join example is located in `./broadcast-join/src/main/java/org/moczalla/joins/broadcastjoin` and the source code of the repartition join is located in `./repartition-join/src/main/java/org/moczalla/joins/repartitionjoin` Before submitting a new version of the source code you need to compile the source code once again. To recompile the code of the broadcast join enter
```bash
gradle :broadcast-join:build
```
into the terminal and to recompile the repartition join source code enter
```bash
gradle :repartition-join:build
```
into the terminal.

### Submit Job
After you created the local Hadoop Docker cluster and build the initial project or recompiled code use the Makefile to submit the broadcast join with
```bash
make submitBroadcastJoinJob
```
and to submit the repartition join use
```bash
make submitRepartitionJoinJob
```