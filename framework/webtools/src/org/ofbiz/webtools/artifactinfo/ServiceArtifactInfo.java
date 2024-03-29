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
package org.ofbiz.webtools.artifactinfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.location.FlexibleLocation;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.FileUtil;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilJavaParse;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilPlist;
import org.ofbiz.minilang.MiniLangException;
import org.ofbiz.minilang.SimpleMethod;
import org.ofbiz.service.ModelParam;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.eca.ServiceEcaRule;
import org.ofbiz.service.eca.ServiceEcaUtil;
import org.ofbiz.service.group.GroupModel;
import org.ofbiz.service.group.GroupServiceModel;
import org.ofbiz.service.group.ServiceGroupReader;

/**
 *
 */
public class ServiceArtifactInfo extends ArtifactInfoBase {
    public static final String module = ServiceArtifactInfo.class.getName();

    protected ModelService modelService;
    protected String displayPrefix = null;

    Set<EntityArtifactInfo> entitiesUsedByThisService = new TreeSet<EntityArtifactInfo>();
    Set<ServiceArtifactInfo> servicesCalledByThisService = new TreeSet<ServiceArtifactInfo>();
    Set<ServiceEcaArtifactInfo> serviceEcasTriggeredByThisService = new TreeSet<ServiceEcaArtifactInfo>();

    public ServiceArtifactInfo(String serviceName, ArtifactInfoFactory aif) throws GeneralException {
        super(aif);
        this.modelService = this.aif.getModelService(serviceName);
    }

    /**
     * This must be called after creation from the ArtifactInfoFactory after this class has been put into the global Map in order to avoid recursive initialization
     *
     * @throws GeneralException
     */
    public void populateAll() throws GeneralException {
        this.populateUsedEntities();
        this.populateCalledServices();
        this.populateTriggeredServiceEcas();
    }

    protected void populateUsedEntities() throws GeneralException {
        // populate entitiesUsedByThisService and for each the reverse-associate cache in the aif
        if ("simple".equals(this.modelService.engineName)) {
            // we can do something with this!
            SimpleMethod simpleMethodToCall = null;
            try {
                Map<String, SimpleMethod> simpleMethods = SimpleMethod.getSimpleMethods(this.modelService.location, null);
                simpleMethodToCall = (SimpleMethod) simpleMethods.get(this.modelService.invoke);
            } catch (MiniLangException e) {
                Debug.logWarning("Error getting Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "]: " + e.toString(), module);
            }
            if (simpleMethodToCall == null) {
                Debug.logWarning("Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "] not found", module);
                return;
            }

            Set<String> allEntityNameSet = simpleMethodToCall.getAllEntityNamesUsed();
            populateEntitiesFromNameSet(allEntityNameSet);
        } else if ("java".equals(this.modelService.engineName)) {
            String fullClassPathAndFile = UtilJavaParse.findRealPathAndFileForClass(this.modelService.location);
            if (fullClassPathAndFile != null) {
                String javaFile = null;
                try {
                    javaFile = FileUtil.readTextFile(fullClassPathAndFile, true).toString();
                } catch (FileNotFoundException e) {
                    Debug.logWarning("Error reading java file [" + fullClassPathAndFile + "] for service implementation: " + e.toString(), module);
                    return;
                } catch (IOException e) {
                    Debug.logWarning("Error reading java file [" + fullClassPathAndFile + "] for service implementation: " + e.toString(), module);
                    return;
                }

                javaFile = UtilJavaParse.stripComments(javaFile);
                int methodBlockStart = UtilJavaParse.findServiceMethodBlockStart(this.modelService.invoke, javaFile);
                int methodBlockEnd = UtilJavaParse.findEndOfBlock(methodBlockStart, javaFile);
                Set<String> allEntityNameSet = UtilJavaParse.findEntityUseInBlock(methodBlockStart, methodBlockEnd, javaFile);
                populateEntitiesFromNameSet(allEntityNameSet);
            }
        } else if ("group".equals(this.modelService.engineName)) {
            // nothing to do, there won't be entities referred to in these
        }
    }
    protected void populateEntitiesFromNameSet(Set<String> allEntityNameSet) throws GeneralException {
        for (String entityName: allEntityNameSet) {
            if (entityName.contains("${")) {
                continue;
            }
            // attempt to convert relation names to entity names
            String validEntityName = aif.getEntityModelReader().validateEntityName(entityName);
            if (validEntityName == null) {
                Debug.logWarning("Entity [" + entityName + "] reference in service [" + this.modelService.name + "] does not exist!", module);
                continue;
            }

            // the forward reference
            this.entitiesUsedByThisService.add(aif.getEntityArtifactInfo(validEntityName));
            // the reverse reference
            UtilMisc.addToSortedSetInMap(this, aif.allServiceInfosReferringToEntityName, validEntityName);
        }
    }

