DOCKER_NETWORK = environment_default #docker-spark_default

submitDistributedJoinJob:
	gradle :distributed-join:build
	@if [ -f "./distributed-join/build/libs/SparkJob-0.1.0.jar" ]; then cp "./distributed-join/build/libs/SparkJob-0.1.0.jar" "./submit/"; fi
	docker build -t spark-job ./submit
	docker run --rm --network ${DOCKER_NETWORK} --name spark-job spark-job
	docker rmi spark-job

