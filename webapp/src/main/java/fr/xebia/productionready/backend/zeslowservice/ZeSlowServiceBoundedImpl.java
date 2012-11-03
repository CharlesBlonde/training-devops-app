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
package fr.xebia.productionready.backend.zeslowservice;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(objectName = "fr.xebia:service=ZeSlowService,type=ZeSlowServiceBoundedImpl")
public class ZeSlowServiceBoundedImpl implements ZeSlowService {

    private final AtomicInteger invocationCount = new AtomicInteger();

    private int maxConcurrentInvocations = 100;

    private final AtomicInteger rejectedInvocationCount = new AtomicInteger();

    private Semaphore semaphore = new Semaphore(maxConcurrentInvocations);

    private long semaphoreAcquireTimeoutInMillis = 1000;

    private ZeSlowService zeSlowService;

    @Override
    public ZeSlowPerson find(long id) {

        invocationCount.incrementAndGet();
        try {
            // copy the semaphore instance reference into a local reference to
            // allow hot reconfiguration
            Semaphore activeSemaphore = this.semaphore;
            boolean acquired = activeSemaphore.tryAcquire(semaphoreAcquireTimeoutInMillis, TimeUnit.MILLISECONDS);
            if (acquired) {
                try {
                    return zeSlowService.find(id);
                } finally {
                    activeSemaphore.release();
                }
            } else {
                rejectedInvocationCount.incrementAndGet();
                throw new RuntimeException("Too many concurrent access to ZeSlowService");
            }
        } catch (InterruptedException e) {
            rejectedInvocationCount.incrementAndGet();
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted waiting to access to the ZeSlowService", e);
        }
    }

    @ManagedAttribute
    public int getAvailablePermits() {
        return this.semaphore.availablePermits();
    }

    @ManagedAttribute
    public int getInvocationCount() {
        return invocationCount.get();
    }

    @ManagedAttribute
    public int getMaxConcurrentInvocations() {
        return maxConcurrentInvocations;
    }

    @ManagedAttribute
    public int getRejectedInvocationCount() {
        return rejectedInvocationCount.get();
    }

    public long getSemaphoreAcquireTimeoutInMillis() {
        return semaphoreAcquireTimeoutInMillis;
    }

    public ZeSlowService getZeSlowService() {
        return zeSlowService;
    }

    @ManagedAttribute
    public void setMaxConcurrentInvocations(int maxConcurrentInvocations) {
        this.maxConcurrentInvocations = maxConcurrentInvocations;
        this.semaphore = new Semaphore(maxConcurrentInvocations);
    }

    public void setSemaphoreAcquireTimeoutInMillis(long semaphoreAcquireTimeoutInMillis) {
        this.semaphoreAcquireTimeoutInMillis = semaphoreAcquireTimeoutInMillis;
    }

    public void setZeSlowService(ZeSlowService zeSlowService) {
        this.zeSlowService = zeSlowService;
    }

}