    protected void populateCalledServices() throws GeneralException {
        // populate servicesCalledByThisService and for each the reverse-associate cache in the aif
        if ("simple".equals(this.modelService.engineName)) {
            // we can do something with this!
            SimpleMethod simpleMethodToCall = null;
            try {
                Map<String, SimpleMethod> simpleMethods = SimpleMethod.getSimpleMethods(this.modelService.location, null);
                simpleMethodToCall = (SimpleMethod) simpleMethods.get(this.modelService.invoke);
            } catch (MiniLangException e) {
                Debug.logWarning("Error getting Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "]: " + e.toString(), module);
            }
            if (simpleMethodToCall == null) {
                Debug.logWarning("Simple-method [" + this.modelService.invoke + "] in [" + this.modelService.location + "] referenced in service [" + this.modelService.name + "] not found", module);
                return;
            }

            Set<String> allServiceNameSet = simpleMethodToCall.getAllServiceNamesCalled();
            populateServicesFromNameSet(allServiceNameSet);
        } else if ("java".equals(this.modelService.engineName)) {
            String fullClassPathAndFile = UtilJavaParse.findRealPathAndFileForClass(this.modelService.location);
            if (fullClassPathAndFile != null) {
                String javaFile = null;
                try {
                    javaFile = FileUtil.readTextFile(fullClassPathAndFile, true).toString();
                } catch (FileNotFoundException e) {
                    Debug.logWarning("Error reading java file [" + fullClassPathAndFile + "] for service implementation: " + e.toString(), module);
                    return;
                } catch (IOException e) {
                    Debug.logWarning("Error reading java file [" + fullClassPathAndFile + "] for service implementation: " + e.toString(), module);
                    return;
                }

                javaFile = UtilJavaParse.stripComments(javaFile);
                int methodBlockStart = UtilJavaParse.findServiceMethodBlockStart(this.modelService.invoke, javaFile);
                int methodBlockEnd = UtilJavaParse.findEndOfBlock(methodBlockStart, javaFile);
                Set<String> allServiceNameSet = UtilJavaParse.findServiceCallsInBlock(methodBlockStart, methodBlockEnd, javaFile);

                populateServicesFromNameSet(allServiceNameSet);
            }
        } else if ("group".equals(this.modelService.engineName)) {
            Set<String> allServiceNameSet = FastSet.newInstance();
            GroupModel groupModel = modelService.internalGroup;
            if (groupModel == null) {
                groupModel = ServiceGroupReader.getGroupModel(this.modelService.location);
            }

            if (groupModel != null) {
                List<GroupServiceModel> groupServiceModels = groupModel.getServices();
                for (GroupServiceModel groupServiceModel: groupServiceModels) {
                    allServiceNameSet.add(groupServiceModel.getName());
                }
            }

            populateServicesFromNameSet(allServiceNameSet);
        }
    }

    protected void populateServicesFromNameSet(Set<String> allServiceNameSet) throws GeneralException {
        for (String serviceName: allServiceNameSet) {
            if (serviceName.contains("${")) {
                continue;
            }
            if (!aif.getDispatchContext().getAllServiceNames().contains(serviceName)) {
                Debug.logWarning("Service [" + serviceName + "] reference in service [" + this.modelService.name + "] does not exist!", module);
                continue;
            }

            // the forward reference
            this.servicesCalledByThisService.add(aif.getServiceArtifactInfo(serviceName));
            // the reverse reference
            UtilMisc.addToSortedSetInMap(this, aif.allServiceInfosReferringToServiceName, serviceName);
        }
    }

