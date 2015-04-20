package myThr;

import java.net.*;
import java.io.*;

import myInfo.*;

public class NodeKeepAliveThr extends Thread {
	private Node mySucc;

	public NodeKeepAliveThr(Node src)
	{
		this.mySucc = src;
	}

	// need lock
	public void run()
	{
		while(true)
		{
			synchronized(this.mySucc)
			{
				try {
					Socket attempt= new Socket(this.mySucc.FingerTable[0].IP, this.mySucc.FingerTable[0].Port);
					attempt.setSoTimeout(1000); // 1s must have feedback, otherwise, throw exception
					ObjectOutputStream os_attempt = new ObjectOutputStream(attempt.getOutputStream());
					InternalCommand inst_attempt = new InternalCommand("track", null);
					os_attempt.writeObject(inst_attempt);
					// get feedback
					ObjectInputStream is_attempt = new ObjectInputStream(attempt.getInputStream());
					String msg = (String)is_attempt.readObject();
					//
					is_attempt.close();
					os_attempt.close();
					attempt.close();
					//
					if(!msg.equals("ScrewU")) 
						throw new IOException("tunnel polluted");
					//
					Thread.sleep(5000); // check every 5s
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//System.out.println(e.toString());
					// update the ring
					System.out.println("" + this.mySucc.Port + " --> " + this.mySucc.FingerTable[0].Port + ": " + "abnormal leave, recovery starts!!!");
					//
					Socket call_leave;
					try {
						call_leave = new Socket(this.mySucc.IP, this.mySucc.Port);
						call_leave.setReuseAddress(true);
						//
						ObjectOutputStream os_call_leave = new ObjectOutputStream(call_leave.getOutputStream());
						InternalCommand inst_call_leavev = new InternalCommand("leave", null);
						os_call_leave.writeObject(inst_call_leavev);
						os_call_leave.flush();
						//
						ObjectInputStream is_call_leave = new ObjectInputStream(call_leave.getInputStream());
						String fdbk_leave = (String)is_call_leave.readObject();
						//
						call_leave.close();
						System.out.println("Recovery completes.");
						//
					} catch (IOException | ClassNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						// do nothing, assume 1 node leave/join at a time
					}
					//
					this.mySucc.notify();
					//
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					this.mySucc.notify();
				}
			}

		}
	}

	// need lock
	public void change(Node new_src)
	{
		synchronized(this.mySucc)
		{
			this.mySucc = new_src;
		}
	}
}
