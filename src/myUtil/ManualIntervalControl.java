package myUtil;

import mySHA1.SimpleSHA1;

public class ManualIntervalControl {
	public static boolean isBetween(String src, String hash1, String hash2, boolean leftClose, boolean rightClose)
	{
		long num = SimpleSHA1.SHA1ToLong(src);
		long numleft = SimpleSHA1.SHA1ToLong(hash1);
		long numright = SimpleSHA1.SHA1ToLong(hash2);
		//
		if(numleft < numright)
		{
			// (a,b)
			if(!leftClose && !rightClose) return num > numleft && num < numright;
			// [a,b]
			else if(leftClose && rightClose) return num >= numleft && num <= numright;
			// (a,b]
			else if(!leftClose && rightClose) return num > numleft && num <= numright;
			// [a,b)
			else return num >= numleft && num < numright;
		}
		else if(numleft == numright) // there is only one node in the chord ring...
			return true;
		else
		{
			// (a,b)
			if(!leftClose && !rightClose) return num > numleft || num < numright;
			// [a,b]
			else if(leftClose && rightClose) return num >= numleft || num <= numright;
			// (a,b]
			else if(!leftClose && rightClose) return num > numleft || num <= numright;
			// [a,b)
			else return num >= numleft || num < numright;
		}
	}
}