    protected void populateTriggeredServiceEcas() throws GeneralException {
        // populate serviceEcasTriggeredByThisService and for each the reverse-associate cache in the aif
        Map<String, List<ServiceEcaRule>> serviceEventMap = ServiceEcaUtil.getServiceEventMap(this.modelService.name);
        if (serviceEventMap == null) return;
        for (List<ServiceEcaRule> ecaRuleList: serviceEventMap.values()) {
            for (ServiceEcaRule ecaRule: ecaRuleList) {
                this.serviceEcasTriggeredByThisService.add(aif.getServiceEcaArtifactInfo(ecaRule));
                // the reverse reference
                UtilMisc.addToSortedSetInMap(this, aif.allServiceInfosReferringToServiceEcaRule, ecaRule);
            }
        }
    }

    public ModelService getModelService() {
        return this.modelService;
    }

    public void setDisplayPrefix(String displayPrefix) {
        this.displayPrefix = displayPrefix;
    }

    public String getDisplayName() {
        return this.getDisplayPrefixedName();
    }
    public String getDisplayPrefixedName() {
        return (this.displayPrefix != null ? this.displayPrefix : "") + this.modelService.name;
    }

    public String getDisplayType() {
        return "Service";
    }

    public String getType() {
        return ArtifactInfoFactory.ServiceInfoTypeId;
    }

    public String getUniqueId() {
        return this.modelService.name;
    }

    public URL getLocationURL() throws MalformedURLException {
        return FlexibleLocation.resolveLocation(this.modelService.definitionLocation, null);
    }

    public URL getImplementationLocationURL() throws MalformedURLException {
        return FlexibleLocation.resolveLocation(this.modelService.location, null);
    }

    public Set<EntityArtifactInfo> getEntitiesUsedByService() {
        return this.entitiesUsedByThisService;
    }

    public Set<ServiceArtifactInfo> getServicesCallingService() {
        return aif.allServiceInfosReferringToServiceName.get(this.modelService.name);
    }

    public Set<ServiceArtifactInfo> getServicesCalledByService() {
        return this.servicesCalledByThisService;
    }

    public Set<ServiceArtifactInfo> getServicesCalledByServiceEcas() {
        // TODO: implement this sometime, not really necessary
        return FastSet.newInstance();
    }

    public Set<ServiceEcaArtifactInfo> getServiceEcaRulesTriggeredByService() {
        return this.serviceEcasTriggeredByThisService;
    }

    public Set<ServiceArtifactInfo> getServicesCallingServiceByEcas() {
        // TODO: implement this sometime, not really necessary
        return FastSet.newInstance();
    }

    public Set<ServiceEcaArtifactInfo> getServiceEcaRulesCallingService() {
        return this.aif.allServiceEcaInfosReferringToServiceName.get(this.modelService.name);
    }

    public Set<FormWidgetArtifactInfo> getFormsCallingService() {
        return this.aif.allFormInfosReferringToServiceName.get(this.modelService.name);
    }

    public Set<FormWidgetArtifactInfo> getFormsBasedOnService() {
        return this.aif.allFormInfosBasedOnServiceName.get(this.modelService.name);
    }

    public Set<ScreenWidgetArtifactInfo> getScreensCallingService() {
        return this.aif.allScreenInfosReferringToServiceName.get(this.modelService.name);
    }

    public Set<ControllerRequestArtifactInfo> getRequestsWithEventCallingService() {
        return this.aif.allRequestInfosReferringToServiceName.get(this.modelService.name);
    }

