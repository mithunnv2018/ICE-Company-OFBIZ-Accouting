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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.LocalDispatcher;


/**
 * Product Config Wrapper: gets product config to display
 */

public class ProductConfigWrapper implements Serializable {

    public static final String module = ProductConfigWrapper.class.getName();

    protected LocalDispatcher dispatcher;
    protected String productStoreId;
    protected String catalogId;
    protected String webSiteId;
    protected String currencyUomId;
    protected GenericDelegator delegator;
    protected GenericValue product = null; // the aggregated product
    protected GenericValue autoUserLogin = null;
    protected BigDecimal listPrice = BigDecimal.ZERO;
    protected BigDecimal basePrice = BigDecimal.ZERO;
    protected BigDecimal defaultPrice = BigDecimal.ZERO;
    protected String configId = null; // Id of persisted ProductConfigWrapper
    protected List<ConfigItem> questions = null; // ProductConfigs

    /** Creates a new instance of ProductConfigWrapper */
    public ProductConfigWrapper() {
    }

    public ProductConfigWrapper(GenericDelegator delegator, LocalDispatcher dispatcher, String productId, String productStoreId, String catalogId, String webSiteId, String currencyUomId, Locale locale, GenericValue autoUserLogin) throws Exception {
        init(delegator, dispatcher, productId, productStoreId, catalogId, webSiteId, currencyUomId, locale, autoUserLogin);
    }

    public ProductConfigWrapper(ProductConfigWrapper pcw) {
        product = GenericValue.create(pcw.product);
        listPrice = pcw.listPrice;
        basePrice = pcw.basePrice;
        defaultPrice = pcw.defaultPrice;
        questions = FastList.newInstance();
        dispatcher = pcw.dispatcher;
        productStoreId = pcw.productStoreId;
        catalogId = pcw.catalogId;
        webSiteId = pcw.webSiteId;
        currencyUomId = pcw.currencyUomId;
        delegator = pcw.delegator;
        autoUserLogin = pcw.autoUserLogin;
        for (ConfigItem ci: pcw.questions) {
            questions.add(new ConfigItem(ci));
        }
    }

    private void init(GenericDelegator delegator, LocalDispatcher dispatcher, String productId, String productStoreId, String catalogId, String webSiteId, String currencyUomId, Locale locale, GenericValue autoUserLogin) throws Exception {
        product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        if (product == null || !product.getString("productTypeId").equals("AGGREGATED")) {
            throw new ProductConfigWrapperException("Product " + productId + " is not an AGGREGATED product.");
        }
        this.dispatcher = dispatcher;
        this.productStoreId = productStoreId;
        this.catalogId = catalogId;
        this.webSiteId = webSiteId;
        this.currencyUomId = currencyUomId;
        this.delegator = delegator;
        this.autoUserLogin = autoUserLogin;

        // get the list Price, the base Price
        Map<String, Object> priceContext = UtilMisc.toMap("product", product, "prodCatalogId", catalogId, "webSiteId", webSiteId, "productStoreId", productStoreId,
                                      "currencyUomId", currencyUomId, "autoUserLogin", autoUserLogin);
        Map<String, Object> priceMap = dispatcher.runSync("calculateProductPrice", priceContext);
        BigDecimal originalListPrice = (BigDecimal) priceMap.get("listPrice");
        BigDecimal price = (BigDecimal) priceMap.get("price");
        if (originalListPrice != null) {
            listPrice = originalListPrice;
        }
        if (price != null) {
            basePrice = price;
        }
        questions = FastList.newInstance();
        if (product.getString("productTypeId") != null && product.getString("productTypeId").equals("AGGREGATED")) {
            List<GenericValue> questionsValues = delegator.findByAnd("ProductConfig", UtilMisc.toMap("productId", productId), UtilMisc.toList("sequenceNum"));
            questionsValues = EntityUtil.filterByDate(questionsValues);
            Set<String> itemIds = FastSet.newInstance();
            for (GenericValue questionsValue: questionsValues) {
                ConfigItem oneQuestion = new ConfigItem(questionsValue);
                oneQuestion.setContent(locale, "text/html"); // TODO: mime-type shouldn't be hardcoded
                if (itemIds.contains(oneQuestion.getConfigItem().getString("configItemId"))) {
                    oneQuestion.setFirst(false);
                } else {
                    itemIds.add(oneQuestion.getConfigItem().getString("configItemId"));
                }
                questions.add(oneQuestion);
                List<GenericValue> configOptions = delegator.findByAnd("ProductConfigOption", UtilMisc.toMap("configItemId", oneQuestion.getConfigItemAssoc().getString("configItemId")), UtilMisc.toList("sequenceNum"));
                for (GenericValue configOption: configOptions) {
                    ConfigOption option = new ConfigOption(delegator, dispatcher, configOption, oneQuestion, catalogId, webSiteId, currencyUomId, autoUserLogin);
                    oneQuestion.addOption(option);
                }
            }
            this.setDefaultPrice();
        }
    }

