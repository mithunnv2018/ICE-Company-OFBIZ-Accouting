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
package org.ofbiz.service.mail;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.ofbiz.base.component.ComponentConfig;
import org.ofbiz.base.config.GenericConfigException;
import org.ofbiz.base.config.MainResourceHandler;
import org.ofbiz.base.config.ResourceHandler;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilXml;
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.service.config.ServiceConfigUtil;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.entity.GenericValue;

import org.w3c.dom.Element;

public class ServiceMcaUtil {

    public static final String module = ServiceMcaUtil.class.getName();
    public static UtilCache<String, ServiceMcaRule> mcaCache = new UtilCache<String, ServiceMcaRule>("service.ServiceMCAs", 0, 0, false);

    public static void reloadConfig() {
        mcaCache.clear();
        readConfig();
    }

    public static void readConfig() {
        Element rootElement = null;
        try {
            rootElement = ServiceConfigUtil.getXmlRootElement();
        } catch (GenericConfigException e) {
            Debug.logError(e, "Error getting Service Engine XML root element", module);
            return;
        }

        for (Element serviceMcasElement: UtilXml.childElementList(rootElement, "service-mcas")) {
            ResourceHandler handler = new MainResourceHandler(ServiceConfigUtil.SERVICE_ENGINE_XML_FILENAME, serviceMcasElement);
            addMcaDefinitions(handler);
        }

        // get all of the component resource eca stuff, ie specified in each ofbiz-component.xml file
        for (ComponentConfig.ServiceResourceInfo componentResourceInfo: ComponentConfig.getAllServiceResourceInfos("mca")) {
            addMcaDefinitions(componentResourceInfo.createResourceHandler());
        }
    }

    public static void addMcaDefinitions(ResourceHandler handler) {
        Element rootElement = null;
        try {
            rootElement = handler.getDocument().getDocumentElement();
        } catch (GenericConfigException e) {
            Debug.logError(e, module);
            return;
        }

        int numDefs = 0;
        for (Element e: UtilXml.childElementList(rootElement, "mca")) {
            String ruleName = e.getAttribute("mail-rule-name");
            mcaCache.put(ruleName, new ServiceMcaRule(e));
            numDefs++;
        }

        if (Debug.importantOn()) {
            String resourceLocation = handler.getLocation();
            try {
                resourceLocation = handler.getURL().toExternalForm();
            } catch (GenericConfigException e) {
                Debug.logError(e, "Could not get resource URL", module);
            }
            Debug.logImportant("Loaded " + numDefs + " Service MCA definitions from " + resourceLocation, module);
        }
    }

    public static List<ServiceMcaRule> getServiceMcaRules() {
    if (mcaCache.size() == 0) {
        readConfig();
    }
        return mcaCache.values();
    }

    public static void evalRules(LocalDispatcher dispatcher, MimeMessageWrapper wrapper, GenericValue userLogin) throws GenericServiceException {
        Set<String> actionsRun = new TreeSet<String>();
        for (ServiceMcaRule rule: getServiceMcaRules()) {
            rule.eval(dispatcher, wrapper, actionsRun, userLogin);
        }
    }
}
