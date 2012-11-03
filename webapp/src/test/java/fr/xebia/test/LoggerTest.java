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

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class LoggerTest {

    @Ignore("don't saturate sysout")
    @Test
    public void testLog4j() throws Exception {

        try {
            callingMethod();
        } catch (RuntimeException e) {
            Logger.getLogger(LoggerTest.class).error("Logged by Log4j", e);
        }
    }
    
    @Ignore("don't saturate sysout")
    @Test
    public void testLogback() throws Exception {

        try {
            callingMethod();
        } catch (RuntimeException e) {
            LoggerFactory.getLogger(LoggerTest.class).error("Logged by Logback", e);
        }
    }

    public void nestedMethod() {
        throw new RuntimeException("Ze cause");
    }

    public void callingMethod() {
        try {
            nestedMethod();
        } catch (RuntimeException e) {
            throw new RuntimeException("Ze caller", e);
        }
    }
}
