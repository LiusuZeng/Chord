package myThr;

import java.net.*;
import java.io.*;

import myInfo.*;
import mySHA1.SimpleSHA1;
import myUtil.ManualIntervalControl;

public class NodeProcessThr extends Thread {
	private Socket mySock;
	private Node myNode;

	public NodeProcessThr(Socket Socket_src, Node Node_src)
	{
		this.mySock = Socket_src;
		this.myNode = Node_src;
	}

	public void run()
	{
		try {
			ObjectInputStream is = new ObjectInputStream(this.mySock.getInputStream());
			ObjectOutputStream os = null;
			InternalCommand thisTime = (InternalCommand)is.readObject();
			String choice = thisTime.getCmdType();
			switch(choice)
			{
			case "create":
				synchronized(this.myNode)
				{
					//
					for(int i = 0; i < this.myNode.FingerTable.length; i++)
					{
						this.myNode.FingerTable[i] = this.myNode;
					}
					this.myNode.prev = this.myNode;
					this.myNode.succ2 = this.myNode;
					//
					System.out.println("Your predecessor is node " + this.myNode.prev.IP + ", port " + this.myNode.prev.Port + ", position " + this.myNode.prev.HashValue);
					System.out.println("Your successor is node " + this.myNode.FingerTable[0].IP + ", port " + this.myNode.FingerTable[0].Port + ", position " + this.myNode.FingerTable[0].HashValue);
					//
					os = new ObjectOutputStream(this.mySock.getOutputStream());
					os.writeObject("fdbk_create");
					os.flush();
					//
					this.myNode.notify();
					//
				}
				break;
			case "join":
				//
				synchronized(this.myNode)
				{
					Node mentor = thisTime.getData();
					Socket call_init_tb = new Socket(this.myNode.IP, this.myNode.Port);
					ObjectOutputStream os_call_init_tb = new ObjectOutputStream(call_init_tb.getOutputStream());
					InternalCommand inst_call_init_tb = new InternalCommand("init_finger_table", mentor);
					os_call_init_tb.writeObject(inst_call_init_tb);
					os_call_init_tb.flush();
					// feedback indicating the end of the execution
					ObjectInputStream is_call_init_tb = new ObjectInputStream(call_init_tb.getInputStream());
					String fdbk1 = (String)is_call_init_tb.readObject();
					//
					call_init_tb.close();
					//
					Socket call_updt_others = new Socket(this.myNode.IP, this.myNode.Port);
					ObjectOutputStream os_call_updt_others = new ObjectOutputStream(call_updt_others.getOutputStream());
					InternalCommand inst_call_updt_others = new InternalCommand("update_others", null);
					os_call_updt_others.writeObject(inst_call_updt_others);
					os_call_updt_others.flush();
					// feedback indicating the end of the execution
					ObjectInputStream is_call_updt_others = new ObjectInputStream(call_updt_others.getInputStream());
					String fdbk2 = (String)is_call_updt_others.readObject();
					//
					call_updt_others.close();
					// At this time, all of the info the current node should be up-to-date
					// Then we update the next successor
					// self
					Socket tunnel_succ_self = new Socket(this.myNode.FingerTable[0].IP, this.myNode.FingerTable[0].Port);
					ObjectOutputStream os_succ_self = new ObjectOutputStream(tunnel_succ_self.getOutputStream());
					InternalCommand inst_succ_self = new InternalCommand("get_dir_succ", null);
					os_succ_self.writeObject(inst_succ_self);
					os_succ_self.flush();
					//
					ObjectInputStream is_succ_self = new ObjectInputStream(tunnel_succ_self.getInputStream());
					this.myNode.succ2 = (Node)is_succ_self.readObject();
					// pre
					Socket tunnel_succ_pre = new Socket(this.myNode.prev.IP, this.myNode.prev.Port);
					ObjectOutputStream os_succ_pre = new ObjectOutputStream(tunnel_succ_pre.getOutputStream());
					InternalCommand inst_succ_pre = new InternalCommand("ModifySucc2", this.myNode.FingerTable[0]);
					os_succ_pre.writeObject(inst_succ_pre);
					os_succ_pre.flush();
					// prepre
					Socket tunnel_succ_prepre = new Socket(this.myNode.prev.IP, this.myNode.prev.Port);
					ObjectOutputStream os_succ_prepre = new ObjectOutputStream(tunnel_succ_prepre.getOutputStream());
					InternalCommand inst_getprepre = new InternalCommand("get_prev", null);
					os_succ_prepre.writeObject(inst_getprepre);
					os_succ_prepre.flush();
					//
					ObjectInputStream is_succ_prepre = new ObjectInputStream(tunnel_succ_prepre.getInputStream());
					Node my_prepre = (Node)is_succ_prepre.readObject();
					//
					tunnel_succ_prepre.close();
					//
					Socket tunnel_set_prepre = new Socket(my_prepre.IP, my_prepre.Port);
					ObjectOutputStream os_set_prepre = new ObjectOutputStream(tunnel_set_prepre.getOutputStream());
					InternalCommand inst_succ_prepre = new InternalCommand("ModifySucc2", this.myNode);
					os_set_prepre.writeObject(inst_succ_prepre);
					os_set_prepre.flush();
					//
					System.out.println("Your predecessor is node " + this.myNode.prev.IP + ", port " + this.myNode.prev.Port + ", position " + this.myNode.prev.HashValue);
					System.out.println("Your successor is node " + this.myNode.FingerTable[0].IP + ", port " + this.myNode.FingerTable[0].Port + ", position " + this.myNode.FingerTable[0].HashValue);
					// [DEBUG]
					/*
					System.out.println("Initial finger table:");
					for(int xxx = 0; xxx < this.myNode.FingerTable.length; xxx++)
					{
						System.out.println("" + xxx + " th: " + this.myNode.FingerTable[xxx].Port);
					}
					*/
					//
					os = new ObjectOutputStream(this.mySock.getOutputStream());
					os.writeObject("fdbk_join");
					os.flush();
					//
					this.myNode.notify();
				}
				break;
			case "leave":
				//
				Node troublemaker = this.myNode.FingerTable[0];
				// step1: update all others' finger table, including self
				Socket rip = new Socket(this.myNode.IP, this.myNode.Port);
				ObjectOutputStream os_rip = new ObjectOutputStream(rip.getOutputStream());
				InternalCommand inst_rip = new InternalCommand("update_others_leave", this.myNode.FingerTable[0]);
				os_rip.writeObject(inst_rip);
				os_rip.flush();
				//
				os_rip.writeObject(this.myNode.succ2);
				os_rip.flush();
				// feedback indicating the end of the execution
				ObjectInputStream is_rip = new ObjectInputStream(rip.getInputStream());
				String fdbk_rip = (String)is_rip.readObject();
				//
				rip.close();
				// step2: update related nodes' next successor, including self
				// -- self
				Node next = this.myNode.FingerTable[0];
				Socket get_succ2_self = new Socket(next.IP, next.Port);
				ObjectOutputStream os_get_succ2_self = new ObjectOutputStream(get_succ2_self.getOutputStream());
				InternalCommand inst_get_succ2_self = new InternalCommand("get_dir_succ", null);
				os_get_succ2_self.writeObject(inst_get_succ2_self);
				os_get_succ2_self.flush();
				//
				ObjectInputStream is_get_succ2_self = new ObjectInputStream(get_succ2_self.getInputStream());
				this.myNode.succ2 = (Node)is_get_succ2_self.readObject();
				//
				get_succ2_self.close();
				// -- pre
				Node my_pre = this.myNode.prev;
				if(!my_pre.HashValue.equals(troublemaker.HashValue)) // in case of 2 nodes cut into 1 node in the ring
				{
					Socket get_succ2_pre = new Socket(my_pre.IP, my_pre.Port);
					ObjectOutputStream os_get_succ2_pre = new ObjectOutputStream(get_succ2_pre.getOutputStream());
					InternalCommand inst_get_succ2_pre = new InternalCommand("ModifySucc2", this.myNode.FingerTable[0]);
					os_get_succ2_pre.writeObject(inst_get_succ2_pre);
					os_get_succ2_pre.flush();
				}

				// step3: update related node's prev
				Socket get_prev_succ = new Socket(this.myNode.FingerTable[0].IP, this.myNode.FingerTable[0].Port);
				ObjectOutputStream os_get_prev_succ = new ObjectOutputStream(get_prev_succ.getOutputStream());
				InternalCommand inst_get_prev_succ = new InternalCommand("Modify", this.myNode);
				os_get_prev_succ.writeObject(inst_get_prev_succ);
				os_get_prev_succ.flush();

				// At this time, all of the info related to this leaving node should be up-to-date
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("leave_fin");
				break;
			case "search":
				// used by Query from user
				Socket getAnswerServer = new Socket(this.myNode.IP, this.myNode.Port);
				ObjectOutputStream osGAS = new ObjectOutputStream(getAnswerServer.getOutputStream());
				InternalCommand getAnswerServerInfo = new InternalCommand("find_successor", thisTime.getIP1(), thisTime.getPort1(), thisTime.getKeyword());
				osGAS.writeObject(getAnswerServerInfo);
				//
				ObjectInputStream isGAS = new ObjectInputStream(getAnswerServer.getInputStream());
				Node answerServer = (Node)isGAS.readObject();
				//
				isGAS.close();
				// Get real server info to client
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject(answerServer.IP);
				os.writeObject(answerServer.Port);
				//
				break;
			case "track":
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("ScrewU");
				os.flush();
				//
				break;
				/*********************minion funcs*********************/
			case "ModifySucc2":
				//
				Node new_succ2 = thisTime.getData();
				this.myNode.succ2 = new_succ2;
				//
				break;
			case "Modify":
				//
				Node chg = thisTime.getData();
				this.myNode.prev = chg;
				//
				break;
			case "get_prev":
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject(this.myNode.prev);
				os.flush();
				//
				break;
			case "get_dir_succ":
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject(this.myNode.FingerTable[0]);
				os.flush();
				//
				break;
			case "dir_search":
				// do "actual" search
				ObjectOutputStream os_back = new ObjectOutputStream(this.mySock.getOutputStream());
				String resp_head = "Response from node " + this.myNode.IP + ", port " + this.myNode.Port + ", position " + this.myNode.HashValue + ":";
				String resp_res = "Not found."; // This should be the place where local hash table search begins
				os_back.writeObject(resp_head);
				os_back.writeObject(resp_res);
				//
				break;
			case "closest_preceding_finger":
				//
				for(int i = this.myNode.FingerTable.length-1; i >= 0; i--)
				{
					if(ManualIntervalControl.isBetween(this.myNode.FingerTable[i].HashValue, this.myNode.HashValue, thisTime.getKeyword(), false, false))
					{
						os = new ObjectOutputStream(this.mySock.getOutputStream());
						os.writeObject(this.myNode.FingerTable[i]); // OMG.. This is a horrible bug!
						os.flush();
						//
						//os.close();
						//
						//this.mySock.close();
						return;
					}
				}
				//
				ObjectOutputStream xos = new ObjectOutputStream(this.mySock.getOutputStream());
				xos.writeObject(this.myNode);
				xos.flush();
				//
				//xos.close();
				break;
			case "find_predecesor":
				//
				Node temp = this.myNode;
				//
				Socket tunnel_temp_succ = new Socket(temp.IP, temp.Port);
				ObjectOutputStream os_temp_succ = new ObjectOutputStream(tunnel_temp_succ.getOutputStream());
				InternalCommand inst_temp_succ = new InternalCommand("get_dir_succ", null);
				os_temp_succ.writeObject(inst_temp_succ);
				os_temp_succ.flush();
				//
				ObjectInputStream is_temp_succ = new ObjectInputStream(tunnel_temp_succ.getInputStream());
				Node temp_succ = (Node)is_temp_succ.readObject();
				//
				tunnel_temp_succ.close();
				//
				while(!ManualIntervalControl.isBetween(thisTime.getKeyword(), temp.HashValue, temp_succ.HashValue, false, true)) // Horrible bug!
				{
					// RMI starts here
					Socket otherHelp = null;
					otherHelp = new Socket(temp.IP, temp.Port);
					//
					ObjectOutputStream os_fp = new ObjectOutputStream(otherHelp.getOutputStream());
					InternalCommand inst = new InternalCommand("closest_preceding_finger", null, null, thisTime.getKeyword());
					os_fp.writeObject(inst);
					os_fp.flush();
					//
					ObjectInputStream iss = new ObjectInputStream(otherHelp.getInputStream());
					temp = (Node)(iss.readObject());
					//
					otherHelp.close();
					//
					Socket tunnel_temp_succ2 = new Socket(temp.IP, temp.Port);
					ObjectOutputStream os_temp_succ2 = new ObjectOutputStream(tunnel_temp_succ2.getOutputStream());
					InternalCommand inst_temp_succ2 = new InternalCommand("get_dir_succ", null);
					os_temp_succ2.writeObject(inst_temp_succ2);
					os_temp_succ2.flush();
					//
					ObjectInputStream is_temp_succ2 = new ObjectInputStream(tunnel_temp_succ2.getInputStream());
					temp_succ = (Node)is_temp_succ2.readObject();
					//
					tunnel_temp_succ2.close();
				}
				//
				ObjectOutputStream oos = new ObjectOutputStream(this.mySock.getOutputStream());
				oos.writeObject(temp);
				oos.flush();
				//
				//oos.close();
				break;
			case "find_successor":
				//
				Node ret = new Node(this.myNode);
				Socket helpMyself = new Socket(this.myNode.IP, this.myNode.Port);
				ObjectOutputStream osfs = new ObjectOutputStream(helpMyself.getOutputStream());
				InternalCommand instfs = new InternalCommand("find_predecesor", null, null, thisTime.getKeyword());
				osfs.writeObject(instfs);
				osfs.flush();
				//
				ObjectInputStream isfs = new ObjectInputStream(helpMyself.getInputStream());
				ret = (Node)(isfs.readObject());
				//
				helpMyself.close();
				////////////////////
				Socket tunnel_get_succ = new Socket(ret.IP, ret.Port);
				ObjectOutputStream os_get_succ = new ObjectOutputStream(tunnel_get_succ.getOutputStream());
				InternalCommand inst_get_succ = new InternalCommand("get_dir_succ", null);
				os_get_succ.writeObject(inst_get_succ);
				os_get_succ.flush();
				//
				ObjectInputStream is_get_succ = new ObjectInputStream(tunnel_get_succ.getInputStream());
				ret = (Node)is_get_succ.readObject();
				//
				tunnel_get_succ.close();
				//
				ObjectOutputStream oosfs = new ObjectOutputStream(this.mySock.getOutputStream());
				oosfs.writeObject(ret);
				oosfs.flush();
				//
				break;
			case "init_finger_table":
				// finger[1].node = n_prime.find_successor(finger[1].start);
				Node n_prime = thisTime.getData();
				Socket n_prime_fs = new Socket(n_prime.IP, n_prime.Port);
				ObjectOutputStream os_n_prime_fs = new ObjectOutputStream(n_prime_fs.getOutputStream());
				InternalCommand inst_n_prime_fs = new InternalCommand("find_successor", null, null, SimpleSHA1.LongToSHA1(this.myNode.getStartAddr(0)));
				os_n_prime_fs.writeObject(inst_n_prime_fs);
				os_n_prime_fs.flush();
				//
				ObjectInputStream is_n_prime_fs = new ObjectInputStream(n_prime_fs.getInputStream());
				this.myNode.FingerTable[0] = (Node)is_n_prime_fs.readObject();
				//
				n_prime_fs.close();
				// predecessor = successor.predecessor; // Should never visit FingerTable member's sub-data-structure! Should do it thru socket
				Socket tunnel_prev = new Socket(this.myNode.FingerTable[0].IP, this.myNode.FingerTable[0].Port);
				ObjectOutputStream os_tunnel_prev = new ObjectOutputStream(tunnel_prev.getOutputStream());
				InternalCommand inst_tunnel_prev = new InternalCommand("get_prev", null);
				os_tunnel_prev.writeObject(inst_tunnel_prev);
				os_tunnel_prev.flush();
				//
				ObjectInputStream is_tunnel_prev = new ObjectInputStream(tunnel_prev.getInputStream());
				this.myNode.prev = (Node)is_tunnel_prev.readObject();
				//
				tunnel_prev.close();
				// successor.predecessor = n; // Modifying remote node info! Should be done thru socket!
				//this.myNode.FingerTable[0].prev = this.myNode;
				Socket modify = new Socket(this.myNode.FingerTable[0].IP, this.myNode.FingerTable[0].Port);
				ObjectOutputStream os_modify = new ObjectOutputStream(modify.getOutputStream());
				InternalCommand inst_modify = new InternalCommand("Modify", this.myNode);
				os_modify.writeObject(inst_modify);
				os_modify.flush();
				// for i = 1 to m-1
				for(int i = 0; i < this.myNode.FingerTable.length-1; i++)
				{
					//  if(finger[i+1].start ibtw [n,finger[i].node))
					if(ManualIntervalControl.isBetween(SimpleSHA1.LongToSHA1(this.myNode.getStartAddr(i+1)), this.myNode.HashValue, this.myNode.FingerTable[i].HashValue, true, false))
					{
						//   finger[i+1].node = finger[i].node;
						this.myNode.FingerTable[i+1] = this.myNode.FingerTable[i];
					}
					// else
					else
					{
						//   finger[i+1].node = n_prime.find_successor(finger[i+1].start);
						Socket n_prime_fs2 = new Socket(n_prime.IP, n_prime.Port);
						ObjectOutputStream os_n_prime_fs2 = new ObjectOutputStream(n_prime_fs2.getOutputStream());
						InternalCommand inst_n_prime_fs2 = new InternalCommand("find_successor", null, null, SimpleSHA1.LongToSHA1(this.myNode.getStartAddr(i+1)));
						os_n_prime_fs2.writeObject(inst_n_prime_fs2);
						os_n_prime_fs2.flush();
						//
						ObjectInputStream is_n_prime_fs2 = new ObjectInputStream(n_prime_fs2.getInputStream());
						this.myNode.FingerTable[i+1] = (Node)is_n_prime_fs2.readObject();
						//
						n_prime_fs2.close();
					}
				}
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("init_fin");
				os.flush();
				//
				break;
			case "update_others_leave":
				Node leave = thisTime.getData(); // the one that leaves
				//
				//ObjectInputStream is_instead = new ObjectInputStream(this.mySock.getInputStream());
				Node instead = (Node)is.readObject();// the one that succeeds the one that leaves, used for update
				//
				for(int i = 0; i < this.myNode.FingerTable.length; i++)
				{
					Long opl = SimpleSHA1.SHA1ToLong(leave.HashValue);
					Long opr = new Long((long)java.lang.Math.pow(2, i)); // i starts from 0
					String fp_para = SimpleSHA1.LongToSHA1(SimpleSHA1.round_pos(opl, opr));
					Socket call_fp = new Socket(this.myNode.IP, this.myNode.Port);
					ObjectOutputStream os_call_fp = new ObjectOutputStream(call_fp.getOutputStream());
					InternalCommand inst_call_fp = new InternalCommand("find_predecesor", null, null, fp_para);
					os_call_fp.writeObject(inst_call_fp);
					os_call_fp.flush();
					//
					ObjectInputStream is_call_fp = new ObjectInputStream(call_fp.getInputStream());
					Node uo_node_p = (Node)is_call_fp.readObject();
					//
					call_fp.close();
					//
					Socket uo_node_p_call_uft = new Socket(uo_node_p.IP, uo_node_p.Port);
					ObjectOutputStream uo_node_p_os_call_uft = new ObjectOutputStream(uo_node_p_call_uft.getOutputStream());
					InternalCommand uo_node_p_inst_call_uft = new InternalCommand("update_finger_table_leave", instead);
					uo_node_p_os_call_uft.writeObject(uo_node_p_inst_call_uft);
					uo_node_p_os_call_uft.flush();
					//
					uo_node_p_os_call_uft.writeObject(new Integer(i));
					uo_node_p_os_call_uft.flush();
					//
					uo_node_p_os_call_uft.writeObject(leave);
					uo_node_p_os_call_uft.flush();
					//
					ObjectInputStream uo_node_p_is_call_uft = new ObjectInputStream(uo_node_p_call_uft.getInputStream());
					String fdbk_uo_node_p_call_uft = (String)uo_node_p_is_call_uft.readObject();
					//
					uo_node_p_call_uft.close();
				}
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("updt_others_leave_fin");
				os.flush();
				//
				break;
			case "update_others":
				//
				for(int i = 0; i < this.myNode.FingerTable.length; i++)
				{
					Long opl = SimpleSHA1.SHA1ToLong(this.myNode.HashValue);
					Long opr = new Long((long)java.lang.Math.pow(2, i));
					String fp_para = SimpleSHA1.LongToSHA1(SimpleSHA1.round_pos(opl, opr));
					Socket call_fp = new Socket(this.myNode.IP, this.myNode.Port);
					ObjectOutputStream os_call_fp = new ObjectOutputStream(call_fp.getOutputStream());
					InternalCommand inst_call_fp = new InternalCommand("find_predecesor",null,null,fp_para);
					os_call_fp.writeObject(inst_call_fp);
					os_call_fp.flush();
					//
					ObjectInputStream is_call_fp = new ObjectInputStream(call_fp.getInputStream());
					Node uo_node_p = (Node)is_call_fp.readObject();
					//
					call_fp.close();
					//
					Socket uo_node_p_call_uft = new Socket(uo_node_p.IP, uo_node_p.Port);
					ObjectOutputStream uo_node_p_os_call_uft = new ObjectOutputStream(uo_node_p_call_uft.getOutputStream());
					InternalCommand uo_node_p_inst_call_uft = new InternalCommand("update_finger_table", this.myNode);
					uo_node_p_os_call_uft.writeObject(uo_node_p_inst_call_uft);
					uo_node_p_os_call_uft.flush();
					//
					uo_node_p_os_call_uft.writeObject(new Integer(i));
					uo_node_p_os_call_uft.flush();
					//
					ObjectInputStream uo_node_p_is_call_uft = new ObjectInputStream(uo_node_p_call_uft.getInputStream());
					String fdbk_uo_node_p_call_uft = (String)uo_node_p_is_call_uft.readObject();
					//
					uo_node_p_call_uft.close();
				}
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("updt_others_fin");
				os.flush();
				//
				break;
			case "update_finger_table_leave":
				//
				Integer index_i_leave = (Integer)is.readObject();
				Node lost = (Node)is.readObject();
				Node node_s_leave = thisTime.getData();
				//
				if(this.myNode.FingerTable[index_i_leave].HashValue.equals(lost.HashValue))
				{
					this.myNode.FingerTable[index_i_leave] = node_s_leave; // use its first successor to update the position it appears in other existing node
					Node node_p_leave = this.myNode.prev;
					//
					if(node_p_leave.HashValue.equals(lost.HashValue)) {}
					//
					else
					{
						Socket tunnel_uft_l = new Socket(node_p_leave.IP, node_p_leave.Port);
						ObjectOutputStream os_uft_l = new ObjectOutputStream(tunnel_uft_l.getOutputStream());
						InternalCommand inst_uft_l = new InternalCommand("update_finger_table_leave", node_s_leave);
						os_uft_l.writeObject(inst_uft_l);
						os_uft_l.flush();
						os_uft_l.writeObject(new Integer(index_i_leave));
						os_uft_l.flush();
						os_uft_l.writeObject(lost);
						os_uft_l.flush();
						//
						ObjectInputStream is_uft_l = new ObjectInputStream(tunnel_uft_l.getInputStream());
						String fdbk_uft = (String)is_uft_l.readObject();
						//
						tunnel_uft_l.close();
					}
				}
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("updt_ft_l_fin");
				os.flush();
				//
				break;
			case "update_finger_table":
				//
				Integer index_i = (Integer)is.readObject();
				Node node_s = thisTime.getData();
				// [DEBUG]
				/*
				System.out.println("Condition: " + index_i);
				System.out.println("existing entry: " + SimpleSHA1.SHA1ToLong(this.myNode.FingerTable[index_i].HashValue));
				System.out.println("new entry: " + SimpleSHA1.SHA1ToLong(node_s.HashValue));
				System.out.println("start point: " + this.myNode.getStartAddr(index_i));
				System.out.println("*************************************************************************");
				*/
				//
				long dist1 = SimpleSHA1.round_pos(SimpleSHA1.SHA1ToLong(this.myNode.FingerTable[index_i].HashValue), this.myNode.getStartAddr(index_i));
				long dist2 = SimpleSHA1.round_pos(SimpleSHA1.SHA1ToLong(node_s.HashValue), this.myNode.getStartAddr(index_i));
				if(dist1 > dist2)
				{
				    //System.out.println("Update local finger table: " + this.myNode.Port + ": " + this.myNode.FingerTable[index_i].Port + "-->" + node_s.Port);
				    //System.out.println("At index: " + index_i + "\n");
					this.myNode.FingerTable[index_i] = node_s;
					//
					Node node_p = this.myNode.prev;
					//
					Socket tunnel_uft = new Socket(node_p.IP, node_p.Port);
					ObjectOutputStream os_uft = new ObjectOutputStream(tunnel_uft.getOutputStream());
					InternalCommand inst_uft = new InternalCommand("update_finger_table", node_s);
					os_uft.writeObject(inst_uft);
					os_uft.flush();
					os_uft.writeObject(new Integer(index_i));
					os_uft.flush();
					//
					ObjectInputStream is_uft = new ObjectInputStream(tunnel_uft.getInputStream());
					String fdbk_uft = (String)is_uft.readObject();
					//
					tunnel_uft.close();
				}
				//
				os = new ObjectOutputStream(this.mySock.getOutputStream());
				os.writeObject("updt_ft_fin");
				os.flush();
				//
				break;
				/******************************************************/
			default:
				break; // do nothing for the unsupported
			}
			//
			//this.mySock.close();

		} catch (IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