    public void loadConfig(GenericDelegator delegator, String configId) throws Exception {
        //configure ProductConfigWrapper according to ProductConfigConfig entity
        if (UtilValidate.isNotEmpty(configId)) {
            this.configId = configId;
            List<GenericValue> productConfigConfig = delegator.findByAnd("ProductConfigConfig", UtilMisc.toMap("configId", configId));
            if (UtilValidate.isNotEmpty(productConfigConfig)) {
                for (GenericValue pcc: productConfigConfig) {
                    String configItemId = pcc.getString("configItemId");
                    String configOptionId = pcc.getString("configOptionId");
                    Long sequenceNum = pcc.getLong("sequenceNum");
                    String comments = pcc.getString("description");
                    this.setSelected(configItemId, sequenceNum, configOptionId, comments);
                }
            }
        }
    }

    public void setSelected(String configItemId, Long sequenceNum, String configOptionId, String comments) throws Exception {
        for (int i = 0; i < questions.size(); i++) {
            ConfigItem ci = questions.get(i);
            if (ci.configItemAssoc.getString("configItemId").equals(configItemId) && ci.configItemAssoc.getLong("sequenceNum").equals(sequenceNum)) {
                List<ConfigOption> avalOptions = ci.getOptions();
                for (int j = 0; j < avalOptions.size(); j++) {
                    ConfigOption oneOption = avalOptions.get(j);
                    if (oneOption.configOption.getString("configOptionId").equals(configOptionId)) {
                        setSelected(i, j, comments);
                        break;
                    }
                }
            }
        }
    }

    public void resetConfig() {
        for (ConfigItem ci: questions) {
            if (!ci.isStandard()) {
                List<ConfigOption> options = ci.getOptions();
                for (ConfigOption co: options) {
                    co.setSelected(false);
                    co.setComments(null);
                }
            }
        }
    }

    public void setDefaultConfig() {
        resetConfig();
        for (ConfigItem ci: questions) {
            if (ci.isMandatory()) {
                ConfigOption co = ci.getDefault();
                if (co != null) {
                    co.setSelected(true);
                } else if (ci.getOptions().size() > 0) {
                    co = ci.getOptions().get(0);
                    co.setSelected(true);
                }
            }
        }
    }

