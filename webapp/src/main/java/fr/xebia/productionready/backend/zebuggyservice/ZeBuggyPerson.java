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

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * @author <a href="mailto:cyrille.leclerc@pobox.com">Cyrille Le Clerc</a>
 */
public class ZeBuggyPerson implements Serializable {
    
    public enum Gender {FEMALE, MALE};
    
    private static final long serialVersionUID = 1L;
    
    private String firstName;
    
    private Gender gender;
    
    private String lastName;
    
    public ZeBuggyPerson() {
        super();
    }
    
    public ZeBuggyPerson(String firstName, String lastName, Gender gender) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public Gender getGender() {
        return gender;
    }

    public String getLastName() {
        return lastName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("lastName", this.lastName).append("firstName", this.lastName).toString();
    }
}
