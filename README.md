DistanceVectorRouting_CN
========================

A project for the Computer Networking Class in Java.

This project consists of the following files:

**9 java files: bfclient.java Cost_n_link_to_node.java Message.java Neighbours.java Processing.java Read_message.java Route_update.java Send_update.java User_input.java

bfclient.java:

Main program
Starts the 3 threads - LinkCommand that handles the User- input Information of linkup, linkdown and showrt; Send - that keeps sending the route update message periodically; and Recieve - that keeps listening to the selector channel for updates.
Cost_n_link_to_node.java:

keeps track of the Cost of the link connected with a neighbour and also the the Node information from where the cost is computed from.

Processing.java: Handles the linkup and linkdown functionality also resets timer when changes are made to the program.

Neighbour.java:

Keeps track of all the neighbour information of the node with its neighbors and cost and link.

Read_message.java:

Recieves the packets that are send uing the selector channel and segregates them into link up and linkdown.

Route_update:

the route-update message that is sent perioically or if changes are made to the program.

Send_update.java:

The thread that periodically keeps sendin gthe route update message to all the connected nodes.
