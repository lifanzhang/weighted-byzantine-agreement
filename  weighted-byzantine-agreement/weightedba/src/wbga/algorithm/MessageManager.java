package wbga.algorithm;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MessageManager {
	
	//Singleton class
	private static MessageManager instance = null;	
	
	private int myId;
	
	private Socket[] sockets;
	private PrintWriter[] dataOut;
	private BufferedReader[] dataIn;	
	
	private Socket testerSocket;
	private PrintWriter testerDataOut;
	private BufferedReader testerDataIn;
	
	private MessageHandler msgHandler;
	
	protected MessageManager() {
		//Singleton class
	}
	
	public static MessageManager getInstance(){ 
		if (instance == null) {
			instance = new MessageManager();
		}
		return instance;
	}
	
	public void init(int myId, int numProcesses) {
		this.myId = myId;
		sockets = new Socket[numProcesses];
		dataOut = new PrintWriter[numProcesses];
		dataIn = new BufferedReader[numProcesses];
	}
	
	public void setMessageHandler(MessageHandler msgHandler) {
		this.msgHandler = msgHandler;
	}
	
	public Socket getChannelSocket(int id) {
		return sockets[id];
	}	
	public void setChannelSocket(int id, Socket s) {
		sockets[id] = s; 
	}
	
	public BufferedReader getChannelInput(int id){
		return dataIn[id];
	}	
	public void setChannelInput(int id, BufferedReader br) {		
		dataIn[id] = br;
	}
	
	public PrintWriter getChannelOutput(int id) {
		return dataOut[id];
	}		
	public void setChannelOutput(int id, PrintWriter pr) {
		dataOut[id] = pr;
	}
	
	public Socket getTesterSocket(){ 
		return testerSocket;
	}
	public void setTesterSocket(Socket s) {
		testerSocket = s;
	}
	
	public BufferedReader getTesterInput() {
		return testerDataIn;	
	}
	public void setTesterInput(BufferedReader br) {
		testerDataIn = br;
	}
	
	public PrintWriter getTesterOutput(){ 
		return testerDataOut;
	}	
	public void setTesterOutput(PrintWriter pr) {
		testerDataOut = pr;
	}
	
	/**
	 * Process message format: "<srcId> <destId> <tag> <message>#"
	 */
	public void sendMessage(int destId, String tag, String message){
		if(destId != myId) {
			dataOut[destId].println(myId + " " + destId + " " + tag + " " + message + "#");
			dataOut[destId].flush();
		}
		else { //sending message to self
			receiveMessage(myId, myId + " " + destId + " " + tag + " " + message + "#");
		}
	}
	
	/**
	 * Tester message format: "<srcId> <tag> <message>#"
	 */
	public void sendTesterMessage(String tag, String message) {		
		testerDataOut.println(myId + " " + tag + " " + message + "#");
		testerDataOut.flush();
	}
	
	public void receiveMessage(int fromId, String line) {
		Message m = Message.parseMessage(line);
		msgHandler._handleMessage(m, m.getSrcId(), m.getTag());		
	}

}
