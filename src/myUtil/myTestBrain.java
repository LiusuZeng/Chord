package myUtil;

import java.security.NoSuchAlgorithmException;
import java.io.*;
import java.net.*;

import mySHA1.SimpleSHA1;

public class myTestBrain {

	public static void main(String[] args) throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		ServerSocket mother = new ServerSocket(8000);
		while(true)
		{
			Socket thisTime = mother.accept();
			ObjectInputStream is = new ObjectInputStream(thisTime.getInputStream());
			String msg = (String)is.readObject();
			StringBuilder tool = new StringBuilder();
			tool.append(msg + msg + msg);
			ObjectOutputStream os = new ObjectOutputStream(thisTime.getOutputStream());
			os.writeObject(tool.toString());
			os.flush();
			//
			is.close();
			os.close();
		}
	}

}
