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
package org.ofbiz.service.test;

import java.util.Map;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.testtools.OFBizTestCase;

public class ServiceEngineTests extends OFBizTestCase {

    public ServiceEngineTests(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public void testBasicJavaInvocation() throws Exception {
        Map<String, Object> result = dispatcher.runSync("testScv", UtilMisc.toMap("message", "Unit Test"));
        assertEquals("Service result success", ModelService.RESPOND_SUCCESS, result.get(ModelService.RESPONSE_MESSAGE));
    }
}
