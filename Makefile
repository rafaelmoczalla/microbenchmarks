sources = source-1 source-2 source-3
streams = stream-1 stream-2

init:
	gradle clean
	gradle build

buildSources:
	gradle :source:build

initKafkaTopics:
	for s in $(sources) ; \
	do \
		docker exec -it $(shell echo $(sources) | cut -d " " -f1) /kafka/bin/kafka-topics.sh --if-not-exists --create --topic $$s --bootstrap-server localhost:9092 ; \
	done

quickStartSources:
	for s in $(sources) ; \
	do \
		docker cp ./source/build/libs/Source-0.1.0.jar $$s:/ ; \
	done
	for s in $(sources) ; \
	do \
		docker exec --detach --env STREAM_LIST="stream-1 stream-2" $$s java -jar Source-0.1.0.jar --eventSleep 10 --nrOfEvents 1000 ; \
	done

startSources: buildSources initKafkaTopics quickStartSources

rmSources:
	for s in $(sources) ; \
	do \
		docker exec -it $(shell echo $(sources) | cut -d " " -f1) /kafka/bin/kafka-topics.sh --if-exists --delete --topic $$s --bootstrap-server localhost:9092 ; \
	done

buildJob:
	gradle :distributed-join:build

quickSubmitJob:
	@if [ -f "./distributed-join/build/libs/SparkJob-0.1.0.jar" ]; then cp "./distributed-join/build/libs/SparkJob-0.1.0.jar" "./submit/"; fi
	docker build --tag spark-job ./submit
	docker run --rm --network environment_default \
		--env BOOTSTRAP_SERVERS_PORT=9092 \
		--env SOURCE_LIST="source-1 source-2 source-3" \
		--env STREAM_LIST="stream-1 stream-2" \
		--name spark-job spark-job
	docker rmi spark-job

submitJob: buildJob quickSubmitJob

clean:
	gradle clean

initProject: clean
	gradle build

