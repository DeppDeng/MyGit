package org;

public class FixedCPUUsage {

	
	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		int busyTime = 1;
		
		int idleTime = busyTime;
		
		long startTime = 0;
		
		while(true){
			startTime = System.currentTimeMillis();
			
			while(System.currentTimeMillis() - startTime <= 1){
				
			}
			
			Thread.sleep(idleTime);
		}
	}

}
