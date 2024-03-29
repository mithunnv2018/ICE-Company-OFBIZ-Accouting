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
package org.ofbiz.product.product;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.PageContext;

import javolution.util.FastList;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.common.geo.GeoWorker;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.product.config.ProductConfigWrapper;
import org.ofbiz.product.config.ProductConfigWrapper.ConfigOption;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;

/**
 * Product Worker class to reduce code in JSPs.
 */
public class ProductWorker {

    public static final String module = ProductWorker.class.getName();
    public static final String resource = "ProductUiLabels";

    public static final MathContext generalRounding = new MathContext(10);

    /** @deprecated */
    public static void getProduct(PageContext pageContext, String attributeName) {
        getProduct(pageContext, attributeName, null);
    }

    public static boolean shippingApplies(GenericValue product) {
        String errMsg = "";
        if (product != null) {
            String productTypeId = product.getString("productTypeId");
            if ("SERVICE".equals(productTypeId) || (ProductWorker.isDigital(product) && !ProductWorker.isPhysical(product))) {
                // don't charge shipping on services or digital goods
                return false;
            }
            Boolean chargeShipping = product.getBoolean("chargeShipping");

            if (chargeShipping == null) {
                return true;
            } else {
                return chargeShipping.booleanValue();
            }
        } else {
            throw new IllegalArgumentException(errMsg);
        }
    }

    public static boolean isBillableToAddress(GenericValue product, GenericValue postalAddress) {
        return isAllowedToAddress(product, postalAddress, "PG_PURCH_");
    }
    public static boolean isShippableToAddress(GenericValue product, GenericValue postalAddress) {
        return isAllowedToAddress(product, postalAddress, "PG_SHIP_");
    }
    private static boolean isAllowedToAddress(GenericValue product, GenericValue postalAddress, String productGeoPrefix) {
        if (UtilValidate.isNotEmpty(product) && UtilValidate.isNotEmpty(postalAddress)) {
            GenericDelegator delegator = product.getDelegator();
            List<GenericValue> productGeos = null;
            try {
                productGeos = product.getRelated("ProductGeo");
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
            }
            List<GenericValue> excludeGeos = EntityUtil.filterByAnd(productGeos, UtilMisc.toMap("productGeoEnumId", productGeoPrefix + "EXCLUDE"));
            List<GenericValue> includeGeos = EntityUtil.filterByAnd(productGeos, UtilMisc.toMap("productGeoEnumId", productGeoPrefix + "INCLUDE"));
            if (UtilValidate.isEmpty(excludeGeos) && UtilValidate.isEmpty(includeGeos)) {
                // If no GEOs are configured the default is TRUE
                return true;
            }
            // exclusion
            for (GenericValue productGeo: excludeGeos) {
                List<GenericValue> excludeGeoGroup = GeoWorker.expandGeoGroup(productGeo.getString("geoId"), delegator);
                if (GeoWorker.containsGeo(excludeGeoGroup, postalAddress.getString("countryGeoId"), delegator) ||
                      GeoWorker.containsGeo(excludeGeoGroup, postalAddress.getString("stateProvinceGeoId"), delegator) ||
                      GeoWorker.containsGeo(excludeGeoGroup, postalAddress.getString("postalCodeGeoId"), delegator)) {
                    return false;
                }
            }
            if (UtilValidate.isEmpty(includeGeos)) {
                // If no GEOs are configured the default is TRUE
                return true;
            }
            // inclusion
            for (GenericValue productGeo: includeGeos) {
                List<GenericValue> includeGeoGroup = GeoWorker.expandGeoGroup(productGeo.getString("geoId"), delegator);
                if (GeoWorker.containsGeo(includeGeoGroup, postalAddress.getString("countryGeoId"), delegator) ||
                      GeoWorker.containsGeo(includeGeoGroup, postalAddress.getString("stateProvinceGeoId"), delegator) ||
                      GeoWorker.containsGeo(includeGeoGroup, postalAddress.getString("postalCodeGeoId"), delegator)) {
                    return true;
                }
            }

        } else {
            throw new IllegalArgumentException("product and postalAddress cannot be null.");
        }
        return false;
    }

    public static boolean taxApplies(GenericValue product) {
        String errMsg = "";
        if (product != null) {
            Boolean taxable = product.getBoolean("taxable");

            if (taxable == null) {
                return true;
            } else {
                return taxable.booleanValue();
            }
        } else {
            throw new IllegalArgumentException(errMsg);
        }
    }

