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
package fr.xebia.productionready.service;

import javax.annotation.security.RolesAllowed;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;

import fr.xebia.audit.Audited;
import fr.xebia.management.statistics.Profiled;

/**
 * @author <a href="mailto:cyrille.leclerc@pobox.com">Cyrille Le Clerc</a>
 */
public class HelloWorldServiceImpl implements HelloWorldService {

    @Profiled
    @RolesAllowed( { "ROLE_USER" })
    @Audited(message = "sayHi(#{args[0]})")
    @Override
    public String sayHi(String text) throws HelloWorldServiceException {
        if (RuntimeException.class.getName().equals(text)) {
            throw new RuntimeException("the runtime exception");
        } else if (SOAPFaultException.class.getName().equals(text)) {
            // Raise a SOAP Faut specifying the faultCode
            try {
                SOAPFault soapFault = SOAPFactory.newInstance().createFault("This Exception Message",
                        QName.valueOf("http://www.xebia.fr/fault/666"));
                throw new SOAPFaultException(soapFault);
            } catch (SOAPException e) {
                e.printStackTrace();
            }
        } else if (Error.class.getName().equals(text)) {
            throw new Error("the error");
        } else if (HelloWorldServiceException.class.getName().equals(text)) {
            throw new HelloWorldServiceException("the hello world service exception");
        }
        return "Hi " + text;
    }
}