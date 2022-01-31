import networkx as nx
import logging as log
import matplotlib.pyplot as plt
plt.rc('font', size=8)
import numpy
import random
import copy

random.seed(24) ## coloring is random

# random.seed(85) ## coloring is random
## (TODO) make choice of colors

log.basicConfig(level=log.INFO)





## label = id , for read_gml does not properly work without labels and
## some of the data is not labeled
G = nx.read_gml("./interconnect_without_geant.gml", label='id')
log.info(f"The loaded graph comprises {len(G.nodes)} nodes.")

ALPHA = 0.1
def randomColor () :
    return [random.random(), random.random(), random.random(), ALPHA]


as_to_color = {}

node_colors = []
node_pos = {}
repaired = 0
for node in G.nodes(data=True):
    if node[1]['asn'] not in as_to_color:
        as_to_color[node[1]['asn']] = randomColor()
    node_colors.append(as_to_color[node[1]['asn']])

    ## repair missing locations
    if ('Latitude' in node[1]):
        latitude = node[1]['Latitude']
        longitude = node[1]['Longitude']
        asn = node[1]['asn']
    elif  asn != node[1]['asn']:
        log.warning(f"Node {node[0]} might not be well positioned.")
        ## otherwise it copies the previous one lat long
    else:
        repaired = repaired + 1
    
    node_pos[node[0]] = (longitude, latitude)

## targeted ASes
as_to_color['1930'][0] = 1 # portugal
as_to_color['1930'][1] = 0.7
as_to_color['1930'][2] = 0
as_to_color['1930'][3] = 1
as_to_color['766'][0] = 0 # spain
as_to_color['766'][1] = 1
as_to_color['766'][2] = 0.7
as_to_color['766'][3] = 1
as_to_color['2200'][0] = 0.3 # france
as_to_color['2200'][1] = 0.6 
as_to_color['2200'][2] = 1
as_to_color['2200'][3] = 1 
as_to_color['15474'][0] = 0.7 # iceland
as_to_color['15474'][1] = 0.4
as_to_color['15474'][2] = 1
as_to_color['15474'][3] = 1


log.warning(f"Fixed {repaired} missing locations.")
    
intraASEdges = []
interASEdges = []


ASES = nx.Graph()

GATEWAYS = nx.Graph()
GATEWAYS_position = {}
GATEWAYS_color = []
g = 0
intraASEdges_colors = []
for edge in G.edges(data = True):
    nA = next(n for n in G.nodes(data=True) if n[0] == edge[0])
    nB = next(n for n in G.nodes(data=True) if n[0] == edge[1])

    # print (f"{nA} vs {nB}")
    # print (f"{G.nodes(data=True)[edge[0]]['asn']} vs {G.nodes(data=True)[edge[1]]['asn']}")
    if nA[1]['asn'] == nB[1]['asn']:
        intraASEdges.append(edge)
        intraASEdges_colors.append(copy.deepcopy(as_to_color[nA[1]['asn']]))
        intraASEdges_colors[-1][3] = 0.3
    else:
        ASES.add_edge(nA[1]['asn'], nB[1]['asn'])
        interASEdges.append(edge)
        
        GATEWAYS.add_node(g)
        GATEWAYS_position[g] = (nA[1]['Longitude'], nA[1]['Latitude'])
        GATEWAYS.add_node(g+1)
        GATEWAYS_position[g + 1] = (nB[1]['Longitude'], nB[1]['Latitude'])
        GATEWAYS.add_node(g + 1)
        g = g + 2

nx.draw_spring(ASES, with_labels=True)
plt.savefig(f"ases.pdf", format=f"pdf", bbox_inches="tight", dpi=200)
plt.clf()

fig, ax = plt.subplots()

log.info(f"There are {len(intraASEdges)} intra-AS link, and {len(interASEdges)} inter-AS links.")

nx.draw_networkx_edges(G, edgelist=intraASEdges, pos=node_pos, edge_color=intraASEdges_colors)

edge_color = [[0.55, 0.55, 0.55, 1] for x in interASEdges]
nx.draw_networkx_edges(G, edgelist=interASEdges, pos=node_pos, style='dashed', edge_color=edge_color)

gateway_colors = [[0.1, 0.1, 0.1, 1] for x in interASEdges]
gateway_colors.extend(gateway_colors)
nx.draw_networkx_nodes(GATEWAYS, pos=GATEWAYS_position, node_size=20, node_color=gateway_colors)
nx.draw_networkx_nodes(G, pos=node_pos, node_size=10, node_color=node_colors)

G2 = nx.read_gml("./workloads/europe_expe_add_disambiguated.gml")
log.info(f"The loaded graph comprises {len(G2.nodes)} nodes.")

node_pos = {}
node_col = []
COLOR = [1, 0.1, 0.1, 0.3]
for node in G2.nodes(data=True):
    latitude = node[1]['Latitude']
    longitude = node[1]['Longitude']
    node_pos[node[0]] = (longitude, latitude)
    node_col.append(COLOR)
    # if (node[1]['Country'] == 'gbr'):
    #     node_col.append([1, 0, 0])
    # else:
    #     node_col.append([0, 0, 1])
    
nx.draw_networkx_nodes(G2, pos=node_pos, node_size=1, node_color=node_col, edgecolors=COLOR)



def inv(latlong):
    return (latlong[1], latlong[0])

def annot(coord, name, time, dev):
    arrowprops=dict(facecolor='black', shrink=0.05, headwidth=6, headlength=4, width=0.3)
    ax.annotate(f"{name} at {time}s", xycoords='data',
                xy=inv(coord), xytext=numpy.add(inv(coord), dev),
                arrowprops=arrowprops,
                ha='center', va='center')

lisbon = (38.722252, -9.139337)
annot(lisbon, "Portugal: Add", 1, (-15, 2))
palma = (39.5696005, 2.6501603)
annot(palma, "Spain: Add", 2, (-23, 5))
paris = (48.856614, 2.3522219)
annot(paris, "France: Add", 3, (-19, 0))
iceland = (66.15134, -18.90512)
annot(iceland, "Iceland: Add", 4, (18, 0))

annot(palma, "Spain: Del", 5, (9.5, -9))

plt.text(35,60,
         "All: 1157 nodes\n\nPortugal: 23 nodes\nSpain: 20 nodes\nFrance: 39 nodes\nIceland: 14 nodes",
         ma='right')



filename = "europe-ases"
extension = "png"
log.info(f"Saving graph as {filename}.{extension}…")

# plt.axis("scaled")
plt.xlim([-29, 57])
plt.box(False)
plt.savefig(f"{filename}.{extension}", format=f"{extension}", bbox_inches="tight", dpi=200)
# plt.show()
