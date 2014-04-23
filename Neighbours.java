import java.net.*;
import java.util.*;

public class Neighbours 																// one object is created for each neighbour
{

	InetAddress addr;
	int port;
	double weight;
	int timeout;
	int up_status;																		// keeps track of whether the link is up or down
	Map<String,Cost_and_link_to_node> neighbour_dv;

	Neighbours(InetAddress addr, int port, double weight, int timeout, int up_status, Map<String,Cost_and_link_to_node> neighbour_dv)
	{
		this.addr = addr;
		this.port = port;
		this.weight = weight;
		this.timeout = timeout;
		this.up_status = up_status;
		this.neighbour_dv = neighbour_dv;
	}
}