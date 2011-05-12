package wbga.algorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/** 
 * Test/example implementation  
 */

public class QueenWBGA extends AbstractWBGA implements Runnable {
	
	public QueenWBGA(int myId, int numProcesses, MessageManager msgManager){
		super(myId, numProcesses, msgManager);
		queues = new ArrayList<LinkedList<Byte>>();
		for(int i = 0; i < numProcesses; ++i) {
			queues.add(new LinkedList<Byte>());
		}
	}
	
	private volatile byte V;
	private volatile double w[];
	private volatile int anchor;
	
	private volatile Random rand;
	private volatile ArrayList<LinkedList<Byte>> queues;
	
	@Override
	public void initAgreement() {
		System.out.println("initAgreement");
		if(myId == 0){
			//Everyone is symmetric in this algorithm,
			//so go ahead and init everything
			for(int i = 1; i < numProcesses; ++i) {
				sendMessage(i, "initAgreement", null);
			}
		}
		//TODO: seed on pid for psuedo-random, repeatable results?
		rand = new Random();
		
		
		V = (byte) rand.nextInt(2);
		w = new double[numProcesses];
		for(int i = 0; i < numProcesses; ++i) {
			w[i] = ((double) 1 / (double) numProcesses);
		}
		anchor = (numProcesses - 1) / 4;
		
		new Thread(this).start();		
		/*
		//Send 'ChooseOne" to all processes, including self
		for(int i = 0; i < numProcesses; i++) {			
			sendMessage(i, "ChooseOne", "empty message");			
		}
		*/	
	}

	@Override
	public void handleMessage(final Message m, final int src, final String tag) {
		System.out.println("handleMessage: " + src + ", "+ tag + ", " + m.getMessage());
		
		//new Thread(new Runnable(){
		//	@Override
		//	public void run() {
				if(tag.equals("V")) {
					synchronized(queues.get(src)) {
						queues.get(src).addLast(Byte.parseByte(m.getMessage().substring(1)));
					}
				} else if(tag.equals("QueenValue")) {
					synchronized(queues.get(src)) {
						queues.get(src).addLast(Byte.parseByte(m.getMessage().substring(1)));
					}
				} else {
					//shouldn't get here...
					throw new RuntimeException(
							"Unknown Tag Received: " + tag);
				}
		//	}
		//}).start();
		/*
		//If receive "ChooseOne", decide on 1.
		if(tag.equals("ChooseOne")){
			//Uncomment the following line to simulate partial failure 
			//if(myId % 2 == 0)
			decide(1);
		}
		*/
	}

	@Override
	public void run() {
		byte myvalue;
		double myweight;
		for(int q = 0; q < anchor; ++q) {
			double s0 = 0, s1 = 0;
			
			//First Phase:
			if(w[myId] > 0) {
				//send V to all processes
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "V", String.valueOf(V));			
				}
			}
			for(int i = 0; i < numProcesses; ++i) {
				if(w[i] > 0) {
					boolean gotValue = false;
					byte theValue = -1;
					while(!gotValue) {
						synchronized(queues.get(i)) {
							if(!queues.get(i).isEmpty()) {
								theValue = queues.get(i).removeFirst();
								gotValue = true;
							}
						}
					}
					if(theValue == 1) {
						s1 += w[i];
					} else if(theValue == 0) {
						s0 += w[i];
					} else {
						throw new RuntimeException(
								"Unknown Value Failure: " + theValue);
					}
				}
			}
			if(s1 > 0.5) {
				myvalue = 1;
				myweight = s1;
			} else {
				myvalue = 0;
				myweight = s0;
			}
			
			//Second Phase:
			//The Queen is process with (pid == q)
			if(q == myId) {
				//send myvalue to all processes
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "QueenValue", String.valueOf(myvalue));			
				}
			}
			boolean gotQueenValue = false;
			byte theQueenValue = -1;
			while(!gotQueenValue) {
				synchronized(queues.get(q)) {
					if(!queues.get(q).isEmpty()) {
						theQueenValue = queues.get(q).removeFirst();
						gotQueenValue = true;
					}
				}
			}
			if(myweight > .75) {
				V = myvalue;
			} else {
				V = theQueenValue;
			}
			
		}
		decide(V);
	}
}

