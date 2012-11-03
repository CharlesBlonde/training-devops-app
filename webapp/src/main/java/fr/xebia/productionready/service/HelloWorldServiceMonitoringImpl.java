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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.cxf.management.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource
public class HelloWorldServiceMonitoringImpl implements HelloWorldService {

    private final AtomicInteger invocationCount = new AtomicInteger();
    private final AtomicInteger activeInvocationCount = new AtomicInteger();
    private final AtomicLong durationInNanos = new AtomicLong();

    private HelloWorldService delegate;

    @Override
    public String sayHi(String text) throws HelloWorldServiceException {

        long nanoTimeBefrore = System.nanoTime();
        invocationCount.incrementAndGet();
        activeInvocationCount.incrementAndGet();
        try {
            return delegate.sayHi(text);
        } finally {
            activeInvocationCount.decrementAndGet();
            durationInNanos.addAndGet(System.nanoTime() - nanoTimeBefrore);
        }
    }

    @ManagedAttribute
    public AtomicInteger getInvocationCount() {
        return invocationCount;
    }

    @ManagedAttribute
    public AtomicInteger getActiveInvocationCount() {
        return activeInvocationCount;
    }

    @ManagedAttribute
    public AtomicLong getDurationInNanos() {
        return durationInNanos;
    }

    public HelloWorldService getDelegate() {
        return delegate;
    }

    public void setDelegate(HelloWorldService delegate) {
        this.delegate = delegate;
    }

}
