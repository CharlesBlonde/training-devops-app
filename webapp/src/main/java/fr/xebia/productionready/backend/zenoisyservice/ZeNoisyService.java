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
package fr.xebia.productionready.backend.zenoisyservice;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.ClassUtils;

import fr.xebia.management.statistics.Profiled;

/**
 * A service that emits a lot of log messages (debugging, audit and perf
 * messages).
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 */
@ManagedResource(objectName = "fr.xebia:service=ZeNoisyService,type=ZeNoisyServiceImpl")
public class ZeNoisyService {

    private final Logger auditLogger = LoggerFactory.getLogger("fr.xebia.audit." + ClassUtils.getShortName(ZeNoisyService.class));

    private long durationWarningThresholdInNanos = TimeUnit.NANOSECONDS.convert(200, TimeUnit.MILLISECONDS);

    private final Logger logger = LoggerFactory.getLogger(ZeNoisyService.class);

    private final Logger performanceLogger = LoggerFactory.getLogger("fr.xebia.performances."
            + ClassUtils.getShortName(ZeNoisyService.class));

    private final Random random = new Random();

    private ZeNoisySubService zeNoisySubService = new ZeNoisySubService();

    @Profiled
    public String doNoisyJob(long id) {
        logger.debug("entering doNoidyJob({})", id);

        // INVOKE POTENTIALLY LONG WORK
        long nanoTimeBefore = System.nanoTime();
        try {
            zeNoisySubService.doPotentiallySlowWork(id);
        } finally {
            // LOG INVOCATION DURATION
            long durationInNanos = System.nanoTime() - nanoTimeBefore;
            if (durationInNanos > durationWarningThresholdInNanos) {
                performanceLogger.warn("ZeNoisyService.doNoisyJob({}) took {} ms ! add query details to help diagnostic", id,
                        TimeUnit.MILLISECONDS.convert(durationInNanos, TimeUnit.NANOSECONDS));
            } else {
                performanceLogger.info("ZeNoisyService.doNoisyJob({}) took {} ms", id, TimeUnit.MILLISECONDS.convert(durationInNanos,
                        TimeUnit.NANOSECONDS));
            }
        }

        // AUDIT 
        String magicStringToAudit = id + "-" + Long.toHexString(random.nextLong());
        auditLogger.info("ZeNoisyService.doNoisyJob({}) -> {}", id, magicStringToAudit);

        String result = "noisy response " + magicStringToAudit;
        logger.debug("exiting ZeNoisyService.doNoisyJob({}) : {}", id, result);
        return result;
    }

    @ManagedAttribute
    public long getDurationWarningThresholdInMillis() {
        return TimeUnit.MILLISECONDS.convert(durationWarningThresholdInNanos, TimeUnit.NANOSECONDS);
    }

    @ManagedAttribute
    public void setDurationWarningThresholdInMillis(long durationInMillis) {
        this.durationWarningThresholdInNanos = TimeUnit.NANOSECONDS.convert(durationInMillis, TimeUnit.MILLISECONDS);
    }
}
