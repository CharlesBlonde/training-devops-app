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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZeNoisySubService {

    private final static Logger logger = LoggerFactory.getLogger(ZeNoisySubService.class);

    private final Random random = new Random();

    public void doPotentiallySlowWork(long id) {
        int workDurationInMillis = random.nextInt(500);
        try {
            Thread.sleep(workDurationInMillis);
        } catch (InterruptedException e) {
            logger.warn("Exception sleeping", e);
            Thread.interrupted();
        }

        if (random.nextInt(50) == 0) {
            logger.warn("An exception randomly occured", new RuntimeException("bad luck"));
        }

        logger.info("The key message troubleshooting teams need to track : {} took {} ms", id, workDurationInMillis);

        logger.debug("Get this noise out of my log file ! {}", id);
    }
}
