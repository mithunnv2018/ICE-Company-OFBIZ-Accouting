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
package org.ofbiz.product.config;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import javolution.util.FastList;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilHttp;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.product.catalog.CatalogWorker;
import org.ofbiz.product.config.ProductConfigWrapper.ConfigItem;
import org.ofbiz.product.config.ProductConfigWrapper.ConfigOption;
import org.ofbiz.product.product.ProductWorker;
import org.ofbiz.product.store.ProductStoreWorker;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.base.util.cache.UtilCache;

/**
 * Product Config Worker class to reduce code in templates.
 */
public class ProductConfigWorker {

    public static final String module = ProductConfigWorker.class.getName();
    public static final String resource = "ProductUiLabels";
    public static final String SEPARATOR = "::";    // cache key separator

    public static UtilCache<String, ProductConfigWrapper> productConfigCache = new UtilCache<String, ProductConfigWrapper>("product.config", true);     // use soft reference to free up memory if needed

    public static ProductConfigWrapper getProductConfigWrapper(String productId, String currencyUomId, HttpServletRequest request) {
        ProductConfigWrapper configWrapper = null;
        String catalogId = CatalogWorker.getCurrentCatalogId(request);
        String webSiteId = CatalogWorker.getWebSiteId(request);
        String productStoreId = ProductStoreWorker.getProductStoreId(request);
        GenericValue autoUserLogin = (GenericValue)request.getSession().getAttribute("autoUserLogin");
        try {
            /* caching: there is one cache created, "product.config"  Each product's config wrapper is cached with a key of
             * productId::catalogId::webSiteId::currencyUomId, or whatever the SEPARATOR is defined above to be.
             */
            String cacheKey = productId + SEPARATOR + productStoreId + SEPARATOR + catalogId + SEPARATOR + webSiteId + SEPARATOR + currencyUomId;
            if (!productConfigCache.containsKey(cacheKey)) {
                configWrapper = new ProductConfigWrapper((GenericDelegator)request.getAttribute("delegator"),
                                                         (LocalDispatcher)request.getAttribute("dispatcher"),
                                                         productId, productStoreId, catalogId, webSiteId,
                                                         currencyUomId, UtilHttp.getLocale(request),
                                                         autoUserLogin);
                productConfigCache.put(cacheKey, new ProductConfigWrapper(configWrapper));
            } else {
                configWrapper = new ProductConfigWrapper(productConfigCache.get(cacheKey));
            }
        } catch (ProductConfigWrapperException we) {
            configWrapper = null;
        } catch (Exception e) {
            Debug.logWarning(e.getMessage(), module);
        }
        return configWrapper;
    }

