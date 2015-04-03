package com.fusesource.examples.activemq;

import java.util.List;

public interface Module {
	enum Status {
		NOT_STARTED(0),
		PROCESSING(1),
		COMPLETED(2),
		FAILED(3);
		
		private final int status;
		 
        Status(int aStatus){
            this.status = aStatus;
        }
        public int status(){
            return this.status;
        }
	}
	/**
	 * Get the list of Ids processed by the Module
	 * If the status of the Module is Completed, this will
	 * return the Ids processed or an empty array. If the Module
	 * is not completed, results are not guaranteed. 
	 * @return
	 */
	public List<String> getCompletedIds();
	
	/**
	 * The current status of the Module
	 * The underlying field should be volatile
	 * @return
	 */
	public Status getStatus();
}
