package wbga.tester;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WBGATester {
	
	//The number of processes to launch
	private static final int NUM_PROCESSES = 50;
	//The project's bin directory
	private static String CURRENT_DIR = "C:\\Users\\Justin\\Projects\\Android_workspace\\WBGA\\bin";
	
	//The port for the tester program
	private static int PORT = 7999;	
	
	//The maximum time to wait (in seconds) for agreement before giving up
	private static int MAX_WAIT = 30;
	
	static StopWatch stopWatch = new StopWatch();
	
	public static void main(String[] args) throws Exception {		
		Process processes[] = new Process[NUM_PROCESSES];
		
		ConnectionManager connManager = new ConnectionManager(PORT, NUM_PROCESSES);
		
		//Launch processes					
		System.out.println("Launching " + NUM_PROCESSES + " WBGA processes.");
		for(int i = 0; i < NUM_PROCESSES; i++) {
			//launch the process
			ProcessBuilder pb = new ProcessBuilder("java", "wbga/algorithm/WBGAInstance", 
					Integer.toString(i), Integer.toString(NUM_PROCESSES), Integer.toString(PORT));
			pb.directory(new File(CURRENT_DIR));
			pb.redirectErrorStream(true);
			processes[i] = pb.start();				
			
			//capture the output stream of the process
			StreamGobbler sg = new StreamGobbler(i, processes[i].getInputStream());
			sg.start();			
		}
		System.out.println("Finished launching " + NUM_PROCESSES + " WBGA processes.");
		
		//Wait for "ready" messages from all launched processes
		//Wait for MAX_WAIT, then fail
		System.out.println("Waiting for 'ready' message from all launched proceseses");
		int numReadysReceived = 0;
		stopWatch.start();
		while(numReadysReceived < NUM_PROCESSES && (stopWatch.getElapsedTimeSecs() < MAX_WAIT)) {
			TesterMessage msg = connManager.getNextMessage();
			if(msg != null) {
				if(msg.getTag().equals("ready")) {
					numReadysReceived++;
				}
			}
		}
		
		//Check if all processes sent 'ready' message
		if(numReadysReceived == NUM_PROCESSES) {
			System.out.println("Received 'ready' from all processes.");
		}
		else {
			System.out.println("Only received 'ready' from " + numReadysReceived + " processes.");
			System.out.println("Exiting.");
			killProcessesAndExit(processes);
		}		
		
		//Start the agreement process		
		stopWatch.start();
		connManager.sendMessage(0, "initAgreement", null);
		
		//Wait for all processes to decide, time out after MAX_WAIT
		System.out.println("Waiting for 'decide' message from all launched proceseses");
		int numDecidesReceived = 0;
		ArrayList<Integer> decisions= new ArrayList<Integer>();
		while((numDecidesReceived < NUM_PROCESSES) && (stopWatch.getElapsedTimeSecs() < MAX_WAIT) ) {
			TesterMessage msg = connManager.getNextMessage();
			if(msg != null) {
				if(msg.getTag().equals("decide")) {
					numDecidesReceived++;
					decisions.add(Integer.parseInt(msg.getMessage().substring(1)));
				}
			}
		}
		stopWatch.stop();
		
		//Display results				
		if (numDecidesReceived == NUM_PROCESSES) {
			System.out.println("Received 'decide' from all processes.");
			System.out.println("Agreement took " + stopWatch.getElapsedTime() + " ms");
		}
		else {
			System.out.println("Agreement failed. Only received " + numDecidesReceived + "'decide' messages.");			
		}
		
		//Verify all processes made same decision
		if(decisions.size() > 0) {
			int first = decisions.get(0);
			boolean match = true;
			for(int i = 1; i < decisions.size(); i++) {
				if(decisions.get(i) != first) {
					match = false;
				}
			}
			if(match) {
				System.out.println("All processes decided the same value: " + first);				
			}
			else {
				System.out.print("The processes did not all decide the same value:");
				System.out.println(decisions);
			}
		}
		
		killProcessesAndExit(processes);
	}
	
	/**
	 * Kills the launched processes and exits. 
	 */
	private static void killProcessesAndExit(Process[] processes) {
		//kill processes
		System.out.println("Killing all WBGA proceses.");
		for(int i = 0; i < NUM_PROCESSES; i++) {
			if(processes[i] != null )
				processes[i].destroy();
		}	
		System.out.println("Finished killing all WBGA proceses.");
		
		System.exit(0);	
	}

}
