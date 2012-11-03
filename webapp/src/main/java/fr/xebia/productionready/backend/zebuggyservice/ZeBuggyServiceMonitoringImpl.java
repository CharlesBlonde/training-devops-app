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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import fr.xebia.productionready.backend.zeslowservice.ZeSlowServiceBoundedImpl;

/**
 * @author <a href="mailto:cyrille.leclerc@pobox.com">Cyrille Le Clerc</a>
 */
@ManagedResource(objectName = "fr.xebia:service=ZeBuggyService,type=ZeBuggyServiceStatistics")
public class ZeBuggyServiceMonitoringImpl implements ZeBuggyService {

    private final static Logger performanceLogger = LoggerFactory.getLogger("fr.xebia.performance."
            + ClassUtils.getShortName(ZeSlowServiceBoundedImpl.class));

    private long durationThresholdInNanos = TimeUnit.NANOSECONDS.convert(300, TimeUnit.MILLISECONDS);

    final private AtomicInteger invocationCount = new AtomicInteger();

    final private AtomicInteger numActive = new AtomicInteger();

    final private AtomicInteger otherRuntimeExceptionCount = new AtomicInteger();

    final private AtomicInteger overThresholdInvocationCount = new AtomicInteger();

    final private AtomicInteger timeoutExceptionCount = new AtomicInteger();

    final private AtomicLong totalDurationInNanos = new AtomicLong();

    private ZeBuggyService zeBuggyService;

    final private AtomicInteger zeBuggyServiceExceptionCount = new AtomicInteger();

    @Override
    public ZeBuggyPerson find(long id) throws ZeBuggyServiceException, ZeBuggyServiceRuntimeException, RuntimeException {

        String meaningFullParameters = "";
        String meaningFillSubDurations = "";

        long nanosBefore = System.nanoTime();
        numActive.incrementAndGet();
        try {
            return zeBuggyService.find(id);
        } catch (RuntimeException e) {
            if (ExceptionUtils.indexOfThrowable(e, SocketTimeoutException.class) != -1) {
                timeoutExceptionCount.incrementAndGet();
            } else {
                otherRuntimeExceptionCount.incrementAndGet();
            }
            throw e;
        } catch (ZeBuggyServiceException e) {
            if (ExceptionUtils.indexOfThrowable(e, SocketTimeoutException.class) != -1) {
                timeoutExceptionCount.incrementAndGet();
            } else {
                zeBuggyServiceExceptionCount.incrementAndGet();
            }
            throw e;
        } finally {
            numActive.decrementAndGet();
            invocationCount.incrementAndGet();

            long durationInNanos = System.nanoTime() - nanosBefore;
            totalDurationInNanos.addAndGet(durationInNanos);
            if (durationInNanos >= durationThresholdInNanos) {
                overThresholdInvocationCount.incrementAndGet();

                performanceLogger.info("doJob(" + meaningFullParameters + ") " + meaningFillSubDurations + " took "
                        + TimeUnit.MILLISECONDS.convert(durationInNanos, TimeUnit.NANOSECONDS) + " ms");
            }
        }
    }

    @ManagedAttribute(description = "Number of active invocations to Ze Buggy Service")
    public int getCurrentActive() {
        return numActive.get();
    }

    @ManagedAttribute
    public long getDurationThresholdInNanos() {
        return durationThresholdInNanos;
    }

    @ManagedAttribute(description = "Number of invocations (trendsup)")
    public int getInvocationCount() {
        return invocationCount.get();
    }

    @ManagedAttribute(description = "Number of invocations that raised unclassified runtime exceptions (trendsup)")
    public int getOtherRuntimeExceptionCount() {
        return otherRuntimeExceptionCount.get();
    }

    @ManagedAttribute
    public int getOverThresholdInvocationCount() {
        return overThresholdInvocationCount.get();
    }

    @ManagedAttribute(description = "Number of invocations that raised timeout exceptions (trendsup)")
    public int getTimeoutExceptionCount() {
        return timeoutExceptionCount.get();
    }

    /**
     * Duration in millis, not very fast to obtain as it does require a unit
     * conversion (nanos to millis) but human readable.
     * 
     * @see #getTotalDurationInNanos()
     */
    @ManagedAttribute(description = "Total duration of the invocations in nanos (trendsup)")
    public long getTotalDurationInMillis() {
        return TimeUnit.MILLISECONDS.convert(totalDurationInNanos.get(), TimeUnit.NANOSECONDS);
    }

    /**
     * Duration in nanos, very fast to obtain as it does not require unit
     * conversion but not human readable.
     * 
     * @see #getTotalDurationInMillis()
     */
    @ManagedAttribute(description = "Total duration of the invocations in nanos (trendsup)")
    public long getTotalDurationInNanos() {
        return totalDurationInNanos.get();
    }

    @ManagedAttribute(description = "Number of invocations that raised unclassified ZeBuggyService exceptions (trendsup)")
    public int getZeBuggyServiceExceptionCount() {
        return zeBuggyServiceExceptionCount.get();
    }

    public void setDurationThresholdInNanos(long durationThresholdInNanos) {
        this.durationThresholdInNanos = durationThresholdInNanos;
    }

    public void setZeBuggyService(ZeBuggyService zeBuggyService) {
        this.zeBuggyService = zeBuggyService;
    }

}
