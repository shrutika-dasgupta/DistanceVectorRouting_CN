import java.net.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.text.*;
import java.nio.*;
import java.nio.channels.*;

public class Send_update extends TimerTask
{
	ByteArrayOutputStream byos;
	ObjectOutputStream oos;
	byte buf[]= new byte[bfclient.MAX_MESSAGE_SIZE];
	SocketAddress sa;	

	//DatagramPacket packet;

	public void send_route_update()
	{
		try
		{
			Message m = new Message("routeupdate",bfclient.rup);
			InetAddress addr;
			int port;
			byos = new ByteArrayOutputStream();
		    oos = new ObjectOutputStream(byos);
			oos.writeObject(m);
			oos.flush();
			buf = byos.toByteArray();																// writes object to byte array
			

			for (String key : bfclient.neighbours.keySet()) 
			{
				ByteBuffer bb = ByteBuffer.wrap(buf);
				if(bfclient.neighbours.get(key).up_status == 1)										// send only to those neighbours whose link is cuurently up.
				{
					try 
			        {
			            bfclient.selector.select();
			            Iterator selectedKeys = bfclient.selector.selectedKeys().iterator();
			            while (selectedKeys.hasNext()) 
			            {
			                try 
			                {
			                	SelectionKey key_select = (SelectionKey) selectedKeys.next();
								selectedKeys.remove();
								if (!key_select.isValid()) 
								{
			                        continue;
			                    }

			                    if (key_select.isWritable()) 
			                    {
			                    	DatagramChannel chan = (DatagramChannel)key_select.channel();
			                        addr = bfclient.neighbours.get(key).addr;
									port = bfclient.neighbours.get(key).port;
									sa = new InetSocketAddress(addr,port);
									int ret = chan.send(bb,sa);
									bb.clear();
									// testing statement
									// System.out.println("packet sent to neighbour: " + key + " success:" + ret);
			                    } 
			                }
			                catch (IOException e) 
			                {
			                    System.err.println("ERROR: " + (e.getMessage()!=null?e.getMessage():""));
			                }
			            }
			        } 
			        catch (IOException e) 
			        {
			            System.err.println("ERROR: " +(e.getMessage()!=null?e.getMessage():""));
			        }
				}
			}
			
		} 
		catch(NotSerializableException e){}

		catch(SocketException e)
		{
			System.out.println("\nSocket disconnected");
		}
		catch(IOException e)
		{
			System.out.println(e); 
		}
		catch(Exception e)
		{
		 	System.out.println(e);
		}
	}

	public void run()
	{
		Send_update su = new Send_update();
		su.send_route_update();
	}
}