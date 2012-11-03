/*
 * Copyright 2008-2012 Xebia and the original author or authors.
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
package fr.xebia.xml;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
public class JaxbJodaTimeAdapter {
    public static DateTime unmarshalDateTime(String dateTime) {
        if (dateTime == null)
            return null;
        return ISODateTimeFormat.dateTime().parseDateTime(dateTime);
    }

    public static String marshalDateTime(DateTime dateTime) {
        if (dateTime == null)
            return null;
        return ISODateTimeFormat.dateTime().print(dateTime);
    }

    public static LocalDate unmarshalLocalDate(String localDate) {
        if (localDate == null)
            return null;
        return ISODateTimeFormat.date().parseLocalDate(localDate);
    }

    public static String marshalLocalDate(LocalDate localDate) {
        if (localDate == null)
            return null;
        return ISODateTimeFormat.date().print(localDate);
    }

    public static LocalTime unmarshalLocalTime(String localTime) {
        if (localTime == null)
            return null;
        return ISODateTimeFormat.time().parseLocalTime(localTime);
    }

    public static String marshalLocalTime(LocalTime localTime) {
        if (localTime == null)
            return null;
        return ISODateTimeFormat.time().print(localTime);
    }
}
