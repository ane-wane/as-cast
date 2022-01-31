
build:
	mvn clean
	mvn package

europe: build
	printf "An experiment to highlight how xas-cast improves as-cast in network of networks.\n"
	mvn exec:java -Dexec.args="examples/as_cast_europe.txt" > temp.dat
	cat temp.dat | grep Traffic > ./results/as_cast_europe_traffic.dat
	cat temp.dat | grep OLocalSpace > ./results/as_cast_europe_local.dat
	mvn exec:java -Dexec.args="examples/xas_cast_europe.txt" > temp.dat
	cat temp.dat | grep Traffic > ./results/xas_cast_europe_traffic.dat
	cat temp.dat | grep OLocalSpace > ./results/xas_cast_europe_local.dat

geant: build
	printf "An experiment to highlight that as-cast even works in networks with physical partitions.\n"
	mvn exec:java -Dexec.args="examples/as_cast_geant.txt" > temp.dat
	cat temp.dat | grep Traffic > ./results/as_cast_geant_traffic.dat
	mvn exec:java -Dexec.args="examples/as_cast_geant_2.txt" > temp.dat
	cat temp.dat | grep Traffic > ./results/as_cast_geant_2_traffic.dat

complexity: build
	printf "An experiment to show the trade-off and complexity of AS-cast in (chained + 2 links at random) networks.\n"
	mvn exec:java -Dexec.args="examples/as_cast_complexity.txt" > temp.dat	
	cat temp.dat | grep Traffic > ./results/as_cast_messages.dat
	python3 ./results/process_traffic.py ./results/as_cast_messages.dat > ./results/as_cast_messages_per_second.dat
	cat temp.dat | grep Complexity > ./results/as_cast_duration.dat
	python3 ./results/process_durations.py ./results/as_cast_duration.dat > ./results/as_cast_duration_2.dat

