/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.ofbiz.base.util.*;
import org.ofbiz.entity.util.EntityUtil;
import javolution.util.FastList;

communicationEvent = delegator.findOne("CommunicationEvent", [communicationEventId : parameters.communicationEventId], true);

if (!communicationEvent.note) return;
nameString = "Sent from: ";
int startEmail = communicationEvent.note.indexOf(nameString) + nameString.length();
int endEmail = communicationEvent.note.indexOf(";", startEmail);
context.emailAddress = communicationEvent.note.substring(startEmail, endEmail);

nameString = "Sent Name from: ";
int startName = communicationEvent.note.indexOf(nameString) + nameString.length();
int endName = communicationEvent.note.indexOf(";", startName);
name = communicationEvent.note.substring(startName, endName);
if (name) {
    counter = 0;
    lastBlank = 0;
    List names = FastList.newInstance();
    while ((nextBlank = name.indexOf(" ", lastBlank)) != -1) {
        names.add(name.substring(lastBlank, nextBlank));
        lastBlank = nextBlank + 1;
    }
    if (lastBlank > 0) {
        names.add(name.substring(lastBlank));
    }
    if (names && names.size() > 0) { //lastname
        context.lastName = names[names.size()-1];
        if (names.size() > 1) { // firstname
            context.firstName = names[0];
        }
        if (names.size() > 2) { // middle name(s)
            context.middleName = "";
            for (counter = 1; counter < names.size()-1; counter++) {
                context.middleName = context.middleName.concat(names[counter]).concat(" ")
            }
        }
    }  else {
        context.lastName = name;
    }
}
