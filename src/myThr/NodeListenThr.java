package myThr;

import java.io.*;
import java.net.*;
import myInfo.Node;

public class NodeListenThr extends Thread {
	private ServerSocket mother;
	private Node myNode;

	public NodeListenThr(ServerSocket mother_src, Node Node_src)
	{
		this.mother = mother_src;
		this.myNode = Node_src;
	}

	public void run()
	{
		try {
			//
			System.out.println("You are listening on port " + this.myNode.Port + ".");
			System.out.println("Your position is " + this.myNode.HashValue + ".");
			while(true)
			{
				// Main UI Thr will close this server socket to signify
				// the retrieve of the listen Thr resources
				Socket son = this.mother.accept();
				NodeProcessThr son_thr = new NodeProcessThr(son, this.myNode);
				son_thr.start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("Listening Thread resource on " + 
			this.mother.getLocalSocketAddress().toString() + " is recycled!");
			return;
		}

	}
}
