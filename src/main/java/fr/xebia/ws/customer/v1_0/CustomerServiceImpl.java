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
package fr.xebia.ws.customer.v1_0;

import java.util.Random;

import javax.annotation.security.RolesAllowed;
import javax.xml.ws.Holder;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

import fr.xebia.audit.Audited;
import fr.xebia.productionready.backend.zebuggyservice.ZeBuggyPerson;
import fr.xebia.productionready.backend.zebuggyservice.ZeBuggyService;
import fr.xebia.productionready.backend.zebuggyservice.ZeBuggyServiceException;
import fr.xebia.productionready.backend.zebuggyservice.ZeBuggyServiceRuntimeException;
import fr.xebia.productionready.backend.zenoisyservice.ZeNoisyService;
import fr.xebia.productionready.backend.zeslowservice.ZeSlowPerson;
import fr.xebia.productionready.backend.zeslowservice.ZeSlowService;
import fr.xebia.productionready.service.ZeJmsService;
import fr.xebia.productionready.service.ZeVerySlowAggregatingService;

@ManagedResource(objectName = "fr.xebia:service=CustomerService,type=CustomerServiceImpl")
public class CustomerServiceImpl implements CustomerService {

    private Cache customerCache;

    private int exceptionRatioInPercent = 5;

    private final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final Random random = new Random();

    private ZeBuggyService zeBuggyService;

    private ZeJmsService zeJmsService;

    private ZeNoisyService zeNoisyService;

    private ZeSlowService zeSlowService;

    private ZeVerySlowAggregatingService zeVerySlowAggregatingService;

    private void doSomeWork(long id, Customer customer) throws CustomerNotFoundException {
        // RANDOMLY THROW EXCEPTION
        boolean isException;
        if (exceptionRatioInPercent <= 0) {
            isException = false;
        } else {
            isException = (0 == random.nextInt(100 / exceptionRatioInPercent));
        }

        if (isException) {
            switch (random.nextInt(2)) {
            case 0:
                throw new RuntimeException("Something went wrong");
            default:
                CustomerNotFoundFault cnff = new CustomerNotFoundFault();
                String message = "Customer " + id + " not found";
                cnff.setMessage(message);
                throw new CustomerNotFoundException(message, cnff);
            }
        }
    }

    @RolesAllowed("ROLE_USER")
    @Audited(message = "CustomerService.getCustomer(#{args[0]})")
    @Override
    public Customer getCustomer(long id) throws CustomerNotFoundException {

        // LOOKUP IN CACHE
        Element customerElement = customerCache.get(id);
        if (customerElement != null) {
            logger.debug("cache hit for {}", id);
            return (Customer) customerElement.getObjectValue();
        }

        // BUILD CUSTOMER
        Customer customer = new Customer();
        try {
            customer.setId(id);
            customer.setFirstName("first-name-" + id);
            customer.setLastName("last-name-" + id);

            doSomeWork(id, customer);

            updateCustomerWithZeBuggyServiceData(id, customer);

            updateCustomerWithZeSlowServiceData(id, customer);
        } catch (RuntimeException e) {
            throw new RuntimeException("Exception getting customer '" + id + "'", e);
        }
        // PUT IN CACHE
        customerCache.put(new Element(id, customer));

        return customer;
    }

    @ManagedAttribute(description = "Exception ratio in percent")
    public int getExceptionRatioInPercent() {
        return exceptionRatioInPercent;
    }

    @RolesAllowed("ROLE_USER")
    @Audited(message = "CustomerService.getCustomer(#{args[0].value.id})")
    @Override
    public void saveCustomer(Holder<Customer> customer) {
        Assert.notNull(customer.value, "customer can not be null");
        logger.debug("save " + customer.value);

        // PUT IN CACHE
        customerCache.put(new Element(customer.value.getId(), customer.value));
    }

    public void setCustomerCache(Cache customerCache) {
        this.customerCache = customerCache;
    }

    @ManagedAttribute(description = "Exception ratio in percent")
    public void setExceptionRatioInPercent(int percentage) {
        exceptionRatioInPercent = percentage;
    }

    public void setZeBuggyService(ZeBuggyService zeBuggyService) {
        this.zeBuggyService = zeBuggyService;
    }

    public void setZeJmsService(ZeJmsService zeJmsService) {
        this.zeJmsService = zeJmsService;
    }

    public void setZeNoisyService(ZeNoisyService zeNoisyService) {
        this.zeNoisyService = zeNoisyService;
    }

    public void setZeSlowService(ZeSlowService zeSlowService) {
        this.zeSlowService = zeSlowService;
    }

    public void setZeVerySlowAggregatingService(ZeVerySlowAggregatingService zeVerySlowAggregatingService) {
        this.zeVerySlowAggregatingService = zeVerySlowAggregatingService;
    }

    protected void updateCustomerWithZeBuggyServiceData(long id, Customer customer) {
        try {
            ZeBuggyPerson zeBuggyPerson = zeBuggyService.find(id);
            Gender gender;
            if (zeBuggyPerson == null) {
                gender = null;
            } else {
                if (ZeBuggyPerson.Gender.FEMALE == zeBuggyPerson.getGender()) {
                    gender = Gender.FEMALE;
                } else if (ZeBuggyPerson.Gender.MALE == zeBuggyPerson.getGender()) {
                    gender = Gender.MALE;
                } else {
                    gender = null;
                }
            }
            customer.setGender(gender);
        } catch (RuntimeException e) {
            logger.warn("NON BLOCKING exception calling zeBuggyService with id: " + id, e);
        } catch (ZeBuggyServiceException e) {
            logger.warn("NON BLOCKING exception calling zeBuggyService with id: " + id, e);
        }
    }

    private void updateCustomerWithZeSlowServiceData(long id, Customer customer) {
        ZeSlowPerson zeSlowPerson = zeSlowService.find(id);
        customer.setBirthdate(zeSlowPerson.getBirthDate());
    }

    @Override
    public String zeBuggyOperation(long id) {
        try {
            return ObjectUtils.toString(zeBuggyService.find(id), "#null#");
        } catch (ZeBuggyServiceRuntimeException e) {
            throw new RuntimeException(e);
        } catch (ZeBuggyServiceException e) {
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String zeJmsOperation(String in) {
        return zeJmsService.jmsRequestReply(in);
    }

    @Override
    public String zeNoisyOperation(long id) {
        return zeNoisyService.doNoisyJob(id);
    }

    @Override
    public String zeSlowOperation(long id) {
        return zeSlowService.find(id).toString();
    }

    @Override
    public String zeVerySlowAggregatingOperation(long id) {
        return zeVerySlowAggregatingService.doWork(id);
    }
}
