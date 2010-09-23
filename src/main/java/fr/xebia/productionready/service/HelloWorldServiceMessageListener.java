/*
 * Copyright 2008-2009 Xebia and the original author or authors.
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

import java.sql.Connection;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.util.Assert;

public class HelloWorldServiceMessageListener implements SessionAwareMessageListener<TextMessage>, InitializingBean {

    private DataSource dataSource;

    private ExecutorService executorService;

    private final Logger logger = LoggerFactory.getLogger(HelloWorldServiceMessageListener.class);

    private final Random random = new Random();

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.dataSource, "datasource can not be null");
        Assert.notNull(this.executorService, "executorService can not be null");
    }

    @Override
    public void onMessage(TextMessage message, Session session) throws JMSException {
        String response = "Hello " + message.getText();

        logger.warn("Reply '{}' to incoming message {}", response, message);

        Destination destination = message.getJMSReplyTo();
        if (destination != null) {
            MessageProducer messageProducer = session.createProducer(destination);
            try {
                TextMessage responseMessage = session.createTextMessage(response);
                String correlationId;
                if(message.getJMSCorrelationID() == null) {
                    correlationId = message.getJMSMessageID();
                } else {
                    correlationId = message.getJMSCorrelationID();
                }
                responseMessage.setJMSCorrelationID(correlationId);
                messageProducer.send(responseMessage);
            } finally {
                messageProducer.close();
            }
        }

        executorService.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    Connection cnn = dataSource.getConnection();
                    try {
                        Thread.sleep(random.nextInt(1000));
                    } finally {
                        cnn.close();
                    }
                } catch (Exception e) {
                    logger.warn("Exception borrowing jdbc connction", e);
                }

            }
        });

    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

}