    public static void fillProductConfigWrapper(ProductConfigWrapper configWrapper, HttpServletRequest request) {
        int numOfQuestions = configWrapper.getQuestions().size();
        for (int k = 0; k < numOfQuestions; k++) {
            String[] opts = request.getParameterValues(Integer.toString(k));
            if (opts == null) {

                //  check for standard item comments
                ProductConfigWrapper.ConfigItem question = configWrapper.getQuestions().get(k);
                if (question.isStandard()) {
                    int i = 0;
                    while (i <= (question.getOptions().size() -1)) {
                        String comments = request.getParameter("comments_" + k + "_" + i);
                        if (UtilValidate.isNotEmpty(comments)) {
                            try {
                                configWrapper.setSelected(k, i, comments);
                            } catch (Exception e) {
                                Debug.logWarning(e.getMessage(), module);
                            }
                        }
                        i++;
                    }
                }
                continue;
            }
            for (String opt: opts) {
                int cnt = -1;
                try {
                    cnt = Integer.parseInt(opt);
                    String comments = null;
                    ProductConfigWrapper.ConfigItem question = (ProductConfigWrapper.ConfigItem) configWrapper.getQuestions().get(k);
                    if (question.isSingleChoice()) {
                        comments = request.getParameter("comments_" + k + "_" + "0");
                    } else {
                        comments = request.getParameter("comments_" + k + "_" + cnt);
                    }

                    configWrapper.setSelected(k, cnt, comments);
                    ProductConfigWrapper.ConfigOption option = configWrapper.getItemOtion(k, cnt);

                    //  set selected variant products
                    if (UtilValidate.isNotEmpty(option) && (option.hasVirtualComponent())) {
                        List<GenericValue> components = option.getComponents();
                        int variantIndex = 0;
                        for (int i = 0; i < components.size(); i++) {
                            GenericValue component = components.get(i);
                            if (option.isVirtualComponent(component)) {
                                String productParamName = "add_product_id" + k + "_" + cnt + "_" + variantIndex;
                                String selectedProdcutId = request.getParameter(productParamName);
                                if (UtilValidate.isEmpty(selectedProdcutId)) {
                                    Debug.logWarning("ERROR: Request param [" + productParamName + "] not found!", module);
                                } else {

                                    //  handle also feature tree virtual variant methods
                                    if (ProductWorker.isVirtual((GenericDelegator)request.getAttribute("delegator"), selectedProdcutId)) {
                                        if ("VV_FEATURETREE".equals(ProductWorker.getProductvirtualVariantMethod((GenericDelegator)request.getAttribute("delegator"), selectedProdcutId))) {
                                            // get the selected features
                                            List<String> selectedFeatures = FastList.newInstance();
                                            Enumeration paramNames = request.getParameterNames();
                                            while (paramNames.hasMoreElements()) {
                                                String paramName = (String)paramNames.nextElement();
                                                if (paramName.startsWith("FT" + k + "_" + cnt + "_" + variantIndex)) {
                                                    selectedFeatures.add(request.getParameterValues(paramName)[0]);
                                                }
                                            }

                                            // check if features are selected
                                            if (UtilValidate.isEmpty(selectedFeatures)) {
                                                Debug.logWarning("ERROR: No features selected for productId [" + selectedProdcutId+ "]", module);
                                            }

                                            String variantProductId = ProductWorker.getVariantFromFeatureTree(selectedProdcutId, selectedFeatures, (GenericDelegator)request.getAttribute("delegator"));
                                            if (UtilValidate.isNotEmpty(variantProductId)) {
                                                selectedProdcutId = variantProductId;
                                            } else {
                                                Debug.logWarning("ERROR: Variant product not found!", module);
                                                request.setAttribute("_EVENT_MESSAGE_", UtilProperties.getMessage("OrderErrorUiLabels", "cart.addToCart.incompatibilityVariantFeature", UtilHttp.getLocale(request)));
                                           }
                                        }
                                    }
                                    configWrapper.setSelected(k, cnt, i, selectedProdcutId);
                                }
                                variantIndex ++;
                            }
                        }
                    }
                } catch (Exception e) {
                    Debug.logWarning(e.getMessage(), module);
                }
            }
        }
    }

