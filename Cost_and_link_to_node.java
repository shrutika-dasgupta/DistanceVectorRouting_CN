import java.io.*;
public class Cost_and_link_to_node implements Serializable
{ 
	double cost;
	String link;

	Cost_and_link_to_node(double cost, String link)
	{
		this.cost = cost;
		this.link = link;
	}
}