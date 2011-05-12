package wbga.algorithm;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

/** 
 * Test/example implementation  
 */

public class RandomWBGA extends AbstractWBGA implements Runnable {
	
	public RandomWBGA(int myId, int numProcesses, MessageManager msgManager){
		super(myId, numProcesses, msgManager);
		queues = new ArrayList<LinkedList<Byte>>();
		hasDecided = new boolean[numProcesses];
		decidedValue = new byte[numProcesses];
		tossValue = new byte[numProcesses];
		for(int i = 0; i < numProcesses; ++i) {
			queues.add(new LinkedList<Byte>());
			hasDecided[i] = false;
			decidedValue[i] = UNDECIDED;
			tossValue[i] = UNDECIDED;
		}
	}
	
	private final byte UNDECIDED = 2;
	
	private volatile byte V;
	private volatile double w[];
	private volatile int g;
	private volatile int t;
	
	private volatile Random rand;
	private volatile ArrayList<LinkedList<Byte>> queues;
	private volatile boolean hasDecided[];
	private volatile byte decidedValue[];
	private volatile byte tossValue[];
	
	private int group(int p) {
		return p / g;
	}
	
	private byte tossCoin() {
		return (byte) rand.nextInt(2);
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
		t = (numProcesses - 1) / 3;
		g = 1;
		
		new Thread(this).start();		
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
				} else if(tag.equals("V_toss")) {
					synchronized(queues.get(src)) {
						String values[] = m.getMessage().substring(1).split(" ");
						queues.get(src).addLast(Byte.parseByte(values[0]));
						queues.get(src).addLast(Byte.parseByte(values[1]));
					}
				} else if(tag.equals("last")) {
					String values[] = m.getMessage().substring(1).split(" ");
					decidedValue[src] = Byte.parseByte(values[0]);
					tossValue[src] = Byte.parseByte(values[1]);
					hasDecided[src] = true;
				} else {
					//shouldn't get here...
					throw new RuntimeException(
							"Unknown Tag Received: " + tag);
				}
		//	}
		//}).start();
	}

	@Override
	public void run() {
		byte ans;
		byte toss;
		double num;
		for(long e = 1; e < Long.MAX_VALUE ; ++e) {
			double s0 = 0, s1 = 0;
			//First Phase:
			//send V to all processes
			if(w[myId] > 0) {
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "V", String.valueOf(V | 16));
				}
			}
			for(int i = 0; i < numProcesses; ++i) {
				if(w[i] > 0) {
					boolean gotValue = false;
					byte theValue = -1;
					while(!gotValue) {
						if(hasDecided[i]) {
							theValue = decidedValue[i];
							gotValue = true;
						} else {
							synchronized(queues.get(i)) {
								if(!queues.get(i).isEmpty()) {
									theValue = queues.get(i).removeFirst();
									gotValue = true;
								}
							}
						}
					}
					theValue = (byte)(theValue & 0x0F);
					if(theValue == 1) {
						s1 += w[i];
					} else if(theValue == 0) {
						s0 += w[i];
					} else if(theValue == UNDECIDED) {
						throw new RuntimeException(
								"Got UNDECIDED; expected otherwise");
					} else {
						throw new RuntimeException(
								"Unknown Value Failure: " + theValue);
					}
				}
			}
			if(s1 >= ((double)(numProcesses - t) / (double)(numProcesses))) {
				V = 1;
			} else if(s0 >= ((double)(numProcesses - t) / (double)(numProcesses))) {
				V = 0;
			} else {
				V = UNDECIDED;
			}
			if(group(myId) == (e % (numProcesses / g))) {
				toss = tossCoin();
			} else {
				toss = 0;
			}
			
			s0 = 0; s1 = 0;
			double t0 = 0, t1 = 0;
			//Second Phase:
			//send V, toss to all processes
			if(w[myId] > 0) {
				for(int i = 0; i < numProcesses; i++) {
					sendMessage(i, "V_toss",
							String.valueOf(V) + " " + 
							String.valueOf(toss));			
				}
			}
			for(int i = 0; i < numProcesses; ++i) {
				if(w[i] > 0) {
					boolean gotValue = false;
					byte theValue = -1;
					byte theToss = -1;
					while(!gotValue) {
						if(hasDecided[i]) {
							theValue = decidedValue[i];
							theToss = tossValue[i];
							gotValue = true;
						} else {
							synchronized(queues.get(i)) {
								if(!queues.get(i).isEmpty()) {
									theValue = queues.get(i).removeFirst();
									theToss = queues.get(i).removeFirst();
									gotValue = true;
								}
							}
						}
					}
					theValue = (byte)(theValue & 0x0F);
					if(theValue == 1) {
						s1 += w[i];
					} else if(theValue == 0) {
						s0 += w[i];
					} else if(theValue == UNDECIDED) {
						//do nothing
					} else {
						throw new RuntimeException(
								"Unknown Value Failure: " + theValue);
					}
					if(group(i) == (e % (numProcesses / g))) {
						theToss = (byte)(theToss & 0x0F);
						if(theToss == 1) {
							t1 += w[i];
						} else if(theToss == 0) {
							t0 += w[i];
						} else if(theToss == UNDECIDED) {
							throw new RuntimeException(
									"Got UNDECIDED; expected otherwise");
						} else {
							throw new RuntimeException(
									"Unknown Toss Failure: " + theToss);
						}
					}
				}
			}
			if(s1 >= s0) {
				ans = 1;
				num = s1;
			} else {
				ans = 0;
				num = s0;
			}
			if(num >= ((double)(numProcesses - t) / (double)(numProcesses))) {
				if(w[myId] > 0) {
					for(int i = 0; i < numProcesses; i++) {
						sendMessage(i, "last",
								String.valueOf(V) + " " + 
								String.valueOf(toss));			
					}
				}
				V = ans;		
				decide(V);
				break;
			} else if(num >= ((double)(t + 1) / (double)(numProcesses))) {
				V = ans;
			} else {
				if(t1 >= t0) {
					V = 1;
				} else {
					V = 0;
				}
			}
		}
	}
}

