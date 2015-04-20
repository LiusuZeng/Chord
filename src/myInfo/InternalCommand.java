package myInfo;

import java.io.*;

public class InternalCommand implements Serializable {
	/*
	 * This Object is for internal communication between nodes within the chord ring.
	 * Different types of internal command:
	 * 1. create (1st join)
	 * --> cmdType
	 * --> hostIP
	 * --> hostPort
	 * 2. join
	 * --> cmdType
	 * --> hostIP
	 * --> hostPort
	 * 3. leave
	 * --> cmdType
	 * --> hostIP
	 * --> hostPort
	 * --> extraInfo(TBD)
	 * 4. search
	 * --> cmdType
	 * --> ansIP
	 * --> ansPort
	 * --> Keyword
	 * --> extraInfo(TBD)
	 * 5. track
	 * --> cmdType
	 * --> hostIP(OP)
	 * --> hostPort(OP)
	 * 6. abnormal leave (failed track)
	 * --> cmdType
	 * --> missIP
	 * --> missPort
	 * --> extraInfo(TBD)
	 * All the above is generated using different constructor.
	 * IP and Port comes in sequence, blank field should be put as null.
	 */
	private static final long serialVersionUID = 2699783128689438022L;
	private String cmdType;
	private String IP1;
	private Integer port1;
	//private String IP2;
	//private Integer port2;
	private String Keyword;
	private Node data;
	//
	// default
	public InternalCommand()
	{
		this.cmdType = null;
		this.IP1 = null;
		this.port1 = new Integer(-1);
		this.Keyword = null;
		this.data = null;
	}
	// 1. create + 2. join + 3. leave(TBD) + 5. track(TBD) + 6. abnormal leave(TBD)
	public InternalCommand(String cmdType_src, String hostIP_src, Integer hostPort_src)
	{
		this.cmdType = cmdType_src;
		this.IP1 = hostIP_src;
		this.port1 = hostPort_src;
		this.Keyword = null;
		this.data = null;
	}
	// 4. search
	public InternalCommand(String cmdType_src, String hostIP_src, Integer hostPort_src, String Keyword_src)
	{
		this.cmdType = cmdType_src;
		this.IP1 = hostIP_src;
		this.port1 = hostPort_src;
		this.Keyword = Keyword_src;
		this.data = null;
	}
	// minions
	public InternalCommand(String cmdType_src, Node src)
	{
		this.cmdType = cmdType_src;
		this.IP1 = null;
		this.port1 = null;
		this.Keyword = null;
		this.data = src;
	}
	
	// get func
	public String getCmdType()
	{
		return this.cmdType;
	}
	
	public String getIP1()
	{
		return this.IP1;
	}
	
	public Integer getPort1()
	{
		return this.port1;
	}
	
	public String getKeyword()
	{
		return this.Keyword;
	}
	
	public Node getData()
	{
		return this.data;
	}
}
