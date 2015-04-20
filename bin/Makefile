target:
	javac ./mySHA1/SimpleSHA1.java -d ../classes
	javac ./myUtil/ManualIntervalControl.java -d ../classes
	javac ./myUtil/ReadUserInputLine.java -d ../classes
	javac ./myInfo/Node.java -d ../classes -classpath ../classes
	javac ./myInfo/InternalCommand.java -d ../classes -classpath ../classes
	javac ./myThr/NodeProcessThr.java -d ../classes -classpath ../classes
	javac ./myThr/NodeListenThr.java -d ../classes -classpath ../classes
	javac ./myThr/NodeKeepAliveThr.java -d ../classes -classpath ../classes
	javac ./userInterface/Chord.java -d ../classes -classpath ../classes
	javac ./userInterface/Query.java -d ../classes -classpath ../classes
clean:
	rm -rf ../classes/myInfo/*.class
	rm -rf ../classes/mySHA1/*.class
	rm -rf ../classes/myThr/*.class
	rm -rf ../classes/myUtil/*.class
	rm -rf ../classes/userInterface/*.class
