import java.net.*;
import java.util.*;
import java.lang.*;
import java.io.*;
import java.text.*;
import java.nio.*;
import java.nio.channels.*;

public class User_input extends bfclient implements Runnable
{
	int iperror = 0;
	int porterror = 0;
	int formaterror = 0;
	int ret = -1;
	ByteArrayOutputStream byos;
	ObjectOutputStream oos;
	byte buf[]= new byte[MAX_MESSAGE_SIZE];
	DatagramPacket packet;
	SocketAddress sa;

	int validate(String s[])
	{
		ret = -1;
		//System.out.println(s[1]+" "+s[2]);
		if( (s.length == 3) )
		{
			if( !(s[1].matches("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$")) )   				//allows v.x.y.z or localhost
			{
				iperror = 1;
			} 
			else if( !(s[2].matches("^[0-9]+$")) )											// allows only numbers
			{
				porterror = 1;
			}
	  		else if( !(Integer.parseInt(s[2]) >=1024 && Integer.parseInt(s[2]) <=65535) )	// port should be only between 1024 to 65535
			{
				porterror = 1;
			}
			else
			{
				ret = 0;
			}

			if( ret != 0)
			{
				if(iperror == 1)
				{
					System.out.println("ERROR: ipaddress should be in format v.x.y.z where v,x,y,z can only be numbers < 256\n");			
				}
				else if(porterror == 1)
				{
					System.out.println("ERROR:  The port should only be between 1024 to 65535 and can contain only numbers");
				}
			}
		}
		else
		{
			ret = -1;
			System.out.println("ERROR: either IP or port is not entered ; or some extra values are entered");
		}
		
		return ret;
	}

