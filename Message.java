import java.io.*;

public class Message implements Serializable
{
	String message_name;
	Route_update rup;

	Message(String message_name,Route_update rup)
	{
		this.message_name = message_name;
		this.rup = rup;
	}
}