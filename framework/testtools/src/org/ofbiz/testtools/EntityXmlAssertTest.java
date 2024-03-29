/*******************************************************************************
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
 *******************************************************************************/
package org.ofbiz.testtools;

import junit.framework.TestResult;
import junit.framework.AssertionFailedError;

import org.w3c.dom.Element;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityDataAssert;
import org.ofbiz.entity.util.EntitySaxReader;
import org.ofbiz.service.testtools.OFBizTestCase;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.location.FlexibleLocation;

import javolution.util.FastList;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.net.URL;

public class EntityXmlAssertTest extends OFBizTestCase {

    public static final String module = ServiceTest.class.getName();

    protected String entityXmlUrlString;
    protected String action;

    /**
     * @param modelTestSuite
     */
    public EntityXmlAssertTest(String caseName, Element mainElement) {
        super(caseName);
        this.entityXmlUrlString = mainElement.getAttribute("entity-xml-url");
        this.action = mainElement.getAttribute("action");
        if (UtilValidate.isEmpty(this.action)) this.action = "assert";
    }

    public int countTestCases() {
        int testCaseCount = 0;
        try {
            URL entityXmlURL = FlexibleLocation.resolveLocation(entityXmlUrlString);
            List<GenericValue> checkValueList = delegator.readXmlDocument(entityXmlURL);
            testCaseCount = checkValueList.size();
        } catch (Exception e) {
            Debug.logError(e, "Error getting test case count", module);
        }
        return testCaseCount;
    }

    public void run(TestResult result) {
        result.startTest(this);

        try {
            URL entityXmlURL = FlexibleLocation.resolveLocation(entityXmlUrlString);
            List<Object> errorMessages = FastList.newInstance();

            if ("assert".equals(this.action)) {
                EntityDataAssert.assertData(entityXmlURL, delegator, errorMessages);
            } else if ("load".equals(this.action)) {
                EntitySaxReader reader = new EntitySaxReader(delegator);
                long numberRead = reader.parse(entityXmlURL);
            } else {
                // uh oh, bad value
                result.addFailure(this, new AssertionFailedError("Bad value [" + this.action + "] for action attribute of entity-xml element"));
            }

            if (UtilValidate.isNotEmpty(errorMessages)) {
                for (Object failureMessage: errorMessages) {
                    result.addFailure(this, new AssertionFailedError(failureMessage.toString()));
                }
            }
        } catch (Exception e) {
            result.addError(this, e);
        }

        result.endTest(this);
    }
}
