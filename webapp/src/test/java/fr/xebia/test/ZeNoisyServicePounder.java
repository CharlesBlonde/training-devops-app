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

import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.xebia.ws.customer.v1_0.CustomerService;

public class ZeNoisyServicePounder {
    
    private static Logger logger = LoggerFactory.getLogger(ZeNoisyServicePounder.class);
    
    public static void main(String[] args) {
        try {
            stressTest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void stressTest() throws InterruptedException {
        StressTestUtils.setlegend("-: success \r\n*: runtime exception \r\n#: time out exception");
        
        final CustomerService customerService = buildCustomerService();

        int injectorsCount = 35;

        ExecutorService executorService = Executors.newFixedThreadPool(injectorsCount);
        final Random random = new Random();

        for (int i = 0; i < injectorsCount; i++) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 10000; j++) {
                        try {
                            customerService.zeNoisyOperation(random.nextInt(100));
                            StressTestUtils.incrementProgressBar("-");
                        } catch (RuntimeException e) {
                            boolean isTimeOutException = ExceptionUtils.indexOfThrowable(e, SocketTimeoutException.class) != -1;
                            if (isTimeOutException) {
                                StressTestUtils.incrementProgressBar("*");
                                logger.trace("SocketTimeoutException", e);
                            } else {
                                StressTestUtils.incrementProgressBar("#");
                                logger.debug("RuntimeException", e);
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

    private static CustomerService buildCustomerService() {
        String username = "admin";
        String password = "admin";
        String url = "http://localhost:8080/production-ready-application/services/v1.0/customerService";

        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(CustomerService.class);
        factory.setAddress(url);

        CustomerService customerService = (CustomerService) factory.create();
        Client client = ClientProxy.getClient(customerService);

        HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
        HTTPClientPolicy httpClient = httpConduit.getClient();

        httpClient.setConnectionTimeout(100);
        httpClient.setReceiveTimeout(5000);

        AuthorizationPolicy authorization = httpConduit.getAuthorization();
        authorization.setUserName(username);
        authorization.setPassword(password);
        return customerService;
    }
}
