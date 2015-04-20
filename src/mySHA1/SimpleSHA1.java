package mySHA1;

import java.security.*;

public class SimpleSHA1 {
	// Get SHA1 String from scratch
	public static String getSHA1(String src) throws NoSuchAlgorithmException
	{
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
		byte[] res = mDigest.digest(src.getBytes());
		// Now change 20-byte SHA1 identifier into 5 4-byte long num
		long data[] = new long[5];
		int cnt = -1;
		int length = res.length;
		for(int i = 0; i < length; i++)
		{
			if(i%4 == 0) cnt++;
			int choice = i%4;
			switch(choice)
			{
			case 0:
				data[cnt] += (res[i] & 0xFFL) << 24;
				break;
			case 1:
				data[cnt] += (res[i] & 0xFFL) << 16;
				break;
			case 2:
				data[cnt] += (res[i] & 0xFFL) << 8;
				break;
			default:
				data[cnt] += (res[i] & 0xFFL);
			}
		}
		// Now XOR them together
		for(int i = 1; i < 5; i++)
		{
			data[0] ^= data[i];
		}
		//
		String ret = Long.toHexString(data[0]);
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < 8-ret.length(); i++)
		{
			prefix.append("0");
		}
		prefix.append(ret);
		return prefix.toString();
	}
	
	// Helper func to get numeric value for SHA1, for comparison
	public static Long SHA1ToLong(String src)
	{
		return new Long(Long.parseUnsignedLong(src, 16));
	}
	
	// Helper func to get SHA1 String from numeric value, for obj passing
	public static String LongToSHA1(Long src)
	{
		String ret = Long.toHexString(src);
		StringBuilder prefix = new StringBuilder();
		for(int i = 0; i < 8-ret.length(); i++)
		{
			prefix.append("0");
		}
		prefix.append(ret);
		return prefix.toString();
	}
	
	//
	public static long round_pos(long left, long right)
	{
		// left - right
		if(left > right) return left - right;
		else
		{
			long boundary = (long)java.lang.Math.pow(2, 32);
			return (boundary + left - right)%boundary;
		}
	}
}
