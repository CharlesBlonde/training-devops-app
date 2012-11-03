/*
 * Copyright 2002-2008 the original author or authors.
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
package fr.xebia.productionready.backend.zebuggyservice;

import java.net.SocketTimeoutException;
import java.util.Random;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import fr.xebia.management.statistics.Profiled;
import fr.xebia.productionready.backend.zebuggyservice.ZeBuggyPerson.Gender;

/**
 * @author <a href="mailto:cyrille.leclerc@pobox.com">Cyrille Le Clerc</a>
 */
@ManagedResource(objectName = "fr.xebia:service=ZeBuggyService,type=ZeBuggyServiceImpl")
public class ZeBuggyServiceImpl implements ZeBuggyService {

    private int exceptionRationInPercent = 5;

    private Random random = new Random();

    @Profiled
    @Override
    public ZeBuggyPerson find(long id) throws ZeBuggyServiceException, ZeBuggyServiceRuntimeException, RuntimeException {
        long sleepDuration = random.nextInt(300);
        try {
            Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        boolean isException;
        if (exceptionRationInPercent <= 0) {
            isException = false;
        } else {
            isException = (0 == random.nextInt(100 / exceptionRationInPercent));
        }

        ZeBuggyPerson zeBuggyPerson;
        if (isException) {
            switch (random.nextInt(7)) {
            case 0:
                throw new NullPointerException("buggy code happens");
            case 1:
                throw new RuntimeException(new SocketTimeoutException("Connect timed out"));
            case 2:
                throw new RuntimeException(new SocketTimeoutException("Receive timed out"));
            case 3:
                throw new ZeBuggyServiceRuntimeException(new SocketTimeoutException("Connect timed out"));
            case 4:
                throw new ZeBuggyServiceRuntimeException(new SocketTimeoutException("Receive timed out"));
            case 5:
                throw new ZeBuggyServiceException("Buggy id '" + id + "'");
            default:
                zeBuggyPerson = null;
            }
        } else {
            Gender gender = random.nextBoolean() ? Gender.FEMALE : Gender.MALE;
            zeBuggyPerson = new ZeBuggyPerson("John", "Buggy Doe-" + id, gender);

        }

        return zeBuggyPerson;
    }

    @ManagedAttribute(description="Exception ratio in percent")
    public int getExceptionRatioInPercent() {
        return exceptionRationInPercent;
    }

    @ManagedAttribute(description="Exception ratio in percent")
    public void setExceptionRatioInPercent(int percentage) {
        exceptionRationInPercent = percentage;
    }
}
