import java.net.*;
import java.util.*;
import java.io.*;

public class Route_update implements Serializable
{
	InetAddress own_ip;
	int own_port;
	int own_timeout;
	Map<String,Cost_and_link_to_node> route_table;

	Route_update(Map<String,Cost_and_link_to_node> route_table)
	{
		this.route_table = route_table;	
	}
}