    public void writeServiceCallGraphEoModel(String eomodeldFullPath) throws GeneralException, FileNotFoundException, UnsupportedEncodingException {
        boolean useMoreDetailedNames = true;

        Debug.logInfo("Writing Service Call Graph EO Model for service [" + this.modelService.name + "] to [" + eomodeldFullPath + "]", module);

        Set<String> allDiagramEntitiesWithPrefixes = FastSet.newInstance();
        List<ServiceArtifactInfo> allServiceList = FastList.newInstance();
        List<ServiceEcaArtifactInfo> allServiceEcaList = FastList.newInstance();

        // make sure that any prefix that might have been set on this is cleared
        this.setDisplayPrefix("");

        // put this service in the master list
        allDiagramEntitiesWithPrefixes.add(this.modelService.name);

        // all services that call this service
        Set<ServiceArtifactInfo> callingServiceSet = this.getServicesCallingService();
        if (callingServiceSet != null) {
            // set the prefix and add to the all list
            for (ServiceArtifactInfo callingService: callingServiceSet) {
                callingService.setDisplayPrefix("Calling_");
                allDiagramEntitiesWithPrefixes.add(callingService.getDisplayPrefixedName());
                allServiceList.add(callingService);
            }
        }

        // all services this service calls
        Set<ServiceArtifactInfo> calledServiceSet = this.getServicesCalledByService();
        for (ServiceArtifactInfo calledService: calledServiceSet) {
            calledService.setDisplayPrefix("Called_");
            allDiagramEntitiesWithPrefixes.add(calledService.getDisplayPrefixedName());
            allServiceList.add(calledService);
        }

        Map<String, Integer> displaySuffixNumByEcaName = FastMap.newInstance();

        // all SECAs and triggering services that call this service as an action
        Set<ServiceEcaArtifactInfo> callingServiceEcaSet = this.getServiceEcaRulesCallingService();
        if (callingServiceEcaSet != null) {
            for (ServiceEcaArtifactInfo callingServiceEca: callingServiceEcaSet) {
                callingServiceEca.setDisplayPrefix("Triggering_");

                Integer displaySuffix = displaySuffixNumByEcaName.get(callingServiceEca.getDisplayPrefixedName());
                if (displaySuffix == null) {
                    displaySuffix = 1;
                } else {
                    displaySuffix++;
                }
                displaySuffixNumByEcaName.put(callingServiceEca.getDisplayPrefixedName(), displaySuffix);
                callingServiceEca.setDisplaySuffixNum(displaySuffix);

                allDiagramEntitiesWithPrefixes.add(callingServiceEca.getDisplayPrefixedName());
                allServiceEcaList.add(callingServiceEca);
            }
        }

        // all SECAs and corresponding services triggered by this service
        Set<ServiceEcaArtifactInfo> calledServiceEcaSet = this.getServiceEcaRulesTriggeredByService();
        for (ServiceEcaArtifactInfo calledServiceEca: calledServiceEcaSet) {
            calledServiceEca.setDisplayPrefix("Triggered_");

            Integer displaySuffix = displaySuffixNumByEcaName.get(calledServiceEca.getDisplayPrefixedName());
            if (displaySuffix == null) {
                displaySuffix = 1;
            } else {
                displaySuffix++;
            }
            displaySuffixNumByEcaName.put(calledServiceEca.getDisplayPrefixedName(), displaySuffix);
            calledServiceEca.setDisplaySuffixNum(displaySuffix);

            allDiagramEntitiesWithPrefixes.add(calledServiceEca.getDisplayPrefixedName());
            allServiceEcaList.add(calledServiceEca);
        }

        // write index.eomodeld file
        Map<String, Object> indexEoModelMap = FastMap.newInstance();
        indexEoModelMap.put("EOModelVersion", "\"2.1\"");
        List<Map<String, Object>> entitiesMapList = FastList.newInstance();
        indexEoModelMap.put("entities", entitiesMapList);
        for (String entityName: allDiagramEntitiesWithPrefixes) {
            Map<String, Object> entitiesMap = FastMap.newInstance();
            entitiesMapList.add(entitiesMap);
            entitiesMap.put("className", "EOGenericRecord");
            entitiesMap.put("name", entityName);
        }
        UtilPlist.writePlistFile(indexEoModelMap, eomodeldFullPath, "index.eomodeld", true);

        // write this service description file
        Map<String, Object> thisServiceEoModelMap = createEoModelMap(callingServiceSet, calledServiceSet, callingServiceEcaSet, calledServiceEcaSet, useMoreDetailedNames);
        UtilPlist.writePlistFile(thisServiceEoModelMap, eomodeldFullPath, this.modelService.name + ".plist", true);

        // write service description files
        if (callingServiceSet != null) {
            for (ServiceArtifactInfo callingService: callingServiceSet) {
                Map<String, Object> serviceEoModelMap = callingService.createEoModelMap(null, UtilMisc.toSet(this), null, null, useMoreDetailedNames);
                UtilPlist.writePlistFile(serviceEoModelMap, eomodeldFullPath, callingService.getDisplayPrefixedName() + ".plist", true);
            }
        }
        if (calledServiceSet != null) {
            for (ServiceArtifactInfo calledService: calledServiceSet) {
                Map<String, Object> serviceEoModelMap = calledService.createEoModelMap(UtilMisc.toSet(this), null, null, null, useMoreDetailedNames);
                UtilPlist.writePlistFile(serviceEoModelMap, eomodeldFullPath, calledService.getDisplayPrefixedName() + ".plist", true);
            }
        }

        // write SECA description files
        if (callingServiceEcaSet != null) {
            for (ServiceEcaArtifactInfo callingServiceEca: callingServiceEcaSet) {
                // add List<ServiceArtifactInfo> for services that trigger this eca rule
                Set<ServiceArtifactInfo> ecaCallingServiceSet = callingServiceEca.getServicesTriggeringServiceEca();
                for (ServiceArtifactInfo ecaCallingService: ecaCallingServiceSet) {
                    ecaCallingService.setDisplayPrefix("Triggering_");
                }
                ecaCallingServiceSet.add(this);

                Map<String, Object> serviceEcaEoModelMap = callingServiceEca.createEoModelMap(ecaCallingServiceSet, null, useMoreDetailedNames);
                UtilPlist.writePlistFile(serviceEcaEoModelMap, eomodeldFullPath, callingServiceEca.getDisplayPrefixedName() + ".plist", true);
            }
        }
        if (calledServiceEcaSet != null) {
            for (ServiceEcaArtifactInfo calledServiceEca: calledServiceEcaSet) {
                // add List<ServiceArtifactInfo> for services this eca rule calls in action
                Set<ServiceArtifactInfo> ecaCalledServiceSet = calledServiceEca.getServicesCalledByServiceEcaActions();
                for (ServiceArtifactInfo ecaCalledService: ecaCalledServiceSet) {
                    ecaCalledService.setDisplayPrefix("Triggered_");
                }
                ecaCalledServiceSet.add(this);

                Map<String, Object> serviceEcaEoModelMap = calledServiceEca.createEoModelMap(null, ecaCalledServiceSet, useMoreDetailedNames);
                UtilPlist.writePlistFile(serviceEcaEoModelMap, eomodeldFullPath, calledServiceEca.getDisplayPrefixedName() + ".plist", true);
            }
        }
    }

