package com.fusesource.examples.activemq;

import java.util.ArrayList;
import java.util.List;

public class HandwritingDetectionModule implements Runnable, Module{

	private final String id;
	
	private ArrayList<String> ids;
	private Status status;
	
	public HandwritingDetectionModule(String id) {
		this.id = id;
	}
	
	@Override
	public void run() {
		// Retrieve files from Data Client
		// Run HandwritingDetectionModule
		// Write results to Data Client
	}

	@Override
	public List<String> getCompletedIds() {
		return ids;
	}

	@Override
	public Status getStatus() {
		return status;
	}

}
