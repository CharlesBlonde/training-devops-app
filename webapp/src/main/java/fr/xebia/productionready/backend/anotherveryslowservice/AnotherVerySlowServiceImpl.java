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
package fr.xebia.productionready.backend.anotherveryslowservice;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import fr.xebia.management.statistics.Profiled;

@ManagedResource(objectName = "fr.xebia:service=AnotherVerySlowService,type=AnotherVerySlowServiceImpl")
public class AnotherVerySlowServiceImpl implements AnotherVerySlowService {

    private final Logger logger = LoggerFactory.getLogger(AnotherVerySlowServiceImpl.class);

    private Random random = new Random();

    private long slowInvocationMinDurationInMillis = 2000;

    private int slowInvocationsRatioInPercent = 5;

    private final AtomicLong totalDurationInNanos = new AtomicLong();

    private final AtomicInteger invocationCount = new AtomicInteger();

    @Profiled
    @Override
    public String find(long id) {

        long nanosBefore = System.nanoTime();
        try {
            long thinkTimeMillis = random.nextInt(500);

            boolean isSlowRequest;
            if (slowInvocationsRatioInPercent <= 0) {
                isSlowRequest = false;
            } else {
                isSlowRequest = (0 == random.nextInt(100 / slowInvocationsRatioInPercent));
            }

            if (isSlowRequest) {
                // add two seconds
                thinkTimeMillis += slowInvocationMinDurationInMillis;
            }
            try {
                Thread.sleep(thinkTimeMillis);
            } catch (InterruptedException e) {
                logger.warn("InterruptedException", e);
            }

            return "another-very-slow-response-" + id + "-" + thinkTimeMillis + "ms";
        } finally {
            totalDurationInNanos.addAndGet(System.nanoTime() - nanosBefore);
            invocationCount.incrementAndGet();
        }
    }

    @ManagedAttribute
    public long getTotalDurationInNanos() {
        return totalDurationInNanos.get();
    }

    /**
     * Human readable version of {@link #getTotalDurationInNanos()}
     */
    @ManagedAttribute
    public long getTotalDurationInMillis() {
        return TimeUnit.MILLISECONDS.convert(getTotalDurationInNanos(), TimeUnit.NANOSECONDS);
    }

    @ManagedAttribute
    public int getInvocationCount() {
        return invocationCount.get();
    }

    @ManagedAttribute
    public long getSlowInvocationMinDurationInMillis() {
        return slowInvocationMinDurationInMillis;
    }

    @ManagedAttribute
    public int getSlowInvocationsRatioInPercent() {
        return slowInvocationsRatioInPercent;
    }

    @ManagedAttribute
    public void setSlowInvocationMinDurationInMillis(long slowInvocationMinDurationInMillis) {
        this.slowInvocationMinDurationInMillis = slowInvocationMinDurationInMillis;
    }

    @ManagedAttribute
    public void setSlowInvocationsRatioInPercent(int slowInvocationsRatioInPercent) {
        this.slowInvocationsRatioInPercent = slowInvocationsRatioInPercent;
    }

}
