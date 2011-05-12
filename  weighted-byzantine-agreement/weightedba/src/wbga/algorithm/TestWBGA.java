package wbga.algorithm;

/** 
 * Test/example implementation  
 */

public class TestWBGA extends AbstractWBGA {
	
	public TestWBGA(int myId, int numProcesses, MessageManager msgManager){
		super(myId, numProcesses, msgManager);
	}
	
	@Override
	public void initAgreement() {
		System.out.println("initAgreement");
		
		//Send 'ChooseOne" to all processes, including self
		for(int i = 0; i < numProcesses; i++) {			
			sendMessage(i, "ChooseOne", "empty message");			
		}	
	}

	@Override
	public void handleMessage(Message m, int src, String tag) {
		System.out.println("handleMessage: " + src + ", "+ tag + ", " + m.getMessage());
		
		//If receive "ChooseOne", decide on 1.
		if(tag.equals("ChooseOne")){
			//Uncomment the following line to simulate partial failure 
			//if(myId % 2 == 0)
			decide(1);
		}
	}

	

}