    public String getConfigId() {
        return configId;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ProductConfigWrapper)) {
            return false;
        }
        ProductConfigWrapper cw = (ProductConfigWrapper)obj;
        if (!product.getString("productId").equals(cw.getProduct().getString("productId"))) {
            return false;
        }
        List<ConfigItem> cwq = cw.getQuestions();
        if (questions.size() != cwq.size()) {
            return false;
        }
        for (int i = 0; i < questions.size(); i++) {
            ConfigItem ci = questions.get(i);
            if (!ci.equals(cwq.get(i))) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return questions.toString();
    }

    public List<ConfigItem> getQuestions() {
        return questions;
    }

    public GenericValue getProduct() {
        return product;
    }

    public void setSelected(int question, int option, String comments) throws Exception {
        ConfigItem ci = questions.get(question);
        List<ConfigOption> avalOptions = ci.getOptions();
        if (ci.isSingleChoice()) {
            for (int j = 0; j < avalOptions.size(); j++) {
                ConfigOption oneOption = (ConfigOption)avalOptions.get(j);
                oneOption.setSelected(false);
                oneOption.setComments(null);
            }
        }
        ConfigOption theOption = null;
        if (option >= 0 && option < avalOptions.size()) {
            theOption = avalOptions.get(option);
        }
        if (theOption != null) {
            theOption.setSelected(true);
            theOption.setComments(comments);
        }
    }

    public void setSelected(int question, int option, int component, String componentOption) throws Exception {
        //  set variant products
        ConfigOption theOption = getItemOtion(question, option);
        List<GenericValue> components = theOption.getComponents();
        GenericValue oneComponent = components.get(component);
        if (theOption.isVirtualComponent(oneComponent)) {
            if (theOption.componentOptions == null) {
                theOption.componentOptions = FastMap.newInstance();
            }
            theOption.componentOptions.put(oneComponent.getString("productId"), componentOption);

            //  recalculate option price
            theOption.recalculateOptionPrice(this);

        }
    }

    public List<ConfigOption> getSelectedOptions() {
        List<ConfigOption> selectedOptions = FastList.newInstance();
        for (ConfigItem ci: questions) {
            if (ci.isStandard()) {
                selectedOptions.addAll(ci.getOptions());
            } else {
                for (ConfigOption oneOption: ci.getOptions()) {
                    if (oneOption.isSelected()) {
                        selectedOptions.add(oneOption);
                    }
                }
            }
        }
        return selectedOptions;
    }

    public List<ConfigOption> getDefaultOptions() {
        List<ConfigOption> defaultOptions = FastList.newInstance();
        for (ConfigItem ci: questions) {
            ConfigOption co = ci.getDefault();
            if (co != null) {
                defaultOptions.add(co);
            }
        }
        return defaultOptions;
    }

    public BigDecimal getTotalListPrice() {
        BigDecimal totalListPrice = listPrice;
        List<ConfigOption> options = getSelectedOptions();
        for (ConfigOption oneOption: options) {
            totalListPrice = totalListPrice.add(oneOption.getListPrice());
        }
        return totalListPrice;
    }

    public BigDecimal getTotalPrice() {
        BigDecimal totalPrice = basePrice;
        List<ConfigOption> options = getSelectedOptions();
        for (ConfigOption oneOption: options) {
            totalPrice = totalPrice.add(oneOption.getPrice());
        }
        return totalPrice;
    }

    private void setDefaultPrice() {
        BigDecimal totalPrice = basePrice;
        List<ConfigOption> options = getDefaultOptions();
        for (ConfigOption oneOption: options) {
            totalPrice = totalPrice.add(oneOption.getPrice());
        }
        defaultPrice = totalPrice;
    }

    public BigDecimal getDefaultPrice() {
        return defaultPrice;
    }

    public boolean isCompleted() {
        boolean completed = true;
        for (ConfigItem ci: questions) {
            if (!ci.isStandard() && ci.isMandatory()) {
                List<ConfigOption> availOptions = ci.getOptions();
                for (ConfigOption oneOption: availOptions) {
                    if (oneOption.isSelected()) {
                        completed = true;
                        break;
                    } else {
                        completed = false;
                    }
                }
                if (!completed) {
                    break;
                }
            }
        }
        return completed;
    }

    public ConfigOption getItemOtion(int itemIndex, int optionIndex) {
        if (questions.size() > itemIndex) {
            ConfigItem ci = questions.get(itemIndex);
            List<ConfigOption> options = ci.getOptions();
            if (options.size() > optionIndex) {
                ConfigOption co = options.get(optionIndex);
                return co;
            }
        }

        return null;
    }

    public class ConfigItem implements java.io.Serializable {
        GenericValue configItem = null;
        GenericValue configItemAssoc = null;
        ProductConfigItemContentWrapper content = null;
        List<ConfigOption> options = null;
        boolean first = true;

        public ConfigItem(GenericValue questionAssoc) throws Exception {
            configItemAssoc = questionAssoc;
            configItem = configItemAssoc.getRelatedOne("ConfigItemProductConfigItem");
            options = FastList.newInstance();
        }

        public ConfigItem(ConfigItem ci) {
            configItem = GenericValue.create(ci.configItem);
            configItemAssoc = GenericValue.create(ci.configItemAssoc);
            options = FastList.newInstance();
            for (ConfigOption co: ci.options) {
                options.add(new ConfigOption(co));
            }
            first = ci.first;
            content = ci.content; // FIXME: this should be cloned
        }

        public void setContent(Locale locale, String mimeTypeId) {
            content = new ProductConfigItemContentWrapper(dispatcher, configItem, locale, mimeTypeId);
        }

        public ProductConfigItemContentWrapper getContent() {
            return content;
        }

        public GenericValue getConfigItem() {
            return configItem;
        }

        public GenericValue getConfigItemAssoc() {
            return configItemAssoc;
        }

        public boolean isStandard() {
            return configItemAssoc.getString("configTypeId").equals("STANDARD");
        }

        public boolean isSingleChoice() {
            return configItem.getString("configItemTypeId").equals("SINGLE");
        }

        public boolean isMandatory() {
            return configItemAssoc.getString("isMandatory") != null && configItemAssoc.getString("isMandatory").equals("Y");
        }

        public boolean isFirst() {
            return first;
        }

        public void setFirst(boolean newValue) {
            first = newValue;
        }

        public void addOption(ConfigOption option) {
            options.add(option);
        }

        public List<ConfigOption> getOptions() {
            return options;
        }

        public String getQuestion() {
            String question = "";
            if (UtilValidate.isNotEmpty(configItemAssoc.getString("description"))) {
                question = configItemAssoc.getString("description");
            } else {
                if (content != null) {
                    question = content.get("DESCRIPTION");
                } else {
                    question = (configItem.getString("description") != null? configItem.getString("description"): "");
                }
            }
            return question;
        }

        public String getDescription() {
            String description = "";
            if (UtilValidate.isNotEmpty(configItemAssoc.getString("longDescription"))) {
                description = configItemAssoc.getString("longDescription");
            } else {
                if (content != null) {
                    description = content.get("LONG_DESCRIPTION");
                } else {
                    description = (configItem.getString("longDescription") != null? configItem.getString("longDescription"): "");
                }
            }
            return description;
        }

        public boolean isSelected() {
            if (isStandard()) return true;
            for (ConfigOption oneOption: getOptions()) {
                if (oneOption.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        public ConfigOption getSelected() {
            for (ConfigOption oneOption: getOptions()) {
                if (oneOption.isSelected()) {
                    return oneOption;
                }
            }
            return null;
        }

        public ConfigOption getDefault() {
            String defaultConfigOptionId = configItemAssoc.getString("defaultConfigOptionId");
            if (UtilValidate.isNotEmpty(defaultConfigOptionId)) {
                for(ConfigOption oneOption : getOptions()) {
                    String currentConfigOptionId = oneOption.getId();
                    if (defaultConfigOptionId.compareToIgnoreCase(currentConfigOptionId) == 0  ) {
                        return oneOption;
                    }
                }
            }
            return null;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ConfigItem)) {
                return false;
            }
            ConfigItem ci = (ConfigItem)obj;
            if (!configItem.getString("configItemId").equals(ci.getConfigItem().getString("configItemId"))) {
                return false;
            }
            List<ConfigOption> opts = ci.getOptions();
            if (options.size() != opts.size()) {
                return false;
            }
            for (int i = 0; i < options.size(); i++) {
                ConfigOption co = options.get(i);
                if (!co.equals(opts.get(i))) {
                    return false;
                }
            }
            return true;
        }

        public String toString() {
            return configItem.getString("configItemId");
        }
    }

    public class ConfigOption implements java.io.Serializable {
        BigDecimal optionListPrice = BigDecimal.ZERO;
        BigDecimal optionPrice = BigDecimal.ZERO;
        Date availabilityDate = null;
        List<GenericValue> componentList = null; // lists of ProductConfigProduct
        Map<String, String> componentOptions = null;
        GenericValue configOption = null;
        boolean selected = false;
        boolean available = true;
        ConfigItem parentConfigItem = null;
        String comments = null;  //  comments for production run entered during ordering

        public ConfigOption(GenericDelegator delegator, LocalDispatcher dispatcher, GenericValue option, ConfigItem configItem, String catalogId, String webSiteId, String currencyUomId, GenericValue autoUserLogin) throws Exception {
            configOption = option;
            parentConfigItem = configItem;
            componentList = option.getRelated("ConfigOptionProductConfigProduct");
            for (GenericValue oneComponent: componentList) {
                BigDecimal listPrice = BigDecimal.ZERO;
                BigDecimal price = BigDecimal.ZERO;
                // Get the component's price
                Map<String, Object> fieldMap = UtilMisc.toMap("product", oneComponent.getRelatedOne("ProductProduct"), "prodCatalogId", catalogId, "webSiteId", webSiteId,
                        "currencyUomId", currencyUomId, "productPricePurposeId", "COMPONENT_PRICE", "autoUserLogin", autoUserLogin);
                Map<String, Object> priceMap = dispatcher.runSync("calculateProductPrice", fieldMap);
                BigDecimal componentListPrice = (BigDecimal) priceMap.get("listPrice");
                BigDecimal componentPrice = (BigDecimal) priceMap.get("price");
                Boolean validPriceFound = (Boolean)priceMap.get("validPriceFound");
                BigDecimal mult = BigDecimal.ONE;
                if (oneComponent.getBigDecimal("quantity") != null) {
                    mult = oneComponent.getBigDecimal("quantity");
                }
                if (mult.compareTo(BigDecimal.ZERO) == 0) {
                    mult = BigDecimal.ONE;
                }
                if (validPriceFound.booleanValue()) {
                    if (componentListPrice != null) {
                        listPrice = componentListPrice;
                    }
                    if (componentPrice != null) {
                        price = componentPrice;
                    }
                } else {
                    fieldMap.put("productPricePurposeId", "PURCHASE");
                    Map<String, Object> purchasePriceResultMap = dispatcher.runSync("calculateProductPrice", fieldMap);
                    BigDecimal purchaseListPrice = (BigDecimal) purchasePriceResultMap.get("listPrice");
                    BigDecimal purchasePrice = (BigDecimal) purchasePriceResultMap.get("price");
                    if (purchaseListPrice != null) {
                        listPrice = purchaseListPrice;
                    }
                    if (purchasePrice != null) {
                        price = purchasePrice;
                    }
                }
                optionListPrice = optionListPrice.add(listPrice.multiply(mult));
                optionPrice = optionPrice.add(price.multiply(mult));
                // TODO: get the component's availability date
            }
        }

        public ConfigOption(ConfigOption co) {
            configOption = GenericValue.create(co.configOption);
            componentList = FastList.newInstance();
            for (GenericValue component: co.componentList) {
                componentList.add(GenericValue.create(component));
            }
            optionListPrice = co.optionListPrice;
            optionPrice = co.optionPrice;
            available = co.available;
            selected = co.selected;
            comments = co.getComments();
        }

        public void recalculateOptionPrice(ProductConfigWrapper pcw) throws Exception {
            optionListPrice = BigDecimal.ZERO;
            optionPrice = BigDecimal.ZERO;
            for (GenericValue oneComponent: componentList) {
                BigDecimal listPrice = BigDecimal.ZERO;
                BigDecimal price = BigDecimal.ZERO;
                GenericValue oneComponentProduct = oneComponent.getRelatedOne("ProductProduct");
                String variantProductId = componentOptions.get(oneComponent.getString("productId"));

                if (UtilValidate.isNotEmpty(variantProductId)) {
                    oneComponentProduct = pcw.delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", variantProductId));
                }

                // Get the component's price
                Map<String, Object> fieldMap = UtilMisc.toMap("product", oneComponentProduct, "prodCatalogId", pcw.catalogId, "webSiteId", pcw.webSiteId,
                        "currencyUomId", pcw.currencyUomId, "productPricePurposeId", "COMPONENT_PRICE", "autoUserLogin", pcw.autoUserLogin);
                Map<String, Object> priceMap = dispatcher.runSync("calculateProductPrice", fieldMap);
                BigDecimal componentListPrice = (BigDecimal) priceMap.get("listPrice");
                BigDecimal componentPrice = (BigDecimal) priceMap.get("price");
                Boolean validPriceFound = (Boolean)priceMap.get("validPriceFound");
                BigDecimal mult = BigDecimal.ONE;
                if (oneComponent.getBigDecimal("quantity") != null) {
                    mult = oneComponent.getBigDecimal("quantity");
                }
                if (mult.compareTo(BigDecimal.ZERO) == 0) {
                    mult = BigDecimal.ONE;
                }
                if (validPriceFound.booleanValue()) {
                    if (componentListPrice != null) {
                        listPrice = componentListPrice;
                    }
                    if (componentPrice != null) {
                        price = componentPrice;
                    }
                } else {
                    fieldMap.put("productPricePurposeId", "PURCHASE");
                    Map<String, Object> purchasePriceResultMap = dispatcher.runSync("calculateProductPrice", fieldMap);
                    BigDecimal purchaseListPrice = (BigDecimal) purchasePriceResultMap.get("listPrice");
                    BigDecimal purchasePrice = (BigDecimal) purchasePriceResultMap.get("price");
                    if (purchaseListPrice != null) {
                        listPrice = purchaseListPrice;
                    }
                    if (purchasePrice != null) {
                        price = purchasePrice;
                    }
                }
                optionListPrice = optionListPrice.add(listPrice.multiply(mult));
                optionPrice = optionPrice.add(price.multiply(mult));
            }
        }

        public String getDescription() {
            return (configOption.getString("description") != null? configOption.getString("description"): "no description");
        }
        
        public String getDescription(Locale locale) {
            return (configOption.getString("description") != null? (String) configOption.get("description", locale): "no description");
        }        

        public String getId() {
            return configOption.getString("configOptionId");
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public BigDecimal getListPrice() {
            return optionListPrice;
        }

        public BigDecimal getPrice() {
            return optionPrice;
        }

        public BigDecimal getOffsetListPrice() {
            ConfigOption defaultConfigOption = parentConfigItem.getDefault();
            if (parentConfigItem.isSingleChoice() && UtilValidate.isNotEmpty(defaultConfigOption)) {
                return optionListPrice.subtract(defaultConfigOption.getListPrice());
            } else {  // can select multiple or no default; show full price
                return optionListPrice;
            }
        }

        public BigDecimal getOffsetPrice() {
            ConfigOption defaultConfigOption = parentConfigItem.getDefault();
            if (parentConfigItem.isSingleChoice() && UtilValidate.isNotEmpty(defaultConfigOption)) {
                return optionPrice.subtract(defaultConfigOption.getPrice());
            } else {  // can select multiple or no default; show full price
                return optionPrice;
            }
        }

        public boolean isDefault() {
            ConfigOption defaultConfigOption = parentConfigItem.getDefault();
            if (this.equals(defaultConfigOption)) {
                return true;
            }
            return false;
        }

        public boolean hasVirtualComponent () {
           List <GenericValue> components = getComponents();
           if (UtilValidate.isNotEmpty(components)) {
               for (GenericValue component : components) {
                   if (isVirtualComponent(component)) {
                       return true;
                   }
               }
           }

           return false;
       }

        public boolean isVirtualComponent (GenericValue component) {
            int index = getComponents().indexOf(component);
            if (index != -1) {
                try {
                    GenericValue product = component.getRelatedOne("ProductProduct");
                    return "Y".equals(product.getString("isVirtual"));
                } catch (GenericEntityException e) {
                    Debug.logWarning(e.getMessage(), module);
                }
            }
            return false;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean newValue) {
            selected = newValue;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean newValue) {
            available = newValue;
        }

        public List<GenericValue> getComponents() {
            return componentList;
        }

        public Map<String, String> getComponentOptions() {
            return componentOptions;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof ConfigOption)) {
                return false;
            }
            ConfigOption co = (ConfigOption)obj;
            if (componentOptions != null && !componentOptions.equals(co.getComponentOptions())) {
                return false;
            }

            return isSelected() == co.isSelected();
        }

        public String toString() {
            return configOption.getString("configItemId") + "/" + configOption.getString("configOptionId") + (isSelected()? "*": "");
        }

    }

}
