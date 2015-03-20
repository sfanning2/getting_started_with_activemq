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
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SplitEmUpConsumerProducer {
    private static final Log LOG = LogFactory.getLog(SplitEmUpConsumerProducer.class);

    private static final Boolean NON_TRANSACTED = false;
    private static final String CONNECTION_FACTORY_NAME = "myJmsFactory";
    
    // Consumer variables
    private static final String CONSUMER_DESTINATION_NAME = "queue/newDocument";
    private static final int MESSAGE_TIMEOUT_MILLISECONDS = 120000;
    
    // Producer variables
    private static final String PRODUCER_DESTINATION_NAME = "queue/splitEmUpComplete";


    public static void main(String args[]) {
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
            Destination consumerDestination = (Destination) context.lookup(CONSUMER_DESTINATION_NAME);
            consumer = session.createConsumer(consumerDestination);

            LOG.info("Start consuming messages from " + consumerDestination.toString() + " with " + MESSAGE_TIMEOUT_MILLISECONDS + "ms timeout");
            
            // Set up Producer
            Destination producerDestination = (Destination) context.lookup(PRODUCER_DESTINATION_NAME);

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