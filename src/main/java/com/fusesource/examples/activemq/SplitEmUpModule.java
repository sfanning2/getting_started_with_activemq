package com.fusesource.examples.activemq;

public class SplitEmUpModule implements Runnable {

	private final String id;
	
	public SplitEmUpModule(String id) {
		this.id = id;
	}
	
	@Override
	public void run() {
		// Retrieve files from Data Client
		// Run SplitEmUpCode
		// Write results to Data Client
	}

}
