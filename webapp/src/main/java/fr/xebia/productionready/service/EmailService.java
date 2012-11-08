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
package fr.xebia.productionready.service;

import com.google.common.base.Strings;
import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 */
@ManagedResource(objectName = "fr.xebia:service=EmailService,type=EmailService")
@Controller
public class EmailService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Session mailSession;

    protected InternetAddress fromAddress;

    @Autowired
    public EmailService(@Qualifier("smtpProperties") Properties smtpProperties) {

        if (Strings.isNullOrEmpty(smtpProperties.getProperty("mail.username"))) {
            logger.info("Initialize anonymous mail session");
            mailSession = Session.getInstance(smtpProperties);
        } else {
            final String username = smtpProperties.getProperty("mail.username");
            final String password = smtpProperties.getProperty("mail.password");
            logger.info("Initialize mail session with username='{}', password='xxx'", username);

            mailSession = Session.getInstance(smtpProperties, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
        }

        try {
            fromAddress = new InternetAddress(smtpProperties.getProperty("mail.from"), smtpProperties.getProperty("mail.fromname"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Exception loading smtp property 'mail.from'='" + smtpProperties.getProperty("mail.from") + "'");
        }

    }

    @RequestMapping(value = "/email/send", method = RequestMethod.POST)
    public void sendEmail(
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("body") String body,
            Model model) throws MessagingException {

        Message msg = new MimeMessage(mailSession);

        msg.setFrom(fromAddress);
        msg.addRecipient(Message.RecipientType.TO, new InternetAddress(email));

        msg.setSubject(subject);
        msg.setText(body);
        logger.debug("Send to {} email {}", email, subject);
        Transport.send(msg);

        model.addAttribute("email", email);
        model.addAttribute("subject", subject);

    }

    @ManagedOperation
    public void resetTransport() {

    }

    @ManagedAttribute
    public String getFromAddress() {
        return fromAddress.toString();
    }

    @ManagedAttribute
    public void setFromAddress(String fromAddress) throws AddressException {
        this.fromAddress = new InternetAddress(fromAddress);
    }

}
