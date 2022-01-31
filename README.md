# AS-cast simulation

This project aims at creating a decentralized partitioning protocol
that guarantees consistent partitioning and termination even in
dynamic settings where nodes join and leave the system, create or
destroy partitions. This project uses PeerSim [1], a Java
simulator, to evaluate and validate protocols properties in large
scale dynamic systems. Thus, it provides (i) partitioning protocols,
(ii) controllers to generate changes over rounds, and (iii) observers
to evaluate metrics over rounds.

## Running experiments

PeerSim uses text files to define the execution settings. They are
located in Folder ```examples```. To activate assertions (that may be
useful in few cases), use ```export MAVEN_OPTS="-ea"```. Then to
build, you need to run the maven command ```mvn package```. Then to
execute the configuration file ```default_example.txt```, you need to
run the maven command ```mvn exec:java
-Dexec.args="examples/default_example.txt"```.  Alternatively, you can
use the makefile to run some of the experiments.

### Complexity and trade-off

The experiment setup is located in file ```examples/as_cast_complexity.txt``` 
(or ```make complexity```).  The output is in a separated file to exploit it afterwards.

It generates 100 partitions by selecting random nodes in a network
of 10k nodes. Nodes add partitions one after the other: The
system checks if they reach consistent partitioning before allowing
the next node to create a partition. Once the system comprises 100
partitions, nodes remove them in order of their creation, i.e.,
the first partition that has been created is the first to be deleted.

The experiment may take time, for at some point, the oracle in charge
of checking consistent partitioning needs to node the shortest path
of 10k nodes for each 100 source of partition. 

We focus the experiments on traffic and termination time. 

```
$ cd ./results
$ gnuplot as_cast_complexity.plot
```

We expect to see an exponential decrease of traffic over new
partitions, and an exponential increase of traffic over deleted
partition. We expect that ```del``` operations are more expensive than ```add```
operations, for the former needs to purge stale control
information before allowing up-to-date control information to flow
through the network. Finally, we expect that termination time follows
the behavior of generated traffic.


### Traffic containment

The experiment setups are located in files ```examples/as_cast_geant.txt``` and ```examples/as_cast_geant_2.txt``` 
(or ```make geant```).
The only difference between these files simply states that the first
experiment has only 1 partition for the two clusters while the second
experiment has 2 partitions, 1 per cluster:

```
$ diff ./examples/as_cast_geant.txt ./examples/as_cast_geant_2.txt
56c56
< control.topo.adders 1
---
> control.topo.adders 2
```

We generate the clusters using the ```.gml``` file in ```topology/condensed_west_europe-inferred.gml```. 
It contains nodes and edges along with inferred latency based on geolocation of these nodes. 
The cluster corresponds to [GÉANT network](https://www.geant.org/), an infrastructure spanning across Europe.
We create 2 such a cluster, and link them with a high latency link (200ms) representing an
inter-continental communication link such as Europe/America.

Then, after nodes reach consistent partitioning, we remove the
inter-continental link.  Then, after nodes reach consistent
partitioning again, we add the inter-continental link again.

```
$ cd ./results
$ gnuplot as_cast_geant.plot
```

We expect to see that the traffic with two partitions (1 per cluster)
is significantly lower than the traffic with only one partition for
two clusters.

### Cross autonomous systems

The experiment setups are located in files ```examples/as_cast_geant.txt```
and ```examples/as_cast_geant_2.txt``` (or ```make europe```).

<p align="center">
  <img width="400" src="https://github.com/Chat-Wane/peersim-xas-cast/blob/master/topology/europe-ases.png">
</p>

Similarly to previous experiment, we generate the network 
using ```topology/interconnect.gml```. It builds a network of networks
comprising 1157 nodes distributed in 40 autonomous systems: the european
NRENs interconnected with GÉANT.
__Gateway nodes__ link different autonoumous systems together, e.g., 
Lisbon node links Portugal to Spain and United Kingdoms through a Madrid node 
and a London node respectively

This experiment aims at highlighting a shortcoming of AS-cast: every
node ends up indexing as soon as a source exists in the network. To
alleviate this issue, we propose xAS-cast (stands for cross AS-cast)
that enable lazy dynamic partitioning by allowing AS’ gateways to stop
notifications when their respective network does not appear to have a
source.

```
$ cd ./results
$ gnuplot europe_as_vs_xas.plot
```

We expect to see a high traffic using AS-cast while xAS-cast further
locks down the traffic to relevant autonomous systems, hence a reduced
global traffic.

## Miscellaneous

Gnuplot files, Python processing files are located in ```./results```. 
Topology or trace files are located in ```./topology```.

## References

[1] Albertor Montresor, and Mark Jelasity. Peersim: a Scalable P2P
Simulator. <i>Proceedings of the 9th International Conference on
Peer-to-Peer (P2P'09)</i>.
