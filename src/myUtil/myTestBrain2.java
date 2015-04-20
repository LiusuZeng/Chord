package myUtil;
import java.io.*;
import java.net.*;

public class myTestBrain2 {

	public static void main(String[] args) throws Throwable, IOException {
		// TODO Auto-generated method stub
		Socket son = new Socket("localhost", 8000);
		ObjectOutputStream os = new ObjectOutputStream(son.getOutputStream());
		os.writeObject("Fuck you!");
		os.flush();
		ObjectInputStream is = new ObjectInputStream(son.getInputStream());
		String echo = (String)is.readObject();
		//
		System.out.println("From the server: " + echo);
	}

}