    public Map<String, Object> createEoModelMap(Set<ServiceArtifactInfo> callingServiceSet, Set<ServiceArtifactInfo> calledServiceSet, Set<ServiceEcaArtifactInfo> callingServiceEcaSet, Set<ServiceEcaArtifactInfo> calledServiceEcaSet, boolean useMoreDetailedNames) {
        if (callingServiceSet == null) callingServiceSet = FastSet.newInstance();
        if (calledServiceSet == null) calledServiceSet = FastSet.newInstance();
        if (callingServiceEcaSet == null) callingServiceEcaSet = FastSet.newInstance();
        if (calledServiceEcaSet == null) calledServiceEcaSet = FastSet.newInstance();
        Map<String, Object> topLevelMap = FastMap.newInstance();

        topLevelMap.put("name", this.getDisplayPrefixedName());
        topLevelMap.put("className", "EOGenericRecord");

        // for classProperties add attribute names AND relationship names to get a nice, complete chart
        List<String> classPropertiesList = FastList.newInstance();
        topLevelMap.put("classProperties", classPropertiesList);
        for (ModelParam param: this.modelService.getModelParamList()) {
            // skip the internal parameters, very redundant in the diagrams
            if (param.internal) continue;

            if (useMoreDetailedNames) {
                classPropertiesList.add(param.getShortDisplayDescription());
            } else {
                classPropertiesList.add(param.name);
            }
        }
        for (ServiceArtifactInfo sai: callingServiceSet) {
            classPropertiesList.add(sai.getDisplayPrefixedName());
        }
        for (ServiceArtifactInfo sai: calledServiceSet) {
            classPropertiesList.add(sai.getDisplayPrefixedName());
        }
        for (ServiceEcaArtifactInfo seai: callingServiceEcaSet) {
            classPropertiesList.add(seai.getDisplayPrefixedName());
        }
        for (ServiceEcaArtifactInfo seai: calledServiceEcaSet) {
            classPropertiesList.add(seai.getDisplayPrefixedName());
        }

        // attributes
        List<Map<String, Object>> attributesList = FastList.newInstance();
        topLevelMap.put("attributes", attributesList);
        for (ModelParam param: this.modelService.getModelParamList()) {
            Map<String, Object> attributeMap = FastMap.newInstance();
            attributesList.add(attributeMap);

            if (useMoreDetailedNames) {
                attributeMap.put("name", param.getShortDisplayDescription());
            } else {
                attributeMap.put("name", param.name);
            }
            attributeMap.put("valueClassName", param.type);
            attributeMap.put("externalType", param.type);
        }

        // relationships
        List<Map<String, Object>> relationshipsMapList = FastList.newInstance();

        for (ServiceArtifactInfo sai: callingServiceSet) {
            Map<String, Object> relationshipMap = FastMap.newInstance();
            relationshipsMapList.add(relationshipMap);

            relationshipMap.put("name", sai.getDisplayPrefixedName());
            relationshipMap.put("destination", sai.getDisplayPrefixedName());
            relationshipMap.put("isToMany", "N");
            relationshipMap.put("isMandatory", "Y");

            // not sure if we can use these, or need them, for this type of diagram
            //relationshipMap.put("joinSemantic", "EOInnerJoin");
            //relationshipMap.put("joins", joinsMapList);
            //joinsMap.put("sourceAttribute", keyMap.getFieldName());
            //joinsMap.put("destinationAttribute", keyMap.getRelFieldName());
        }
        for (ServiceArtifactInfo sai: calledServiceSet) {
            Map<String, Object> relationshipMap = FastMap.newInstance();
            relationshipsMapList.add(relationshipMap);

            relationshipMap.put("name", sai.getDisplayPrefixedName());
            relationshipMap.put("destination", sai.getDisplayPrefixedName());
            relationshipMap.put("isToMany", "Y");
            relationshipMap.put("isMandatory", "Y");

            // not sure if we can use these, or need them, for this type of diagram
            //relationshipMap.put("joinSemantic", "EOInnerJoin");
            //relationshipMap.put("joins", joinsMapList);
            //joinsMap.put("sourceAttribute", keyMap.getFieldName());
            //joinsMap.put("destinationAttribute", keyMap.getRelFieldName());
        }

        for (ServiceEcaArtifactInfo seai: callingServiceEcaSet) {
            Map<String, Object> relationshipMap = FastMap.newInstance();
            relationshipsMapList.add(relationshipMap);

            relationshipMap.put("name", seai.getDisplayPrefixedName());
            relationshipMap.put("destination", seai.getDisplayPrefixedName());
            relationshipMap.put("isToMany", "N");
            relationshipMap.put("isMandatory", "Y");
        }
        for (ServiceEcaArtifactInfo seai: calledServiceEcaSet) {
            Map<String, Object> relationshipMap = FastMap.newInstance();
            relationshipsMapList.add(relationshipMap);

            relationshipMap.put("name", seai.getDisplayPrefixedName());
            relationshipMap.put("destination", seai.getDisplayPrefixedName());
            relationshipMap.put("isToMany", "Y");
            relationshipMap.put("isMandatory", "Y");
        }

        if (relationshipsMapList.size() > 0) {
            topLevelMap.put("relationships", relationshipsMapList);
        }

        return topLevelMap;
    }

    public boolean equals(Object obj) {
        if (obj instanceof ServiceArtifactInfo) {
            return this.modelService.name.equals(((ServiceArtifactInfo) obj).modelService.name);
        } else {
            return false;
        }
    }
}
