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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;

/**
 * Util class for Stress tests
 * 
 * @author <a href="mailto:cleclerc@pobox.com">Cyrille Le Clerc </a>
 */
public final class StressTestUtils {

    private static final int DASH_PER_LINE = 30;

    private static final StressTestUtils instance = new StressTestUtils();

    public static void incrementProgressBar(String symbol) {
        instance._incrementProgressBar(symbol);
    }

    public static void setlegend(String legend) {
        instance._setLegend(legend);
    }

    private long lastSampleTime;

    private String legend;

    private int lineCounter;

    private int progressBarCounter;

    /**
     * Private contructor for singleton class
     */
    private StressTestUtils() {
        super();
    }

    /**
     * <p>
     * Outputs a dash in SystemOut and line breaks when necessary
     * </p>
     */
    private synchronized void _incrementProgressBar(String symbol) {
        if (this.lastSampleTime == 0) {
            this.lastSampleTime = System.currentTimeMillis();
        }

        if (this.progressBarCounter % DASH_PER_LINE == 0) {
            // line break
            System.out.println();

            // print legend
            if (lineCounter % 10 == 0) {
                System.out.println(legend);
            }

            // print date and req/s
            long now = System.currentTimeMillis();
            long elapsedDuration = now - this.lastSampleTime;
            String throughput;
            if (elapsedDuration == 0) {
                throughput = "n/a";
            } else {
                long throughputAsInt = (DASH_PER_LINE * 1000) / elapsedDuration;
                throughput = String.valueOf(throughputAsInt);
            }
            System.out.print("[" + DateFormatUtils.format(System.currentTimeMillis(), "HH:mm:ss") + " "
                    + StringUtils.leftPad(throughput + "", 4) + " req/s]\t");

            lineCounter++;
            this.lastSampleTime = System.currentTimeMillis();
        }
        this.progressBarCounter++;
        System.out.print(symbol);
    }

    private void _setLegend(String legend) {
        this.legend = legend;
    }
}