    /**
     * First search persisted configurations and update configWrapper.configId if found.
     * Otherwise store ProductConfigWrapper to ProductConfigConfig entity and updates configWrapper.configId with new configId
     * This method persists only the selected options, price data is lost.
     * @param ProductConfigWrapper
     * @param delegator
     */
    public static void storeProductConfigWrapper(ProductConfigWrapper configWrapper, GenericDelegator delegator) {
        if (configWrapper == null || (!configWrapper.isCompleted()))  return;
        String configId = null;
        List<ConfigItem> questions = configWrapper.getQuestions();
        List<GenericValue> configsToCheck = FastList.newInstance();
        int selectedOptionSize = 0;
        for (ConfigItem ci: questions) {
            String configItemId = null;
            Long sequenceNum = null;
            List<ProductConfigWrapper.ConfigOption> selectedOptions = FastList.newInstance();
            List<ConfigOption> options = ci.getOptions();
            if (ci.isStandard()) {
                selectedOptions.addAll(options);
            } else {
                for (ConfigOption oneOption: options) {
                    if (oneOption.isSelected()) {
                        selectedOptions.add(oneOption);
                    }
                }
            }

            if (selectedOptions.size() > 0) {
                selectedOptionSize += selectedOptions.size();
                configItemId = ci.getConfigItemAssoc().getString("configItemId");
                sequenceNum = ci.getConfigItemAssoc().getLong("sequenceNum");
                try {
                    List<GenericValue> configs = delegator.findByAnd("ProductConfigConfig", UtilMisc.toMap("configItemId",configItemId,"sequenceNum", sequenceNum));
                    for (GenericValue productConfigConfig: configs) {
                        for (ConfigOption oneOption: selectedOptions) {
                            String configOptionId = oneOption.configOption.getString("configOptionId");
                            if (productConfigConfig.getString("configOptionId").equals(configOptionId)) {
                                String comments = oneOption.getComments() != null ? oneOption.getComments() : "";
                                if ((UtilValidate.isEmpty(comments) && UtilValidate.isEmpty(productConfigConfig.getString("description"))) || comments.equals(productConfigConfig.getString("description"))) {
                                    configsToCheck.add(productConfigConfig);
                                }
                            }
                        }
                    }

                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }

            }
        }
        if (UtilValidate.isNotEmpty(configsToCheck)) {
            for (GenericValue productConfigConfig: configsToCheck) {
                String tempConfigId = productConfigConfig.getString("configId");
                try {
                    List<GenericValue> tempResult = delegator.findByAnd("ProductConfigConfig", UtilMisc.toMap("configId",tempConfigId));
                    if (tempResult.size() == selectedOptionSize && configsToCheck.containsAll(tempResult)) {
                        List<GenericValue> configOptionProductOptions = delegator.findByAnd("ConfigOptionProductOption", UtilMisc.toMap("configId",tempConfigId));
                        if (UtilValidate.isNotEmpty(configOptionProductOptions)) {

                            //  check for variant product equality
                            for (ConfigItem ci: questions) {
                                String configItemId = null;
                                Long sequenceNum = null;
                                List<ProductConfigWrapper.ConfigOption> selectedOptions = FastList.newInstance();
                                List<ConfigOption> options = ci.getOptions();
                                if (ci.isStandard()) {
                                    selectedOptions.addAll(options);
                                } else {
                                    for (ConfigOption oneOption: options) {
                                        if (oneOption.isSelected()) {
                                            selectedOptions.add(oneOption);
                                        }
                                    }
                                }

                                boolean match = true;
                                for (ProductConfigWrapper.ConfigOption anOption : selectedOptions) {
                                    if (match && anOption.hasVirtualComponent()) {
                                        List<GenericValue> components = anOption.getComponents();
                                        for (GenericValue aComponent : components) {
                                            if (anOption.isVirtualComponent(aComponent)) {
                                                Map<String, String> componentOptions = anOption.getComponentOptions();
                                                String optionProductId = aComponent.getString("productId");
                                                String optionProductOptionId = componentOptions.get(optionProductId);
                                                String configOptionId = anOption.configOption.getString("configOptionId");
                                                configItemId = ci.getConfigItemAssoc().getString("configItemId");
                                                sequenceNum = ci.getConfigItemAssoc().getLong("sequenceNum");

                                                GenericValue configOptionProductOption = delegator.makeValue("ConfigOptionProductOption");
                                                configOptionProductOption.set("configId", tempConfigId);
                                                configOptionProductOption.set("configItemId",configItemId);
                                                configOptionProductOption.set("sequenceNum", sequenceNum);
                                                configOptionProductOption.set("configOptionId", configOptionId);
                                                configOptionProductOption.set("productId", optionProductId);
                                                configOptionProductOption.set("productOptionId", optionProductOptionId);
                                                if (!configOptionProductOptions.remove(configOptionProductOption)) {
                                                    match = false;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                if (match && (UtilValidate.isEmpty(configOptionProductOptions))) {
                                    configWrapper.configId = tempConfigId;
                                    Debug.logInfo("Existing configuration found with configId:"+ tempConfigId,  module);
                                    return;
                                }
                            }

                        } else {
                            configWrapper.configId = tempConfigId;
                            Debug.logInfo("Existing configuration found with configId:"+ tempConfigId,  module);
                            return;
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }

            }
        }

        //Current configuration is not found in ProductConfigConfig entity. So lets store this one
        boolean nextId = true;
        for (ConfigItem ci: questions) {
            String configItemId = null;
            Long sequenceNum = null;
            List<ProductConfigWrapper.ConfigOption> selectedOptions = FastList.newInstance();
            List<ConfigOption> options = ci.getOptions();
           if (ci.isStandard()) {
                selectedOptions.addAll(options);
            } else {
                for (ConfigOption oneOption: options) {
                    if (oneOption.isSelected()) {
                        selectedOptions.add(oneOption);
                    }
                }
            }

            if (selectedOptions.size() > 0) {
                if (nextId) {
                    configId = delegator.getNextSeqId("ProductConfigConfig");
                    //get next configId only once and only if there are selectedOptions
                    nextId = false;
                }
                configItemId = ci.getConfigItemAssoc().getString("configItemId");
                sequenceNum = ci.getConfigItemAssoc().getLong("sequenceNum");
                for (ConfigOption oneOption: selectedOptions) {
                    List<GenericValue> toBeStored = FastList.newInstance();
                    String configOptionId = oneOption.configOption.getString("configOptionId");
                    String description = oneOption.getComments();
                    GenericValue productConfigConfig = delegator.makeValue("ProductConfigConfig");
                    productConfigConfig.put("configId", configId);
                    productConfigConfig.put("configItemId", configItemId);
                    productConfigConfig.put("sequenceNum", sequenceNum);
                    productConfigConfig.put("configOptionId", configOptionId);
                    productConfigConfig.put("description", description);
                    toBeStored.add(productConfigConfig);

                    if (oneOption.hasVirtualComponent()) {
                        List<GenericValue> components = oneOption.getComponents();
                        for (GenericValue component: components) {
                            if (oneOption.isVirtualComponent(component)) {
                                String componentOption = (String)oneOption.componentOptions.get(component.getString("productId"));
                                GenericValue configOptionProductOption = delegator.makeValue("ConfigOptionProductOption");
                                configOptionProductOption.put("configId", configId);
                                configOptionProductOption.put("configItemId", configItemId);
                                configOptionProductOption.put("sequenceNum", sequenceNum);
                                configOptionProductOption.put("configOptionId", configOptionId);
                                configOptionProductOption.put("productId", component.getString("productId"));
                                configOptionProductOption.put("productOptionId", componentOption);
                                toBeStored.add(configOptionProductOption);
                            }
                        }
                    }
                    try {
                        delegator.storeAll(toBeStored);
                    } catch (GenericEntityException e) {
                        configId = null;
                        Debug.logWarning(e.getMessage(), module);
                    }
                }
            }
        }

        //save  configId to configWrapper, so we can use it in shopping cart operations
        configWrapper.configId = configId;
        Debug.logInfo("New configId created:"+ configId,  module);
        return;
    }

    /**
     * Creates a new ProductConfigWrapper for productId and configures it according to ProductConfigConfig entity with configId
     * ProductConfigConfig entity stores only the selected options, and the product price is calculated from input params
     * @param delegator
     * @param dispatcher
     * @param configId configuration Id
     * @param productId AGGRAGATED productId
     * @param productStoreId needed for price calculations
     * @param catalogId needed for price calculations
     * @param webSiteId needed for price calculations
     * @param currencyUomId needed for price calculations
     * @param locale
     * @param autoUserLogin
     * @return ProductConfigWrapper
     */
    public static ProductConfigWrapper loadProductConfigWrapper(GenericDelegator delegator, LocalDispatcher dispatcher, String configId, String productId, String productStoreId, String catalogId, String webSiteId, String currencyUomId, Locale locale, GenericValue autoUserLogin) {
        ProductConfigWrapper configWrapper = null;
        try {
             configWrapper = new ProductConfigWrapper(delegator, dispatcher, productId, productStoreId, catalogId, webSiteId, currencyUomId, locale, autoUserLogin);
            if (configWrapper != null && UtilValidate.isNotEmpty(configId)) {
                configWrapper.loadConfig(delegator, configId);
            }
        } catch (Exception e) {
            Debug.logWarning(e.getMessage(), module);
            configWrapper = null;
        }
        return configWrapper;
    }

}

