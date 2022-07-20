sources = source-1 source-2 source-3
streams = stream-1 stream-2

startSources:
	gradle :source:build
	for s in $(streams) ; \
	do \
		docker exec -it $(shell echo $(sources) | cut -d " " -f1) /kafka/bin/kafka-topics.sh --if-not-exists --create --topic $$s --bootstrap-server localhost:9092 ; \
	done
	for s in $(sources) ; \
	do \
		docker cp ./source/build/libs/Source-0.1.0.jar $$s:/ ; \
	done
	for s in $(sources) ; \
	do \
		docker exec -d $$s java -jar Source-0.1.0.jar --eventSleep 10 --nrOfEvents 1000 ; \
	done

submitJob:
	gradle :distributed-join:build
	@if [ -f "./distributed-join/build/libs/SparkJob-0.1.0.jar" ]; then cp "./distributed-join/build/libs/SparkJob-0.1.0.jar" "./submit/"; fi
	docker build --tag spark-job ./submit
	docker run --rm --network environment_default --name spark-job spark-job
	docker rmi spark-job

init:
	gradle build

clean:
	gradle clean

