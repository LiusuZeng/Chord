package myInfo;

import java.security.NoSuchAlgorithmException;
import java.io.*;
import mySHA1.SimpleSHA1;

public class Node implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6493438004743756180L;
	public String IP;
	public Integer Port;
	public String HashValue;
	//
	public Node prev;
	// Note that the hash value is simplified into 4-byte (32-bit)
	// therefore, m = 32, finger table should have 32 entries
	public Node[] FingerTable;
	
	public Node succ2; // next successor
	
	// dup
	public Node(Node src)
	{
		this.IP = new String(src.IP);
		this.Port = new Integer(src.Port);
		this.HashValue = new String(src.HashValue);
		this.prev = src.prev;
		//
		this.FingerTable = new Node[src.FingerTable.length];
		for(int i = 0; i < this.FingerTable.length; i++)
		{
			this.FingerTable[i] = src.FingerTable[i];
		}
	}
	
	public Node(String IP_src, Integer Port_src) throws NoSuchAlgorithmException
	{
		this.IP = IP_src;
		this.Port = Port_src;
		StringBuilder tool = new StringBuilder();
		tool.append(this.IP + ":");
		tool.append(Port_src);
		this.HashValue = SimpleSHA1.getSHA1(tool.toString());
		this.FingerTable = new Node[4*this.HashValue.length()]; // in our case: 32
		this.prev = null; // value should be given externally
	}
	
	public long getStartAddr(int i)
	{
		long base = SimpleSHA1.SHA1ToLong(this.HashValue);
		long plus = (long)java.lang.Math.pow(2, i);
		long boundary = (long)java.lang.Math.pow(2, 32);
		return (base+plus)%boundary;
	}
}
