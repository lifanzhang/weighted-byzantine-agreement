package wbga.algorithm;

public abstract class AbstractWBGA implements MessageHandler{	

	protected int myId;
	protected int numProcesses;
	
	private MessageManager msgManager;
	
	public AbstractWBGA(int myId, int numProcesses, MessageManager msgManager){
		this.myId = myId;
		this.numProcesses = numProcesses;
		this.msgManager = msgManager;
	}	
	/**
	 * An "initAgreement" is sent from the tester process to start the a
	 * agreement process. This method can do whatever is appropriate for the 
	 * algorithm to start the agreement process. 
	 */
	public abstract void initAgreement();
	
	/**
	 * When this process receives a message, this method is called to handle it.  
	 */
	public abstract void handleMessage(Message m, int srcId, String tag);		
	
	/**
	 * Sends an application message to another process. 
	 */
	protected void sendMessage(int destId, String tag, String message){		
		msgManager.sendMessage(destId, tag, message);		
	}	
	
	/**
	 * Call this function once the process has decided on a value.
	 * 
	 * Sends a message to the tester program, letting the tester process know
	 * that a decision was reached. This message includes the decided value.
	 *  
	 * @param value the value decided 
	 */
	protected void decide(int value){ 
		msgManager.sendTesterMessage("decide", Integer.toString(value));
	}
	
	@Override
	public void _handleMessage(Message m, int srcId, String tag) {
		if(tag.equals("initAgreement")){
			initAgreement();
		}
		else {
			handleMessage(m, srcId, tag);
		}	
	}	
}