    /** @deprecated */
    public static void getProduct(PageContext pageContext, String attributeName, String productId) {
        GenericDelegator delegator = (GenericDelegator) pageContext.getRequest().getAttribute("delegator");
        ServletRequest request = pageContext.getRequest();

        if (productId == null)
            productId = UtilFormatOut.checkNull(request.getParameter("product_id"), request.getParameter("PRODUCT_ID"));

        if (productId.equals(""))
            return;

        GenericValue product = null;

        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
            product = null;
        }
        if (product != null)
            pageContext.setAttribute(attributeName, product);
    }

    public static String getInstanceAggregatedId(GenericDelegator delegator, String instanceProductId) throws GenericEntityException {
        GenericValue instanceProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", instanceProductId));

        if (UtilValidate.isNotEmpty(instanceProduct) && "AGGREGATED_CONF".equals(instanceProduct.getString("productTypeId"))) {
            GenericValue productAssoc = EntityUtil.getFirst(EntityUtil.filterByDate(instanceProduct.getRelatedByAnd("AssocProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_CONF"))));
            if (UtilValidate.isNotEmpty(productAssoc)) {
                return productAssoc.getString("productId");
            }
        }
        return null;
    }

    public static String getAggregatedInstanceId(GenericDelegator delegator, String  aggregatedProductId, String configId) throws GenericEntityException {
        List<GenericValue> productAssocs = getAggregatedAssocs(delegator, aggregatedProductId);
        if (UtilValidate.isNotEmpty(productAssocs) && UtilValidate.isNotEmpty(configId)) {
            for (GenericValue productAssoc: productAssocs) {
                GenericValue product = productAssoc.getRelatedOne("AssocProduct");
                if (configId.equals(product.getString("configId"))) {
                    return productAssoc.getString("productIdTo");
                }
            }
        }
        return null;
    }

    public static List<GenericValue> getAggregatedAssocs(GenericDelegator delegator, String  aggregatedProductId) throws GenericEntityException {
        GenericValue aggregatedProduct = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", aggregatedProductId));

        if (UtilValidate.isNotEmpty(aggregatedProduct) && "AGGREGATED".equals(aggregatedProduct.getString("productTypeId"))) {
            List<GenericValue> productAssocs = EntityUtil.filterByDate(aggregatedProduct.getRelatedByAnd("MainProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_CONF")));
            return productAssocs;
        }
        return null;
    }

    public static String getVariantVirtualId(GenericValue variantProduct) throws GenericEntityException {
        List<GenericValue> productAssocs = getVariantVirtualAssocs(variantProduct);
        if (productAssocs == null) {
            return null;
        }
        GenericValue productAssoc = EntityUtil.getFirst(productAssocs);
        if (productAssoc != null) {
            return productAssoc.getString("productId");
        } else {
            return null;
        }
    }

    public static List<GenericValue> getVariantVirtualAssocs(GenericValue variantProduct) throws GenericEntityException {
        if (variantProduct != null && "Y".equals(variantProduct.getString("isVariant"))) {
            List<GenericValue> productAssocs = EntityUtil.filterByDate(variantProduct.getRelatedByAndCache("AssocProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_VARIANT")));
            return productAssocs;
        }
        return null;
    }

    /**
     * invokes the getInventoryAvailableByFacility service, returns true if specified quantity is available, else false
     * this is only used in the related method that uses a ProductConfigWrapper, until that is refactored into a service as well...
     */
    private static boolean isProductInventoryAvailableByFacility(String productId, String inventoryFacilityId, BigDecimal quantity, LocalDispatcher dispatcher) throws GenericServiceException {
        BigDecimal availableToPromise = null;

        try {
            Map<String, Object> result = dispatcher.runSync("getInventoryAvailableByFacility",
                                            UtilMisc.toMap("productId", productId, "facilityId", inventoryFacilityId));

            availableToPromise = (BigDecimal) result.get("availableToPromiseTotal");

            if (availableToPromise == null) {
                Debug.logWarning("The getInventoryAvailableByFacility service returned a null availableToPromise, the error message was:\n" + result.get(ModelService.ERROR_MESSAGE), module);
                return false;
            }
        } catch (GenericServiceException e) {
            Debug.logWarning(e, "Error invoking getInventoryAvailableByFacility service in isCatalogInventoryAvailable", module);
            return false;
        }

        // check to see if we got enough back...
        if (availableToPromise.compareTo(quantity) >= 0) {
            if (Debug.infoOn()) Debug.logInfo("Inventory IS available in facility with id " + inventoryFacilityId + " for product id " + productId + "; desired quantity is " + quantity + ", available quantity is " + availableToPromise, module);
            return true;
        } else {
            if (Debug.infoOn()) Debug.logInfo("Returning false because there is insufficient inventory available in facility with id " + inventoryFacilityId + " for product id " + productId + "; desired quantity is " + quantity + ", available quantity is " + availableToPromise, module);
            return false;
        }
    }

    /**
     * Invokes the getInventoryAvailableByFacility service, returns true if specified quantity is available for all the selected parts, else false.
     * Also, set the available flag for all the product configuration's options.
     **/
    public static boolean isProductInventoryAvailableByFacility(ProductConfigWrapper productConfig, String inventoryFacilityId, BigDecimal quantity, LocalDispatcher dispatcher) throws GenericServiceException {
        boolean available = true;
        List<ConfigOption> options = productConfig.getSelectedOptions();
        for (ConfigOption ci: options) {
            List<GenericValue> products = ci.getComponents();
            for (GenericValue product: products) {
                String productId = product.getString("productId");
                BigDecimal cmpQuantity = product.getBigDecimal("quantity");
                BigDecimal neededQty = BigDecimal.ZERO;
                if (cmpQuantity != null) {
                    neededQty = quantity.multiply(cmpQuantity);
                }
                if (!isProductInventoryAvailableByFacility(productId, inventoryFacilityId, neededQty, dispatcher)) {
                    ci.setAvailable(false);
                }
            }
            if (!ci.isAvailable()) {
                available = false;
            }
        }
        return available;
    }

    /** @deprecated */
    public static void getAssociatedProducts(PageContext pageContext, String productAttributeName, String assocPrefix) {
        GenericValue product = (GenericValue) pageContext.getAttribute(productAttributeName);

        if (product == null)
            return;

        try {
            List<GenericValue> upgradeProducts = product.getRelatedByAndCache("MainProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_UPGRADE"));

            List<GenericValue> complementProducts = product.getRelatedByAndCache("MainProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_COMPLEMENT"));

            List<GenericValue> obsolescenceProducts = product.getRelatedByAndCache("AssocProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_OBSOLESCENCE"));

            List<GenericValue> obsoleteByProducts = product.getRelatedByAndCache("MainProductAssoc",
                    UtilMisc.toMap("productAssocTypeId", "PRODUCT_OBSOLESCENCE"));

            // since ProductAssoc records have a fromDate and thruDate, we can filter by now so that only assocs in the date range are included
            upgradeProducts = EntityUtil.filterByDate(upgradeProducts);
            complementProducts = EntityUtil.filterByDate(complementProducts);
            obsolescenceProducts = EntityUtil.filterByDate(obsolescenceProducts);
            obsoleteByProducts = EntityUtil.filterByDate(obsoleteByProducts);

            if (upgradeProducts != null && upgradeProducts.size() > 0)
                pageContext.setAttribute(assocPrefix + "upgrade", upgradeProducts);
            if (complementProducts != null && complementProducts.size() > 0)
                pageContext.setAttribute(assocPrefix + "complement", complementProducts);
            if (obsolescenceProducts != null && obsolescenceProducts.size() > 0)
                pageContext.setAttribute(assocPrefix + "obsolescence", obsolescenceProducts);
            if (obsoleteByProducts != null && obsoleteByProducts.size() > 0)
                pageContext.setAttribute(assocPrefix + "obsoleteby", obsoleteByProducts);
        } catch (GenericEntityException e) {
            Debug.logWarning(e, module);
        }
    }

    /**
     * Gets ProductFeature GenericValue for all distinguishing features of a variant product.
     * Distinguishing means all features that are selectable on the corresponding virtual product and standard on the variant plus all DISTINGUISHING_FEAT assoc type features on the variant.
     */
    public static Set<GenericValue> getVariantDistinguishingFeatures(GenericValue variantProduct) throws GenericEntityException {
        if (variantProduct == null) {
            return FastSet.newInstance();
        }
        if (!"Y".equals(variantProduct.getString("isVariant"))) {
            throw new IllegalArgumentException("Cannot get distinguishing features for a product that is not a variant (ie isVariant!=Y).");
        }
        GenericDelegator delegator = variantProduct.getDelegator();
        String virtualProductId = getVariantVirtualId(variantProduct);

        // find all selectable features on the virtual product that are also standard features on the variant
        Set<GenericValue> distFeatures = FastSet.newInstance();

        List<GenericValue> variantDistinguishingFeatures = delegator.findByAndCache("ProductFeatureAndAppl", UtilMisc.toMap("productId", variantProduct.get("productId"), "productFeatureApplTypeId", "DISTINGUISHING_FEAT"));
        // Debug.logInfo("Found variantDistinguishingFeatures: " + variantDistinguishingFeatures, module);

        for (GenericValue variantDistinguishingFeature: EntityUtil.filterByDate(variantDistinguishingFeatures)) {
            GenericValue dummyFeature = delegator.makeValue("ProductFeature");
            dummyFeature.setAllFields(variantDistinguishingFeature, true, null, null);
            distFeatures.add(dummyFeature);
        }

        List<GenericValue> virtualSelectableFeatures = delegator.findByAndCache("ProductFeatureAndAppl", UtilMisc.toMap("productId", virtualProductId, "productFeatureApplTypeId", "SELECTABLE_FEATURE"));
        // Debug.logInfo("Found virtualSelectableFeatures: " + virtualSelectableFeatures, module);

        Set<String> virtualSelectableFeatureIds = FastSet.newInstance();
        for (GenericValue virtualSelectableFeature: EntityUtil.filterByDate(virtualSelectableFeatures)) {
            virtualSelectableFeatureIds.add(virtualSelectableFeature.getString("productFeatureId"));
        }

        List<GenericValue> variantStandardFeatures = delegator.findByAndCache("ProductFeatureAndAppl", UtilMisc.toMap("productId", variantProduct.get("productId"), "productFeatureApplTypeId", "STANDARD_FEATURE"));
        // Debug.logInfo("Found variantStandardFeatures: " + variantStandardFeatures, module);

        for (GenericValue variantStandardFeature: EntityUtil.filterByDate(variantStandardFeatures)) {
            if (virtualSelectableFeatureIds.contains(variantStandardFeature.get("productFeatureId"))) {
                GenericValue dummyFeature = delegator.makeValue("ProductFeature");
                dummyFeature.setAllFields(variantStandardFeature, true, null, null);
                distFeatures.add(dummyFeature);
            }
        }

        return distFeatures;
    }

    /**
     *  Get the name to show to the customer for GWP alternative options.
     *  If the alternative is a variant, find the distinguishing features and show those instead of the name; if it is not a variant then show the PRODUCT_NAME content.
     */
    public static String getGwpAlternativeOptionName(LocalDispatcher dispatcher, GenericDelegator delegator, String alternativeOptionProductId, Locale locale) {
        try {
            GenericValue alternativeOptionProduct = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", alternativeOptionProductId));
            if (alternativeOptionProduct != null) {
                if ("Y".equals(alternativeOptionProduct.getString("isVariant"))) {
                    Set<GenericValue> distFeatures = getVariantDistinguishingFeatures(alternativeOptionProduct);
                    if (UtilValidate.isNotEmpty(distFeatures)) {
                        // Debug.logInfo("Found distinguishing features: " + distFeatures, module);

                        StringBuilder nameBuf = new StringBuilder();
                        for (GenericValue productFeature: distFeatures) {
                            if (nameBuf.length() > 0) {
                                nameBuf.append(", ");
                            }
                            GenericValue productFeatureType = productFeature.getRelatedOneCache("ProductFeatureType");
                            if (productFeatureType != null) {
                                nameBuf.append(productFeatureType.get("description", locale));
                                nameBuf.append(":");
                            }
                            nameBuf.append(productFeature.get("description", locale));
                        }
                        return nameBuf.toString();
                    }
                }

                // got to here, default to PRODUCT_NAME
                String alternativeProductName = ProductContentWrapper.getProductContentAsText(alternativeOptionProduct, "PRODUCT_NAME", locale, dispatcher);
                // Debug.logInfo("Using PRODUCT_NAME: " + alternativeProductName, module);
                return alternativeProductName;
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        } catch (Exception e) {
            Debug.logError(e, module);
        }
        // finally fall back to the ID in square braces
        return "[" + alternativeOptionProductId + "]";
    }

    /**
     * gets productFeatures given a productFeatureApplTypeId
     * @param delegator
     * @param productId
     * @param productFeatureApplTypeId - if null, returns ALL productFeatures, regardless of applType
     * @return List
     */
    public static List<GenericValue> getProductFeaturesByApplTypeId(GenericDelegator delegator, String productId, String productFeatureApplTypeId) {
        if (productId == null) {
            return null;
        }
        try {
            return getProductFeaturesByApplTypeId(delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId)),
                    productFeatureApplTypeId);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return null;
    }

    public static List<GenericValue> getProductFeaturesByApplTypeId(GenericValue product, String productFeatureApplTypeId) {
        if (product == null) {
            return null;
        }
        List<GenericValue> features = FastList.newInstance();
        try {
            if (product != null) {
                List<GenericValue> productAppls;
                if (productFeatureApplTypeId == null) {
                    productAppls = product.getRelated("ProductFeatureAppl");
                } else {
                    productAppls = product.getRelatedByAnd("ProductFeatureAppl",
                            UtilMisc.toMap("productFeatureApplTypeId", productFeatureApplTypeId));
                }
                for (GenericValue productAppl: productAppls) {
                    features.add(productAppl.getRelatedOne("ProductFeature"));
                }
                features = EntityUtil.orderBy(features, UtilMisc.toList("description"));
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return features;
    }

    public static String getProductvirtualVariantMethod(GenericDelegator delegator, String productId) {
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        if (product != null) {
            return product.getString("virtualVariantMethodEnum");
        } else {
            return null;
        }
    }

    /**
     *
     * @param product
     * @return list featureType and related featuresIds, description and feature price for this product ordered by type and sequence
     */
    public static List<List<Map<String,String>>> getSelectableProductFeaturesByTypesAndSeq(GenericValue product) {
        if (product == null) {
            return null;
        }
        List <List<Map<String,String>>> featureTypeFeatures = FastList.newInstance();
        try {
            if (product != null) {
                GenericDelegator delegator = product.getDelegator();
                Map<String,String> fields = UtilMisc.toMap("productId", product.getString("productId"), "productFeatureApplTypeId", "SELECTABLE_FEATURE");
                List<String> order = UtilMisc.toList("productFeatureTypeId", "sequenceNum");
                List<GenericValue> features = delegator.findByAndCache("ProductFeatureAndAppl", fields, order);
                List<GenericValue> featuresSorted = EntityUtil.orderBy(features, order);
                String oldType = null;
                List<Map<String,String>> featureList = FastList.newInstance();
                for (GenericValue productFeatureAppl: featuresSorted) {
                    if (oldType == null || !oldType.equals(productFeatureAppl.getString("productFeatureTypeId"))) {
                        // use first entry for type and description
                        if (oldType != null) {
                            featureTypeFeatures.add(featureList);
                            featureList = FastList.newInstance();
                            }
                        GenericValue productFeatureType = delegator.findByPrimaryKey("ProductFeatureType", UtilMisc.toMap("productFeatureTypeId",
                                productFeatureAppl.getString("productFeatureTypeId")));
                        featureList.add(UtilMisc.<String, String>toMap("productFeatureTypeId", productFeatureAppl.getString("productFeatureTypeId"),
                                                        "description", productFeatureType.getString("description")));
                        oldType = productFeatureAppl.getString("productFeatureTypeId");
                    }
                    // fill other entries with featureId, description and default price and currency
                    Map<String,String> featureData = UtilMisc.toMap("productFeatureId", productFeatureAppl.getString("productFeatureId"));
                    if (UtilValidate.isNotEmpty(productFeatureAppl.get("description"))) {
                        featureData.put("description", productFeatureAppl.getString("description"));
                    } else {
                        featureData.put("description", productFeatureAppl.getString("productFeatureId"));
                    }
                    List<GenericValue> productFeaturePrices = EntityUtil.filterByDate(delegator.findByAnd("ProductFeaturePrice",
                            UtilMisc.toMap("productFeatureId", productFeatureAppl.getString("productFeatureId"), "productPriceTypeId", "DEFAULT_PRICE")));
                    if (UtilValidate.isNotEmpty(productFeaturePrices)) {
                        GenericValue productFeaturePrice = productFeaturePrices.get(0);
                        if (UtilValidate.isNotEmpty(productFeaturePrice.get("price"))) {
                            featureData.put("price", productFeaturePrice.getBigDecimal("price").toString());
                            featureData.put("currencyUomId", productFeaturePrice.getString("currencyUomId"));
                        }
                    }
                    featureList.add(featureData);
                }
                if (oldType != null) {
                    // last map
                    featureTypeFeatures.add(featureList);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return featureTypeFeatures;
    }

    public static Map<String, List<GenericValue>> getOptionalProductFeatures(GenericDelegator delegator, String productId) {
        Map<String, List<GenericValue>> featureMap = new LinkedHashMap<String, List<GenericValue>>();

        List<GenericValue> productFeatureAppls = null;
        try {
            productFeatureAppls = delegator.findByAnd("ProductFeatureAndAppl", UtilMisc.toMap("productId", productId, "productFeatureApplTypeId", "OPTIONAL_FEATURE"), UtilMisc.toList("productFeatureTypeId", "sequenceNum"));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        if (productFeatureAppls != null) {
            for (GenericValue appl: productFeatureAppls) {
                String featureType = appl.getString("productFeatureTypeId");
                List<GenericValue> features = featureMap.get(featureType);
                if (features == null) {
                    features = FastList.newInstance();
                }
                features.add(appl);
                featureMap.put(featureType, features);
            }
        }

        return featureMap;
    }

    // product calc methods

    public static BigDecimal calcOrderAdjustments(List<GenericValue> orderHeaderAdjustments, BigDecimal subTotal, boolean includeOther, boolean includeTax, boolean includeShipping) {
        BigDecimal adjTotal = BigDecimal.ZERO;

        if (UtilValidate.isNotEmpty(orderHeaderAdjustments)) {
            List<GenericValue> filteredAdjs = filterOrderAdjustments(orderHeaderAdjustments, includeOther, includeTax, includeShipping, false, false);
            for (GenericValue orderAdjustment: filteredAdjs) {
                adjTotal = adjTotal.add(calcOrderAdjustment(orderAdjustment, subTotal));
            }
        }
        return adjTotal;
    }

    public static BigDecimal calcOrderAdjustment(GenericValue orderAdjustment, BigDecimal orderSubTotal) {
        BigDecimal adjustment = BigDecimal.ZERO;

        if (orderAdjustment.get("amount") != null) {
            adjustment = adjustment.add(orderAdjustment.getBigDecimal("amount"));
        }
        else if (orderAdjustment.get("sourcePercentage") != null) {
            adjustment = adjustment.add(orderAdjustment.getBigDecimal("sourcePercentage").multiply(orderSubTotal));
        }
        return adjustment;
    }

    public static List<GenericValue> filterOrderAdjustments(List<GenericValue> adjustments, boolean includeOther, boolean includeTax, boolean includeShipping, boolean forTax, boolean forShipping) {
        List<GenericValue> newOrderAdjustmentsList = FastList.newInstance();

        if (UtilValidate.isNotEmpty(adjustments)) {
            for (GenericValue orderAdjustment: adjustments) {
                boolean includeAdjustment = false;

                if ("SALES_TAX".equals(orderAdjustment.getString("orderAdjustmentTypeId"))) {
                    if (includeTax) includeAdjustment = true;
                } else if ("SHIPPING_CHARGES".equals(orderAdjustment.getString("orderAdjustmentTypeId"))) {
                    if (includeShipping) includeAdjustment = true;
                } else {
                    if (includeOther) includeAdjustment = true;
                }

                // default to yes, include for shipping; so only exclude if includeInShipping is N, or false; if Y or null or anything else it will be included
                if (forTax && "N".equals(orderAdjustment.getString("includeInTax"))) {
                    includeAdjustment = false;
                }

                // default to yes, include for shipping; so only exclude if includeInShipping is N, or false; if Y or null or anything else it will be included
                if (forShipping && "N".equals(orderAdjustment.getString("includeInShipping"))) {
                    includeAdjustment = false;
                }

                if (includeAdjustment) {
                    newOrderAdjustmentsList.add(orderAdjustment);
                }
            }
        }
        return newOrderAdjustmentsList;
    }

    public static BigDecimal getAverageProductRating(GenericDelegator delegator, String productId) {
        return getAverageProductRating(delegator, productId, null);
    }

    public static BigDecimal getAverageProductRating(GenericDelegator delegator, String productId, String productStoreId) {
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return ProductWorker.getAverageProductRating(product, productStoreId);
    }

    public static BigDecimal getAverageProductRating(GenericValue product, String productStoreId) {
        return getAverageProductRating(product, null, productStoreId);
    }

    public static BigDecimal getAverageProductRating(GenericValue product, List<GenericValue> reviews, String productStoreId) {
        if (product == null) {
            Debug.logWarning("Invalid product entity passed; unable to obtain valid product rating", module);
            return BigDecimal.ZERO;
        }

        BigDecimal productRating = BigDecimal.ZERO;
        BigDecimal productEntityRating = product.getBigDecimal("productRating");
        String entityFieldType = product.getString("ratingTypeEnum");

        // null check
        if (productEntityRating == null) {
            productEntityRating = BigDecimal.ZERO;
        }
        if (entityFieldType == null) {
            entityFieldType = "";
        }

        if ("PRDR_FLAT".equals(entityFieldType)) {
            productRating = productEntityRating;
        } else {
            // get the product rating from the ProductReview entity; limit by product store if ID is passed
            Map<String, String> reviewByAnd = UtilMisc.toMap("statusId", "PRR_APPROVED");
            if (productStoreId != null) {
                reviewByAnd.put("productStoreId", productStoreId);
            }

            // lookup the reviews if we didn't pass them in
            if (reviews == null) {
                try {
                    reviews = product.getRelatedCache("ProductReview", reviewByAnd, UtilMisc.toList("-postedDateTime"));
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                }
            }

            // tally the average
            BigDecimal ratingTally = BigDecimal.ZERO;
            BigDecimal numRatings = BigDecimal.ZERO;
            if (reviews != null) {
                for (GenericValue productReview: reviews) {
                    BigDecimal rating = productReview.getBigDecimal("productRating");
                    if (rating != null) {
                        ratingTally = ratingTally.add(rating);
                        numRatings.add(BigDecimal.ONE);
                    }
                }
            }
            if (ratingTally.compareTo(BigDecimal.ZERO) > 0 && numRatings.compareTo(BigDecimal.ZERO) > 0) {
                productRating = ratingTally.divide(numRatings, generalRounding);
            }

            if ("PRDR_MIN".equals(entityFieldType)) {
                // check for min
                if (productEntityRating.compareTo(productRating) > 0) {
                    productRating = productEntityRating;
                }
            } else if ("PRDR_MAX".equals(entityFieldType)) {
                // check for max
                if (productRating.compareTo(productEntityRating) > 0) {
                    productRating = productEntityRating;
                }
            }
        }

        return productRating;
    }

    public static List<GenericValue> getCurrentProductCategories(GenericDelegator delegator, String productId) {
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return getCurrentProductCategories(delegator, product);
    }

    public static List<GenericValue> getCurrentProductCategories(GenericDelegator delegator, GenericValue product) {
        if (product == null) {
            return null;
        }
        List<GenericValue> categories = FastList.newInstance();
        try {
            List<GenericValue> categoryMembers = product.getRelated("ProductCategoryMember");
            categoryMembers = EntityUtil.filterByDate(categoryMembers);
            categories = EntityUtil.getRelated("ProductCategory", categoryMembers);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }
        return categories;
    }

    //get parent product
    public static GenericValue getParentProduct(String productId, GenericDelegator delegator) {
        GenericValue _parentProduct = null;
        if (productId == null) {
            Debug.logWarning("Bad product id", module);
        }

        try {
            List<GenericValue> virtualProductAssocs = delegator.findByAndCache("ProductAssoc", UtilMisc.toMap("productIdTo", productId, "productAssocTypeId", "PRODUCT_VARIANT"), UtilMisc.toList("-fromDate"));
            virtualProductAssocs = EntityUtil.filterByDate(virtualProductAssocs);
            if (UtilValidate.isEmpty(virtualProductAssocs)) {
                //okay, not a variant, try a UNIQUE_ITEM
                virtualProductAssocs = delegator.findByAndCache("ProductAssoc", UtilMisc.toMap("productIdTo", productId, "productAssocTypeId", "UNIQUE_ITEM"), UtilMisc.toList("-fromDate"));
                virtualProductAssocs = EntityUtil.filterByDate(virtualProductAssocs);
            }
            if (UtilValidate.isNotEmpty(virtualProductAssocs)) {
                //found one, set this first as the parent product
                GenericValue productAssoc = EntityUtil.getFirst(virtualProductAssocs);
                _parentProduct = productAssoc.getRelatedOneCache("MainProduct");
            }
        } catch (GenericEntityException e) {
            throw new RuntimeException("Entity Engine error getting Parent Product (" + e.getMessage() + ")");
        }
        return _parentProduct;
    }

    public static boolean isDigital(GenericValue product) {
        boolean isDigital = false;
        if (product != null) {
            GenericValue productType = null;
            try {
                productType = product.getRelatedOneCache("ProductType");
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
            }
            String isDigitalValue = (productType != null? productType.getString("isDigital"): null);
            isDigital = isDigitalValue != null && "Y".equalsIgnoreCase(isDigitalValue);
        }
        return isDigital;
    }

    public static boolean isPhysical(GenericValue product) {
        boolean isPhysical = false;
        if (product != null) {
            GenericValue productType = null;
            try {
                productType = product.getRelatedOneCache("ProductType");
            } catch (GenericEntityException e) {
                Debug.logWarning(e.getMessage(), module);
            }
            String isPhysicalValue = (productType != null? productType.getString("isPhysical"): null);
            isPhysical = isPhysicalValue != null && "Y".equalsIgnoreCase(isPhysicalValue);
        }
        return isPhysical;
    }

    public static boolean isVirtual(GenericDelegator delegator, String productI) {
        try {
            GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productI));
            if (product != null) {
                return "Y".equals(product.getString("isVirtual"));
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
        }

        return false;
    }

    public static boolean isAmountRequired(GenericDelegator delegator, String productI) {
        try {
            GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productI));
            if (product != null) {
                return "Y".equals(product.getString("requireAmount"));
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
        }

        return false;
    }

    public static String getProductTypeId(GenericDelegator delegator, String productI) {
        try {
            GenericValue product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", productI));
            if (product != null) {
                return product.getString("productTypeId");
            }
        } catch (GenericEntityException e) {
            Debug.logWarning(e.getMessage(), module);
        }

        return null;
    }


    /**
     * Generic service to find product by id.
     * By default return the product find by productId
     * but you can pass searchProductFirst at false if you want search in goodIdentification before
     * or pass searchAllId at true to find all product with this id (product.productId and goodIdentification.idValue)
     * @param delegator
     * @param idToFind
     * @param goodIdentificationTypeId
     * @param searchProductFirst
     * @param searchAllId
     * @return
     * @throws GenericEntityException
     */
    public static List<GenericValue> findProductsById( GenericDelegator delegator,
            String idToFind, String goodIdentificationTypeId,
            boolean searchProductFirst, boolean searchAllId) throws GenericEntityException {

        if (Debug.verboseOn()) Debug.logVerbose("Analyze goodIdentification: entered id = " + idToFind + ", goodIdentificationTypeId = " + goodIdentificationTypeId, module);

        GenericValue product = null;
        List<GenericValue> productsFound = null;

        // 1) look if the idToFind given is a real productId
        if (searchProductFirst) {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", idToFind));
        }

        if (searchAllId || (searchProductFirst && UtilValidate.isEmpty(product))) {
            // 2) Retrieve product in GoodIdentification
            Map<String, String> conditions = UtilMisc.toMap("idValue", idToFind);
            if (UtilValidate.isNotEmpty(goodIdentificationTypeId)) {
                conditions.put("goodIdentificationTypeId", goodIdentificationTypeId);
            }
            productsFound = delegator.findByAndCache("GoodIdentificationAndProduct", conditions, UtilMisc.toList("productId"));
        }

        if (! searchProductFirst) {
            product = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", idToFind));
        }

        if (UtilValidate.isNotEmpty(product)) {
            if (UtilValidate.isNotEmpty(productsFound)) productsFound.add(product);
            else productsFound = UtilMisc.toList(product);
        }
        if (Debug.verboseOn()) Debug.logVerbose("Analyze goodIdentification: found product.productId = " + product + ", and list : " + productsFound, module);
        return productsFound;
    }

    public static List<GenericValue> findProductsById( GenericDelegator delegator, String idToFind, String goodIdentificationTypeId)
    throws GenericEntityException {
        return findProductsById(delegator, idToFind, goodIdentificationTypeId, true, false);
    }

    public static String findProductId(GenericDelegator delegator, String idToFind, String goodIdentificationTypeId) throws GenericEntityException {
        GenericValue product = findProduct(delegator, idToFind, goodIdentificationTypeId);
        if (UtilValidate.isNotEmpty(product)) {
            return product.getString("productId");
        } else {
            return null;
        }
    }

    public static String findProductId(GenericDelegator delegator, String idToFind) throws GenericEntityException {
        return findProductId(delegator, idToFind, null);
    }

    public static GenericValue findProduct(GenericDelegator delegator, String idToFind, String goodIdentificationTypeId) throws GenericEntityException {
        List<GenericValue> products = findProductsById(delegator, idToFind, goodIdentificationTypeId);
        GenericValue product = EntityUtil.getFirst(products);
        return product;
    }

    public static List<GenericValue> findProducts(GenericDelegator delegator, String idToFind, String goodIdentificationTypeId) throws GenericEntityException {
        List<GenericValue> productsByIds = findProductsById(delegator, idToFind, goodIdentificationTypeId);
        List<GenericValue> products = null;
        if (UtilValidate.isNotEmpty(productsByIds)) {
            for (GenericValue product : productsByIds) {
                GenericValue productToAdd = product;
                //retreive product GV if the actual genericValue came from viewEntity
                if (! "Product".equals(product.getEntityName())) {
                    productToAdd = delegator.findByPrimaryKeyCache("Product", UtilMisc.toMap("productId", product.get("productId")));
                }

                if (UtilValidate.isEmpty(products)) {
                    products = UtilMisc.toList(productToAdd);
                }
                else {
                    products.add(productToAdd);
                }
            }
        }
        return products;
    }

    public static List<GenericValue> findProducts(GenericDelegator delegator, String idToFind) throws GenericEntityException {
        return findProducts(delegator, idToFind, null);
    }

    public static GenericValue findProduct(GenericDelegator delegator, String idToFind) throws GenericEntityException {
        return findProduct(delegator, idToFind, null);
    }

    public static boolean isSellable(GenericDelegator delegator, String productId, Timestamp atTime) throws GenericEntityException {
        return isSellable(findProduct(delegator, productId), atTime);
    }

    public static boolean isSellable(GenericDelegator delegator, String productId) throws GenericEntityException {
        return isSellable(findProduct(delegator, productId));
    }

    public static boolean isSellable(GenericValue product) {
        return isSellable(product, UtilDateTime.nowTimestamp());
    }

    public static boolean isSellable(GenericValue product, Timestamp atTime) {
        if (product != null) {
            Timestamp introDate = product.getTimestamp("introductionDate");
            Timestamp discDate = product.getTimestamp("salesDiscontinuationDate");
            if (introDate == null || introDate.before(atTime)) {
                if (discDate == null || discDate.after(atTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Set<String> getRefurbishedProductIdSet(String productId, GenericDelegator delegator) throws GenericEntityException {
        Set<String> productIdSet = FastSet.newInstance();

        // find associated refurb items, we want serial number for main item or any refurb items too
        List<GenericValue> refubProductAssocs = EntityUtil.filterByDate(delegator.findByAnd("ProductAssoc",
                UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_REFURB")));
        for (GenericValue refubProductAssoc: refubProductAssocs) {
            productIdSet.add(refubProductAssoc.getString("productIdTo"));
        }

        // see if this is a refurb productId to, and find product(s) it is a refurb of
        List<GenericValue> refubProductToAssocs = EntityUtil.filterByDate(delegator.findByAnd("ProductAssoc",
                UtilMisc.toMap("productIdTo", productId, "productAssocTypeId", "PRODUCT_REFURB")));
        for (GenericValue refubProductToAssoc: refubProductToAssocs) {
            productIdSet.add(refubProductToAssoc.getString("productId"));
        }

        return productIdSet;
    }

    public static String getVariantFromFeatureTree(String productId, List<String> selectedFeatures, GenericDelegator delegator) {

        //  all method code moved here from ShoppingCartEvents.addToCart event
        String variantProductId = null;
        try {

            for (String paramValue: selectedFeatures) {
                // find incompatibilities..
                List<GenericValue> incompatibilityVariants = delegator.findByAndCache("ProductFeatureIactn", UtilMisc.toMap("productId", productId,
                        "productFeatureIactnTypeId","FEATURE_IACTN_INCOMP"));
                for (GenericValue incompatibilityVariant: incompatibilityVariants) {
                    String featur = incompatibilityVariant.getString("productFeatureId");
                    if (paramValue.equals(featur)) {
                        String featurTo = incompatibilityVariant.getString("productFeatureIdTo");
                        for (String paramValueTo: selectedFeatures) {
                            if (featurTo.equals(paramValueTo)) {
                                Debug.logWarning("Incompatible features", module);
                                return null;
                            }
                        }

                    }
                }
                // find dependencies..
                List<GenericValue> dependenciesVariants = delegator.findByAndCache("ProductFeatureIactn", UtilMisc.toMap("productId", productId,
                        "productFeatureIactnTypeId","FEATURE_IACTN_DEPEND"));
                for (GenericValue dpVariant: dependenciesVariants) {
                    String featur = dpVariant.getString("productFeatureId");
                    if (paramValue.equals(featur)) {
                        String featurTo = dpVariant.getString("productFeatureIdTo");
                        boolean found = false;
                        for (String paramValueTo: selectedFeatures) {
                            if (featurTo.equals(paramValueTo)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Debug.logWarning("Dependency features", module);
                            return null;
                        }
                    }
                }
            }
            // find variant
            // Debug.log("=====try to find variant for product: " + productId + " and features: " + selectedFeatures);
            List<GenericValue> productAssocs = EntityUtil.filterByDate(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId","PRODUCT_VARIANT")));
            boolean productFound = false;
nextProd:
            for (GenericValue productAssoc: productAssocs) {
                for (String featureId: selectedFeatures) {
                    List<GenericValue> pAppls = delegator.findByAndCache("ProductFeatureAppl", UtilMisc.toMap("productId", productAssoc.getString("productIdTo"), "productFeatureId", featureId, "productFeatureApplTypeId","STANDARD_FEATURE"));
                    if (UtilValidate.isEmpty(pAppls)) {
                        continue nextProd;
                    }
                }
                productFound = true;
                variantProductId = productAssoc.getString("productIdTo");
                break;
            }
//          if (productFound)
//              Debug.log("=====product found:" + productId + " and features: " + selectedFeatures);

            /**
             * 1. variant not found so create new variant product and use the virtual product as basis, new one  is a variant type and not a virtual type.
             *    adjust the prices according the selected features
             */
            if (!productFound) {
                // copy product to be variant
                GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId",  productId));
                product.put("isVariant", "Y");
                product.put("isVirtual", "N");
                product.put("productId", delegator.getNextSeqId("Product"));
                product.remove("virtualVariantMethodEnum"); // not relevant for a non virtual product.
                product.create();
                // add the selected/standard features as 'standard features' to the 'ProductFeatureAppl' table
                GenericValue productFeatureAppl = delegator.makeValue("ProductFeatureAppl",
                        UtilMisc.toMap("productId", product.getString("productId"), "productFeatureApplTypeId", "STANDARD_FEATURE"));
                productFeatureAppl.put("fromDate", UtilDateTime.nowTimestamp());
                for (String productFeatureId: selectedFeatures) {
                    productFeatureAppl.put("productFeatureId",  productFeatureId);
                    productFeatureAppl.create();
                }
                //add standard features too
                List<GenericValue> stdFeaturesAppls = EntityUtil.filterByDate(delegator.findByAnd("ProductFeatureAppl", UtilMisc.toMap("productId", productId, "productFeatureApplTypeId", "STANDARD_FEATURE")));
                for (GenericValue stdFeaturesAppl: stdFeaturesAppls) {
                    stdFeaturesAppl.put("productId",  product.getString("productId"));
                    stdFeaturesAppl.create();
                }
                /* 3. use the price of the virtual product(Entity:ProductPrice) as a basis and adjust according the prices in the feature price table.
                 *  take the default price from the vitual product, go to the productfeature table and retrieve all the prices for the difFerent features
                 *  add these to the price of the virtual product, store the result as the default price on the variant you created.
                 */
                List<GenericValue> productPrices = EntityUtil.filterByDate(delegator.findByAnd("ProductPrice", UtilMisc.toMap("productId", productId)));
                for (GenericValue productPrice: productPrices) {
                    for (String selectedFeaturedId: selectedFeatures) {
                        List<GenericValue> productFeaturePrices = EntityUtil.filterByDate(delegator.findByAnd("ProductFeaturePrice",
                                UtilMisc.toMap("productFeatureId", selectedFeaturedId, "productPriceTypeId", productPrice.getString("productPriceTypeId"))));
                        if (UtilValidate.isNotEmpty(productFeaturePrices)) {
                            GenericValue productFeaturePrice = productFeaturePrices.get(0);
                            if (UtilValidate.isNotEmpty(productFeaturePrice)) {
                                productPrice.put("price", productPrice.getBigDecimal("price").add(productFeaturePrice.getBigDecimal("price")));
                            }
                        }
                    }
                    if (productPrice.get("price") == null) {
                        productPrice.put("price", productPrice.getBigDecimal("price"));
                    }
                    productPrice.put("productId",  product.getString("productId"));
                    productPrice.create();
                }
                // add the product association
                GenericValue productAssoc = delegator.makeValue("ProductAssoc", UtilMisc.toMap("productId", productId, "productIdTo", product.getString("productId"), "productAssocTypeId", "PRODUCT_VARIANT"));
                productAssoc.put("fromDate", UtilDateTime.nowTimestamp());
                productAssoc.create();
                Debug.log("set the productId to: " + product.getString("productId"));

                // copy the supplier
                List<GenericValue> supplierProducts = delegator.findByAndCache("SupplierProduct", UtilMisc.toMap("productId", productId));
                for (GenericValue supplierProduct: supplierProducts) {
                    supplierProduct.set("productId",  product.getString("productId"));
                    supplierProduct.create();
                }

                // copy the content
                List<GenericValue> productContents = delegator.findByAndCache("ProductContent", UtilMisc.toMap("productId", productId));
                for (GenericValue productContent: productContents) {
                    productContent.set("productId",  product.getString("productId"));
                    productContent.create();
                }

                // finally use the new productId to be added to the cart
                variantProductId = product.getString("productId"); // set to the new product
            }

        } catch (GenericEntityException e) {
            Debug.logError(e, module);
        }

        return variantProductId;
    }
}
