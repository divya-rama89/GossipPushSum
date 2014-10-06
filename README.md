GossipPushSum
============
Team members: Anirudh Subramanian (UFID:94453124), Divya Ramachandran (UFID:46761308)

A program written as part of Distributed Operating Systems course to simulate gossip algorithm for group communication and aggregate computation.
Inputs :: numNodes topology algorithm

numNodes: the number of nodes forming the network

topology: the topology used will be 1 out of the following -
1) line: nodes are arranged as a line; neighbours are adjacent nodes
2) full: each node can select a neighbour from the entire network i.e. all the nodes are considered neighbours
3) 2D: nodes are arranged as a 2D mesh; neighbours are the adjacent nodes  along two dimensions (top, bottom, left, right)
4) imp2D: nodes are arranged as a 2D mesh; neighbours are the adjacent nodes  along two dimensions (top, bottom, left, right) as well as one other random node

algorithm: the algorithm to be used would be 'gossip' or 'push-sum'

Example: 1000 2D gossip

Output ::
Time taken for the nodes to converge

-------------------------------------------------------------------------------------------------------------------------------

Instructions {How to run}
---------------------------------------------------------------------
//Add stuff required to be filled in config

-----------------------------------------------------------------

Now from the GossipPushSum-master folder do:

sbt publishLocal 

commands(Make sure you are in GossipPushSum-master folder)
-------------------------------------------------------------------

sbt runMain project1 numNodes topology algorithm


Note
----------------------------------------------------------------------------

Please refer to excel sheet in the project root folder for our test results and the number of rounds required to converge for various inputs

Details:
----------------------------------------------------------------------------
