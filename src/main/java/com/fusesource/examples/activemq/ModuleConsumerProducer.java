/*
 * Copyright (C) Red Hat, Inc.
 * http://www.redhat.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fusesource.examples.activemq;

import javax.jms.*;
import javax.jms.IllegalStateException;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ModuleConsumerProducer implements Runnable{
    private static final Log LOG = LogFactory.getLog(ModuleConsumerProducer.class);

    private static final Boolean NON_TRANSACTED = false;
    private static final String CONNECTION_FACTORY_NAME = "myJmsFactory";
    
    // Consumer variables
    private  final String consumerDestinationName;
    private final Class<?> moduleClass;
    private static final int MESSAGE_TIMEOUT_MILLISECONDS = 120000;
    
    // Producer variables
    private final String producerDestinationName;


    public static void main(String args[]) throws IllegalStateException, ClassNotFoundException {
    	// Process command line arguments:
    	// arg1 Consumer Destination
    	// arg2 Producer Destination
    	// arg3 Module
    	
    	if (args.length < 3 || args[0] == null || args[1] == null || args[2] == null) {
    		throw new IllegalStateException("Supply Consumer Destination, Producer Destination, and Module.");
    	}
    	
    	new ModuleConsumerProducer(args[0],args[1],Class.forName(args[2])).run();
    }
    
    /**
     * 
     * @param consumerDestinationName Example: "queue/newDocument"
     * @param producerDestinationName Example: "queue/SplitEmUpComplete"
     * @param moduleClass	Example: com.mutulofomaha.samitization.service.split.em.up.module.SplitEmUpModule
     */
    public ModuleConsumerProducer(String consumerDestinationName, String producerDestinationName, Class<?> moduleClass) {
    	this.consumerDestinationName = consumerDestinationName;
    	this.producerDestinationName = producerDestinationName;
    	this.moduleClass = moduleClass;
    }

    public void run() {
        Connection connection = null;
        Session session = null;
        MessageConsumer consumer = null;
        MessageProducer producer = null;

        try {
            // JNDI lookup of JMS Connection Factory and JMS Destination
            Context context = new InitialContext();
            ConnectionFactory factory = (ConnectionFactory) context.lookup(CONNECTION_FACTORY_NAME);


            connection = factory.createConnection();
            connection.start();

            session = connection.createSession(NON_TRANSACTED, Session.AUTO_ACKNOWLEDGE);
            
        	// Set up Consumer
            Destination consumerDestination = (Destination) context.lookup(consumerDestinationName);
            consumer = session.createConsumer(consumerDestination);

            LOG.info("Start consuming messages from " + consumerDestination.toString() + " with " + MESSAGE_TIMEOUT_MILLISECONDS + "ms timeout");
            
            // Set up Producer
            Destination producerDestination = (Destination) context.lookup(producerDestinationName);

            producer = session.createProducer(producerDestination);

            // Synchronous message consumer
            int i = 1;
            while (true) {
                Message consumerMessage = consumer.receive(MESSAGE_TIMEOUT_MILLISECONDS);
                if (consumerMessage != null) {
                    if (consumerMessage instanceof TextMessage) {
                        String text = ((TextMessage) consumerMessage).getText();
                        LOG.info("Got " + (i++) + ". message: " + text);
                        // Run Split Em Up Module
                        (new SplitEmUpModule("id_placeholder")).run();
                        // On completion, produce Split Em Up Complete Message
                        TextMessage producerMessage = session.createTextMessage(i + ". message sent from " + SplitEmUpProducer.class.getName());
                        LOG.info("Sending to destination: " + producerDestination.toString() + " this text: '" + producerMessage.getText());
                        producer.send(producerMessage);
                    }
                } else {
                    break;
                }
            }

            consumer.close();
            session.close();
        } catch (Throwable t) {
            LOG.error(t);
        } finally {
            // Cleanup code
            // In general, you should always close producers, consumers,
            // sessions, and connections in reverse order of creation.
            // For this simple example, a JMS connection.close will
            // clean up all other resources.
            if (producer != null) {
                try {
                    producer.close();
                } catch (JMSException e) {
                    LOG.error(e);
                }
            }
            if (consumer != null) {
                try {
                    consumer.close();
                } catch (JMSException e) {
                    LOG.error(e);
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    LOG.error(e);
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    LOG.error(e);
                }
            }
        }
    }
    
}