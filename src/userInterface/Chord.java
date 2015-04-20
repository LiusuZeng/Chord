package userInterface;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.net.*;
import java.io.*;

import myUtil.*;
import myInfo.*;
import myThr.*;

public class Chord {

	public static void main(String[] args) throws NumberFormatException, NoSuchAlgorithmException, UnknownHostException {
		// TODO Auto-generated method stub
		// 2 types of cmd is acceptable
		// #1: create a new chord ring: Chord 8001
		// #2: join an existing ring at some addr: Chord 8001 128.2.205.42 8010
		// Also, should provide a way to exit the ring normally
		
		Socket tunnel = null;
		ServerSocket assigned_listenSock = null;
		String myPC_IP = null;
		Integer myPC_Port = null;
		Node assigned_node = null;
		NodeKeepAliveThr assigned_trackThr = null;
		//
		if(args == null)
		{
			System.out.println("Wrong input!");
			ShowInstruct();
			return;
		}
		else if(args.length == 1)
		{
			System.out.println("Creating the Chord ring...");
			// create a new chord ring (should have error handling)
			InetAddress addr = InetAddress.getLocalHost();
			myPC_IP = addr.getHostAddress().toString();
			myPC_Port = Integer.parseUnsignedInt(args[0]);
			assigned_node = new Node(myPC_IP, myPC_Port);
			try {
				assigned_listenSock = new ServerSocket(assigned_node.Port);
				NodeListenThr assigned_listenThr = new NodeListenThr(assigned_listenSock, assigned_node);
				assigned_listenThr.start();
				//
				tunnel = new Socket(assigned_node.IP, assigned_node.Port);
				ObjectOutputStream os_create = new ObjectOutputStream(tunnel.getOutputStream());
				InternalCommand cmd_create = new InternalCommand("create", assigned_node.IP, assigned_node.Port);
				os_create.writeObject(cmd_create);
				os_create.flush();
				//
				ObjectInputStream is_create = new ObjectInputStream(tunnel.getInputStream());
				String fdbk_create = (String)is_create.readObject();
				//
				os_create.close();
				is_create.close();
				tunnel.close();
				//
				// start tracking
				assigned_trackThr = new NodeKeepAliveThr(assigned_node);
				assigned_trackThr.start();
				//
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("[DEBUG]: " + e.toString());
				System.out.println("Create failed! Please check your IP/Port address and re-run the program.");
				//tunnel.close();
				return;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(args.length == 3)
		{
			System.out.println("Joining the Chord ring...");
			// join an existing chord ring (should have error handling)
			InetAddress addr = InetAddress.getLocalHost();
			myPC_IP = addr.getHostAddress().toString();
			myPC_Port = Integer.parseUnsignedInt(args[0]);
			assigned_node = new Node(myPC_IP, myPC_Port);
			try {
			    // Check if the target address is reachable
				Socket try_connection_tunnel = new Socket();;
				InetSocketAddress try_addr = new InetSocketAddress(args[1], Integer.parseUnsignedInt(args[2]));
			    try_connection_tunnel.connect(try_addr, 3000);
			    InternalCommand inst_try = new InternalCommand("try_connect", null);
			    ObjectOutputStream os_try = new ObjectOutputStream(try_connection_tunnel.getOutputStream());
			    os_try.writeObject(inst_try);
			    os_try.flush();
			    //
			    try_connection_tunnel.close();
			    //
				assigned_listenSock = new ServerSocket(assigned_node.Port);
				NodeListenThr assigned_listenThr = new NodeListenThr(assigned_listenSock, assigned_node);
				assigned_listenThr.start();
				//step1: call init_finger_table
				Node n_prime_fake = new Node(args[1], Integer.parseUnsignedInt(args[2]));
				tunnel = new Socket(assigned_node.IP, assigned_node.Port);
				ObjectOutputStream os_join = new ObjectOutputStream(tunnel.getOutputStream());
				InternalCommand cmd_join = new InternalCommand("join", n_prime_fake);
				os_join.writeObject(cmd_join);
				os_join.flush();
				//
				ObjectInputStream is_join = new ObjectInputStream(tunnel.getInputStream());
				String fdbk_join = (String)is_join.readObject();
				//
				os_join.close();
				is_join.close();
				tunnel.close();
				// start tracking
				assigned_trackThr = new NodeKeepAliveThr(assigned_node);
				assigned_trackThr.start();
				//
			} catch (IOException e) {
				// TODO Auto-generated catch block
			    System.out.println("[DEBUG]: " + e.toString());
			    System.out.println("Join failed! Pleae check your IP/Port address and re-run the program.");
			    return;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Incompatible parameters!");
			ShowInstruct();
			return;
		}
		// waiting for exiting
		String usr_input = "Default";
		while(!usr_input.equals("quit"))
		{
			System.out.println("Please input 'quit' to leave the ring: ('print' is reserved for debug)");
			try {
				usr_input = ReadUserInputLine.readLine();
				//
				if(usr_input.equals("print"))
				{
					print_tb(assigned_node);
				}
				else continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("Wrong input!");
				usr_input = "Default";
				continue;
			}
		}
		// user decides to leave the ring (stop listening as a server)
		// Update table part...
		try {
			assigned_trackThr.stop();
			assigned_listenSock.close();
			//
			System.out.println("You have exited the chord ring safe and sound.");
			//
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void ShowInstruct()
	{
		System.out.println(
				"Two types of cmd is acceptable:\n"
						+ "#1: Create a new chord ring, eg: Chord 8001\n"
						+ "#2: Join an existing ring at some address, eg: Chord 8001 128.2.205.42 8010\n"
						+ "Please try again!"
				);
	}
	
	public static void print_tb(Node src)
	{
		System.out.println("++++++++++++++++++++++++");
		System.out.println("Info of Node: " + src.Port);
		System.out.println("Prev: " + src.prev.Port);
		System.out.println("Next Successor: " + src.succ2.Port);
		//
		for(int i = 0; i < src.FingerTable.length; i++)
		{
			System.out.println("" + i + "th entry: " + src.FingerTable[i].Port);
		}
		//
		System.out.println("++++++++++++++++++++++++\n\n");
	}

}
