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
package fr.xebia.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendHelloWorldJmsMessagePounder {

    private static Logger logger = LoggerFactory.getLogger(SendHelloWorldJmsMessagePounder.class);

    public static void main(String[] args) {
        try {
            stressTest();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void stressTest() throws InterruptedException, IOException {
        StressTestUtils.setlegend("-: success \r\n*: runtime exception \r\n#: time out exception");

        int injectorsCount = 5;

        final URL url = new URL(
                "http://localhost:8080/production-ready-application/jms/sendHelloWorldJmsMessage.jsp?use-managed-connection=true");

        ExecutorService executorService = Executors.newFixedThreadPool(injectorsCount);
        final Random random = new Random();

        for (int i = 0; i < injectorsCount; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10000; j++) {
                        try {
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                StressTestUtils.incrementProgressBar("-");
                            } else {
                                StressTestUtils.incrementProgressBar("#");
                            }
                        } catch (Exception e) {
                            boolean isTimeOutException = ExceptionUtils.indexOfThrowable(e, SocketTimeoutException.class) != -1;
                            if (isTimeOutException) {
                                StressTestUtils.incrementProgressBar("*");
                                logger.trace("SocketTimeoutException", e);
                            } else {
                                StressTestUtils.incrementProgressBar("#");
                                logger.debug("Exception", e);
                            }
                        }

                        try {
                            Thread.sleep(random.nextInt(1500));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            executorService.execute(runnable);
        }
        executorService.shutdown();
        executorService.awaitTermination(60, TimeUnit.MINUTES);
        System.out.println("bye");
    }
}
