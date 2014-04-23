import java.net.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.text.*;
import java.nio.*;
import java.nio.channels.*;

public class Read_message extends bfclient implements Runnable
{

    public class Con 
    {
    	ByteBuffer bb;
        public Con() 
        {
        	bb = ByteBuffer.allocate(MAX_MESSAGE_SIZE);     
        }
    }

	public void read(SelectionKey key) throws IOException, ClassNotFoundException
	{
		DatagramChannel chan = (DatagramChannel)key.channel();
        Con con = (Con)key.attachment();
        chan.receive(con.bb);
        InputStream baos = new ByteArrayInputStream(con.bb.array(),0,con.bb.limit());
	    ObjectInputStream oos = new ObjectInputStream(baos);
		Message m_received = (Message)oos.readObject();
		
        //System.out.println("Message type: " + m_received.message_name);
        switch(m_received.message_name)
        {
        	case "routeupdate":
        	{
        		processing.routeupdate(m_received);
        		break;
        	}
        	case "linkdown":
        	{
        		processing.linkdown(m_received);
        		break;
        	}
        	case "linkup":
        	{
        		processing.linkup(m_received);
        		break;
        	}
        }
       	con.bb.clear();
	}

	public void run()
	{
		try
		{
			Selector selector = Selector.open();
		    DatagramChannel channel = DatagramChannel.open();
		    InetSocketAddress isa = new InetSocketAddress(rup.own_port);
		    channel.socket().bind(isa);
		    channel.configureBlocking(false);
		    SelectionKey neighbourKey = channel.register(selector, SelectionKey.OP_READ);
		    neighbourKey.attach(new Con());
		    while (true) 
		    {
		        try 
		        {
		            selector.select();
		            Iterator selectedKeys = selector.selectedKeys().iterator();
		            while (selectedKeys.hasNext()) 
		            {
		                try 
		                {
		                	SelectionKey key = (SelectionKey) selectedKeys.next();
							selectedKeys.remove();
							if (!key.isValid()) 
							{
		                        continue;
		                    }

		                    if (key.isReadable()) 
		                    {
		                        read(key);
		                       	//key.interestOps(SelectionKey.OP_READ);
		                    } 
		                }
		                catch (IOException e) 
		                {
		                    System.err.println("ERROR: " + (e.getMessage()!=null?e.getMessage():""));
		                }
		                catch(ClassNotFoundException e)
		                {
		                	System.out.println("ERROR: " + (e.getMessage()!=null?e.getMessage():""));
		                }
		            }
		        } 
		        catch (IOException e) 
		        {
		            System.err.println("ERROR: " +(e.getMessage()!=null?e.getMessage():""));
		        }
	  		}
	    } 
	    catch (IOException e) 
	    {
	            System.err.println("network error: " + (e.getMessage()!=null?e.getMessage():""));
	    }
	}
}
/*
	- listen on socket
	- use select
	- selectchannels
	- poll the select
	- record activities
	- process activities
	- type of activities - routeupdate, linkdown, linkup

	# linkdown
		- update routing table to make the sender node as infinity and up_status = 0;
		- mark rup.changed = 1
	# linkup
		- update routing table to original valur and send up_status = 1;
		- mark rup.changed = 1
	# route update
		if not already in neighbours then
			update routing table entry ( based on min cost )
			rup.route_table.get(k).cost
			rup.route_table.get(k).link
			rup.changed_status = 1;
		else
			create new node
				add to array key
				put the node in neighbours
				new cost_and_link_to_node object
				put in route_table_rt
				overwrite route_update
				rup.changed_status = 1;

	# handle 3*timeout
		- create hashmap <key,(last_received_timestamp,3*timeout)>
			- attach a timer to each neighbour
			- if current_time - last_received_timestamp > 3*timeout then mark link as down and cost infinite
*/			
