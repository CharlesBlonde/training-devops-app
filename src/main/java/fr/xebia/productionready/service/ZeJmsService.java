/*
 * Copyright 2008-2010 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.productionready.service;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;

import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.SessionCallback;

public class ZeJmsService {

    private JmsTemplate jmsTemplate = new JmsTemplate();

    /**
     * 
     * @param in
     * @return not <code>null</code>
     */
    public String jmsRequestReply(final String in) {

        String response = jmsTemplate.execute(new SessionCallback<String>() {
            @Override
            public String doInJms(Session session) throws JMSException {
                Queue queue = session.createQueue("hello-world-queue");
                TemporaryQueue replyToQueue = session.createTemporaryQueue();
                try {

                    // REQUEST
                    MessageProducer messageProducer = session.createProducer(queue);
                    String correlationId;
                    try {
                        Message requestMessage = session.createTextMessage("hello world " + in);
                        requestMessage.setJMSReplyTo(replyToQueue);
                        messageProducer.send(requestMessage);
                        correlationId = requestMessage.getJMSMessageID();
                    } finally {
                        messageProducer.close();
                    }

                    // RESPONSE
                    MessageConsumer messageConsumer = session.createConsumer(replyToQueue, "JMSCorrelationID = '" + correlationId + "'");
                    try {
                        Message responseMessage = messageConsumer.receive(1000);
                        return responseMessage == null ? "#null#" : responseMessage.toString();
                    } finally {
                        messageConsumer.close();
                    }
                } finally {
                    // don't close the temporary queue to prevent ActiveMQ "JMSException: A consumer is consuming from the temporary destination"
                    // replyToQueue.delete();
                }
            }
        }, true);

        return response;
    }

    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.jmsTemplate.setConnectionFactory(connectionFactory);
    }
}
