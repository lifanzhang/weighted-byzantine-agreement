package wbga.tester;

/**
 * A thread that outputs an InputStream, line by line.
 * 
 * Used for capturing the standard output of another process and displaying it
 * in this process' standard output. 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class StreamGobbler extends Thread {
	
	private InputStream is;
	private int pid; 
	
	public StreamGobbler(int pid, InputStream is) {
		this.pid = pid;
		this.is = is;	
	}
	
	public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                System.out.println("pid " + pid + "> " + line);    
        } catch (IOException ioe) {
            ioe.printStackTrace();  
        }
    }
}
