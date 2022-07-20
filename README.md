# Batch/Stream Join Micro Benchmarks
These project several micro benchmarks that measure sustainable throughput, latency; CPU, memory & bandwidth consumption of different batch & stream join approaches.

Author: [Rafael Moczalla](Rafael.Moczalla@hpi.de)

Create Date: 19 July 2022

Last Update: 19 July 2022

Tested on Ubuntu 22.04 LTS.

## Prerequisites
1. Install git, a java JDK, Docker & Gradle.
    ```bash
    sudo apt install gradle default-jdk-headless docker-ce
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
    sdk install gradle 7.5
    ```

2. Install Docker Compose.
    ```bash
    sudo curl -SL https://github.com/docker/compose/releases/download/v2.6.1/docker-compose-linux-x86_64 -o /usr/local/bin/docker-compose
    ```

3. Download the project & change directory to the project folder.
    ```bash
    git clone https://github.com/rafaelmoczalla/TBD.git
    cd TBD
    ```

## Usage
To run the examples you first need to have a running Spark cluster where you can submit the map reduce job. Then you build the project & afterwards you submit one of both join examples as a job to the map reduce cluster.

### Start Local Spark Cluster with Docker
We use the Docker Spark cluster setup provided in the `./environment` subproject. To start the local cluster open a new terminal in the `./environment` folder & start the cluster with Docker Compose as follows.
```bash
cd environment
make startCluster
```

### Build
The project is build with Gradle & split into a source subproject & a actual join subproject. You can build all projects with
```bash
gradle build
```

To build only the sources enter
```bash
gradle :source:build
```
into the terminal & to build only the join job enter
```bash
gradle :distributed-join:build
```
into the terminal.

### Start Micro Benchmarks
Before starting the actual micro benchmark we need to start the sources. We prepared a make target for that task. Run
```bash
make startSources
```

After starting the sources we can submit & start the join with
```bash
make submitJob
```

## ToDo
- [ ] Basic source
- [ ] Add a "measuring" subproject.