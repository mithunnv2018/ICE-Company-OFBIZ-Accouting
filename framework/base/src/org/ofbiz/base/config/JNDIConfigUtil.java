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
package org.ofbiz.base.config;

import java.util.*;
import org.w3c.dom.*;

import javolution.util.FastMap;

import org.ofbiz.base.util.*;

/**
 * JNDIConfigUtil
 *
 */
public class JNDIConfigUtil {

    public static final String module = JNDIConfigUtil.class.getName();
    public static final String JNDI_CONFIG_XML_FILENAME = "jndiservers.xml";
    protected static Map<String, JndiServerInfo> jndiServerInfos = FastMap.newInstance();

    protected static Element getXmlRootElement() throws GenericConfigException {
        try {
            return ResourceLoader.getXmlRootElement(JNDIConfigUtil.JNDI_CONFIG_XML_FILENAME);
        } catch (GenericConfigException e) {
            throw new GenericConfigException("Could not get JNDI XML root element", e);
        }
    }

    protected static Document getXmlDocument() throws GenericConfigException {
        try {
            return ResourceLoader.getXmlDocument(JNDIConfigUtil.JNDI_CONFIG_XML_FILENAME);
        } catch (GenericConfigException e) {
            throw new GenericConfigException("Could not get JNDI XML document", e);
        }
    }

    static {
        try {
            initialize(getXmlRootElement());
        } catch (Exception e) {
            Debug.logError(e, "Error loading JNDI config XML file " + JNDI_CONFIG_XML_FILENAME, module);
        }
    }
    public static void initialize(Element rootElement) throws GenericConfigException {
        // jndi-server - jndiServerInfos
        for (Element curElement: UtilXml.childElementList(rootElement, "jndi-server")) {
            JNDIConfigUtil.JndiServerInfo jndiServerInfo = new JNDIConfigUtil.JndiServerInfo(curElement);

            JNDIConfigUtil.jndiServerInfos.put(jndiServerInfo.name, jndiServerInfo);
        }
    }

    public static JNDIConfigUtil.JndiServerInfo getJndiServerInfo(String name) {
        return jndiServerInfos.get(name);
    }

    public static final class JndiServerInfo {
        public final String name;
        public final String contextProviderUrl;
        public final String initialContextFactory;
        public final String urlPkgPrefixes;
        public final String securityPrincipal;
        public final String securityCredentials;

        public JndiServerInfo(Element element) {
            this.name = element.getAttribute("name");
            this.contextProviderUrl = element.getAttribute("context-provider-url");
            this.initialContextFactory = element.getAttribute("initial-context-factory");
            this.urlPkgPrefixes = element.getAttribute("url-pkg-prefixes");
            this.securityPrincipal = element.getAttribute("security-principal");
            this.securityCredentials = element.getAttribute("security-credentials");
        }
    }
}
