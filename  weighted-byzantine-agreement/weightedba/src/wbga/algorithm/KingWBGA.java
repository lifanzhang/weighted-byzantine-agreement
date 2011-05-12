package wbga.algorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class KingWBGA extends AbstractWBGA implements Runnable{
	
	private final byte UNDECIDED = -1;
	
	private volatile byte V;
	private volatile double w[];
	private volatile int anchor;
	
	private volatile Random rand;
	private volatile ArrayList<LinkedList<Byte>> queues;

	public KingWBGA(int myId, int numProcesses, MessageManager msgManager) {
		super(myId, numProcesses, msgManager);
		queues = new ArrayList<LinkedList<Byte>>();
		for(int i = 0; i < numProcesses; ++i) {
			queues.add(new LinkedList<Byte>());
		}
	}

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

	}

	@Override
	public void handleMessage(final Message m, final int src, final String tag) {
		System.out.println("handleMessage: " + src + ", "+ tag + ", " + m.getMessage());
		
		//new Thread(new Runnable(){
		//	@Override
        //    public void run() {
                if(tag.equals("V")) {
                    synchronized(queues.get(src)) {
                            queues.get(src).addLast(Byte.parseByte(m.getMessage().substring(1)));
                    }
                } else if(tag.equals("kingValue")) {
                    synchronized(queues.get(src)) {
                            queues.get(src).addLast(Byte.parseByte(m.getMessage().substring(1)));
                    }
                } else {
                    //shouldn't get here...
                    throw new RuntimeException(
                                    "Unknown Tag Received: " + tag);
                }
        //    }
		//}).start();
	}
	
	@Override 
	public void run(){ 		
		for(int k = 0; k < anchor; k++) {
			double s0 = 0.0, s1 = 0.0, su = 0.0;
			double myWeight = 0.0;
			
			//First phase			
			if(w[myId] > 0) {
				//Send V to all processes
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "V", Integer.toString(V));
				}
			}			
			//get V from all other processes
			for(int j = 0; j < numProcesses; j++) {
				if(w[j] > 0) {
					boolean gotValue = false;
					byte theValue = -1;					
					while(!gotValue) {
						synchronized (queues.get(j)) {
							if(!queues.get(j).isEmpty()) {
								theValue = queues.get(j).removeFirst();
								gotValue = true;
							}							
						}
					}
					if(theValue == 1) {
						s1 += w[j];
					}
					else if(theValue == 0) {
						s0 += w[j];
					} 
					else if(theValue == UNDECIDED) {
						//do nothing
					}
					else {
                        throw new RuntimeException(
                                "Unknown Value Failure: " + theValue);
					}					
				}
			}
			//Update value
			if(s0 >= (2.0/3.0)) {
				V = 0;
			}
			else if(s1 >= (2.0/3.0)){
				V = 1;
			}
			else{
				V = UNDECIDED;
			}
			
			//Second phase			
			s0 = s1 = su = 0.0;
			if(w[myId] > 0) {
				//Send V to all processes
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "V", Integer.toString(V));
				}
			}		
			//get V from all other processes
			for(int j = 0; j < numProcesses; j++) {
				if(w[j] > 0) {
					boolean gotValue = false;
					byte theValue = -1;					
					while(!gotValue) {
						synchronized (queues.get(j)) {
							if(!queues.get(j).isEmpty()) {
								theValue = queues.get(j).removeFirst();
								gotValue = true;
							}							
						}
					}
					if(theValue == 1) {
						s1 += w[j];
					}
					else if(theValue == 0) {
						s0 += w[j];
					} 
					else if(theValue == UNDECIDED) {
						su += w[j];
					}
					else {
                        throw new RuntimeException(
                                "Unknown Value Failure: " + theValue);
					}					
				}
			}
			//Update value
			if(s0 >= (1.0/3.0)) {
				V = 0;
				myWeight = s0;
			}
			else if(s1 >= (1.0/3.0)){
				V = 1;
				myWeight = s1;
			}
			else if(su >= (1.0/3.0)){
				V = UNDECIDED;
				myWeight = su;
			}
			
			//Third phase		
			//If king, send value to all other processes
			if(k == myId) {
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "kingValue", Integer.toString(V));
				}
			}
			//Get king value from king
			boolean gotKingValue = false;
            byte theKingValue = -1;
            while(!gotKingValue) {
                synchronized(queues.get(k)) {
                    if(!queues.get(k).isEmpty()) {
                            theKingValue = queues.get(k).removeFirst();
                            gotKingValue = true;
                    }
                }
            }
            if ( (V == UNDECIDED) || (myWeight < (2.0/3.0))) {
            	if(theKingValue == UNDECIDED) {
            		V = 1;
            	}
            	else {
            		V = theKingValue;
            	}
            }		
		}
		decide(V);
	}	

}
