package userInterface;

import java.io.IOException;

import mySHA1.SimpleSHA1;
import myUtil.*;
import myInfo.*;

import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.io.*;

public class Query {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Socket client_req = null;
		//
		if(args == null)
		{
			System.out.println("Wrong input!");
			ShowInstruct();
			return;
		}
		else if(args.length == 2)
		{
			System.out.println("Please enter your search key (or type 'quit' to leave):");
			//
			String usr_input = "Default";
			try {
				usr_input = ReadUserInputLine.readLine();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			//
			String req_ip = args[0];
			Integer req_port = Integer.parseUnsignedInt(args[1]);
			//
			while(!usr_input.equals("quit"))
			{
				//
				try {
					client_req = new Socket(req_ip, req_port);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
					System.out.println("Cannot access the remote chord ring node. Please check your ip/port address.");
					return;
				}

				//
				try {
					//
					ObjectOutputStream os_client = new ObjectOutputStream(client_req.getOutputStream());
					String hashed_kywd = SimpleSHA1.getSHA1(usr_input);
					//
					System.out.println("Hash value is " + hashed_kywd);
					//
					//System.out.println("[DEBUG]: " + client_req.getLocalAddress().getHostAddress() + ":" + client_req.getLocalPort());
					//
					System.out.println("Searching...");
					//
					InternalCommand cmd_client = new InternalCommand("search", client_req.getLocalAddress().getHostAddress(), client_req.getLocalPort(), hashed_kywd);
					os_client.writeObject(cmd_client);
					os_client.flush();
					//
					//
					ObjectInputStream is_client = new ObjectInputStream(client_req.getInputStream());
					String real_ip = (String)is_client.readObject();
					Integer real_port = (Integer)is_client.readObject();
					//
					os_client.close();
					is_client.close();
					//
					Socket real = new Socket(real_ip, real_port);
					ObjectOutputStream os_real = new ObjectOutputStream(real.getOutputStream());
					InternalCommand inst_real = new InternalCommand("dir_search", null, null, hashed_kywd);
					os_real.writeObject(inst_real);
					ObjectInputStream is_real = new ObjectInputStream(real.getInputStream());
					String resp_head = (String)is_real.readObject();
					String resp_res = (String)is_real.readObject();
					//
					System.out.println(resp_head + "\n" + resp_res + "\n");
					//
					System.out.println("Please enter your search key (or type 'quit' to leave):");
					usr_input = ReadUserInputLine.readLine();
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//
			System.out.println("Your chord ring hash table look-up session ends.");
		}
		else
		{
			System.out.println("Wrong input!");
			ShowInstruct();
			return;
		}
	}

	private static void ShowInstruct()
	{
		System.out.println("Parameters should be input like this:");
		System.out.println("Query IP_address Port_num");
	}

}