	// PJ: still need to handle via links i.e if A -> B -> C and link A -> B breaks, then what to do about link C.
	void linkdown(String s[])														
	{
		try
		{
			String socket  = "/" + s[1] + ":" + s[2];
			int isneighbour = 0;
			String k= "";
			for(String key : neighbours.keySet())
			{
				if(key.equals(socket) && neighbours.get(key).up_status==1)
				{
					isneighbour = 1;
					neighbours.get(key).up_status = 0;
					k = key;
					break;
				}
			}

			if(isneighbour == 1)
			{
				//go through the rt table for all key's , check for which all nodes is the link as k ..make those also null.. do this itertatively until no more changes
				processing.linkdown_calculation(k);

				Message m = new Message("linkdown",rup);
				InetAddress addr;
				int port;
				byos = new ByteArrayOutputStream();
			    oos = new ObjectOutputStream(byos);
				oos.writeObject(m);
				oos.flush();

																			// writes object to byte array
				buf = byos.toByteArray();																// writes object to byte array
				
				ByteBuffer bb = ByteBuffer.wrap(buf);
				try
				{
					selector.select();
					Iterator selectedKeys = bfclient.selector.selectedKeys().iterator();
					while(selectedKeys.hasNext())
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
		                        addr = bfclient.neighbours.get(k).addr;
								port = bfclient.neighbours.get(k).port;
								sa = new InetSocketAddress(addr,port);
								int ret = chan.send(bb,sa);
								bb.clear();
								
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

				

				// for testing
				System.out.println("reseted timer due to user cmd linkdown");
				send_update.send_route_update();
				t.cancel();
				t = new Timer();
				t.schedule(new Send_update(),(long)timeout*1000,(long)timeout*1000);
				bb.clear();
			}
			else
			{
				System.out.println("ERROR: either linkis already down or the given node is not a neighbour");
			}
		}
		catch(NotSerializableException e){}
		catch(Exception e)
		{
		 	System.out.println(e);
		}
	}
 	
 	void linkup(String s[])
 	{
 		try
		{
			String socket  = "/" + s[1] + ":" + s[2];
			int isneighbour = 0;
			String k="";
			for(String key : neighbours.keySet())
			{

				if(key.equals(socket) && neighbours.get(key).up_status==0)
				{
					isneighbour = 1;
					neighbours.get(key).up_status = 1;
					k = key;
					break;
				}
			}
			if(isneighbour == 1)
			{
				rup.route_table.get(k).cost = neighbours.get(k).weight;
				rup.route_table.get(k).link = k;
			
				Message m = new Message("linkup",rup);
				InetAddress addr;
				int port;
				byos = new ByteArrayOutputStream();
			    oos = new ObjectOutputStream(byos);
				oos.writeObject(m);
				oos.flush();
				buf = byos.toByteArray();																// writes object to byte array
				ByteBuffer bb = ByteBuffer.wrap(buf);
				try
				{
					selector.select();
					Iterator selectedKeys = bfclient.selector.selectedKeys().iterator();
					while(selectedKeys.hasNext())
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
		                        addr = bfclient.neighbours.get(k).addr;
								port = bfclient.neighbours.get(k).port;
								sa = new InetSocketAddress(addr,port);
								int ret = chan.send(bb,sa);
								bb.clear();
								
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

				System.out.println("reseted timer due to user command linkup");
				send_update.send_route_update();
				t.cancel();
				t = new Timer();
				t.schedule(new Send_update(),(long)timeout*1000,(long)timeout*1000);
				bb.clear();
			}
			else
			{
				System.out.println("ERROR: either linkis already up or the given node is not a neighbour");
			}
		}
		catch(NotSerializableException e){}
		catch(Exception e)
		{
		 	System.out.println(e);
		}
 	}

 	// display routing table for this node
 	void showrt()
 	{
 		Date date = new Date();	
		SimpleDateFormat fmt =  new SimpleDateFormat ("yyyy/MM/dd - HH:mm:ss");
 		System.out.println(fmt.format(date) + "	Distance vector list is:");

  		for( int i = 0 ; i < rt.size() ; i++ )
		{
			System.out.println("Destination = " + key[i] + ", cost = " + rup.route_table.get(key[i]).cost +" , Link = " + rup.route_table.get(key[i]).link);	
		}
		System.out.println("After some time, new nodes might be added as they are discovered.");
 	}

 	void close_cmd()
 	{
 		System.out.println("Node is shutdown");
 		System.exit(0);
 	}

	public void run()
	{
		try
		{
			String cont = "Y";
			String command;
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			User_input ui = new User_input();

			while(cont.equals("Y") == true)
			{
				iperror = 0;
				porterror = 0;
				formaterror = 0;

				System.out.println("\nType the action required:\n 1. LINKDOWN {ip_address port} \n 2. LINKUP {ip_address port} \n 3. SHOWRT \n 4. CLOSE");
				command = br.readLine();
				String s[] =command.split(" ");

				switch(s[0])
				{
					case "LINKDOWN":
					{
						ret = ui.validate(s);
						if(ret != -1)
						{
							ui.linkdown(s);	
							break;
						}
						else
						{
							break;
						}
					}
					case "LINKUP":
					{
						ret = ui.validate(s);
						if(ret != -1)
						{
							ui.linkup(s);	
							break;
						}
						else
						{
							break;
						}
						
					}
					case "SHOWRT":
					{
						ui.showrt();
						break;
					}
					case "CLOSE":
					{
						ui.close_cmd();
						break;
					}
					default:
					{
						System.out.println("ERROR: Format error: please enter commands in the exact format");
						formaterror = 1;
						break;
					}

				}
				if(formaterror == 1)
				{
					continue;
				}
				else
				{
					System.out.println("\nDo you want to run more actions: default is N. choose (Y/N)");
					cont = br.readLine();
				}
			}

			System.out.println("no more actions will be performed, just periodic route_update are being sent");
			System.out.println("if you wish to shut the node, press Y");
			if(br.readLine().equals("Y"))
			{
				System.exit(0);
			}
			else
			{
				System.out.println("Node is active.. If you want to terminate later, just press ctrl-c");
			}
		}
		catch(Exception e)
		{
		 	System.out.println(e);
		}
	}
}