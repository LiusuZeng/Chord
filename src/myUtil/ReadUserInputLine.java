package myUtil;

import java.io.*;

public class ReadUserInputLine {
	public static String readLine() throws IOException
	{
		InputStreamReader isr = new InputStreamReader(System.in);
		return new BufferedReader(isr).readLine();
	}
}
