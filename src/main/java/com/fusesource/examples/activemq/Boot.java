package com.fusesource.examples.activemq;

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
		for(int i = 0; i < 2; i++) {
			new SimpleProducer();
		}
		for(int i = 0; i < 2; i++) {
			Runnable consumer = new ModuleConsumerProducer("queue/simple","queue/testout", SplitEmUpModule.class);
			Future<?> f = executorService.submit(consumer);
		}
		for(int i = 0; i < 2; i++) {
			Runnable consumer = new ModuleConsumerProducer("queue/testout","queue/simple", SplitEmUpModule.class);
			Future<?> f = executorService.submit(consumer);
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
