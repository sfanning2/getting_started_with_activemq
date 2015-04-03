package com.fusesource.examples.activemq;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

public class Boot {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Using method invocation
		// Create a InitContext instance
//		InitialContext context = new InitialContext();

		// Obtaining a default ManagedExecutorService
		// Using the java:comp/DefaultManagedExecutorService
//		ManagedExecutorService executorService = (ManagedExecutorService) context
//				.lookup("java:comp/DefaultManagedExecutorService");
		
		/* FIXME: temporary non jee thread pool */
		int  corePoolSize  =    5;
		int  maxPoolSize   =   10;
		long keepAliveTime = 5000;
		
		ExecutorService executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>()
                );
		
		// Configure Modules
		ArrayList<ModuleConfig> configs = new ArrayList<ModuleConfig>();
		// Split Em' Up Module
		configs.add(new ModuleConfig("queue/simple","queue/splitEmUpOutput", SplitEmUpModule.class));
		// OCR Module
		configs.add(new ModuleConfig("queue/splitEmUpOutput","queue/ocrOutput", OcrModule.class));
		// Line Detection Module
		configs.add(new ModuleConfig("queue/ocrOutput","queue/lineDetectionOutput", LineDetectionModule.class));
		// Handwriting Detection Module
		configs.add(new ModuleConfig("queue/lineDetectionOutput","queue/handwritingDetectionOutput", HandwritingDetectionModule.class));
		// Keyword Detection Module
		configs.add(new ModuleConfig("queue/handwritingDetectionOutput","queue/keywordDetectionOutput", KeywordDetectionModule.class));
		// Heuristic Module
		configs.add(new ModuleConfig("queue/keywordDetectionOutput","queue/heuristicOutput", HeuristicModule.class));
		// Typed Number Detection Module
		configs.add(new ModuleConfig("queue/heuristicOutput","queue/typedNumberDetectionOutput", TypedNumberDetectionModule.class));
		// Redaction Module
		configs.add(new ModuleConfig("queue/typedNumberDetectionOutput","queue/redactionOutput", RedactionModule.class));

		
//		for(int i = 0; i < 2; i++) {
//			new SimpleProducer();
//		}

		for(ModuleConfig config : configs) {
			
			Runnable consumer = new ModuleConsumerProducer(config.getInputQueue(),config.getOutputQueue(), config.getModuleClass());
			Future<?> f = executorService.submit(consumer);
		}

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	static class ModuleConfig {
		private final String inputQueue;
		private final String outputQueue;
		private final Class<?> moduleClass;
		
		ModuleConfig(String inputQueue, String outputQueue, Class<?> moduleClass) {
			this.inputQueue = inputQueue;
			this.outputQueue = outputQueue;
			this.moduleClass = moduleClass;
		}
		
		public String getInputQueue() {
			return inputQueue;
		}

		public String getOutputQueue() {
			return outputQueue;
		}

		public Class<?> getModuleClass() {
			return moduleClass;
		}
	}

}
