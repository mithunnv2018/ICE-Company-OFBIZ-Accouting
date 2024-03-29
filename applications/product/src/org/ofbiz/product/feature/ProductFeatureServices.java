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
package org.ofbiz.product.feature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;
import javolution.util.FastSet;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntity;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ModelService;
import org.ofbiz.service.ServiceUtil;


/**
 * Services for product features
 */

public class ProductFeatureServices {

    public static final String module = ProductFeatureServices.class.getName();
    public static final String resource = "ProductUiLabels";

    /*
     * Parameters: productFeatureCategoryId, productFeatureGroupId, productId, productFeatureApplTypeId
     * Result: productFeaturesByType, a Map of all product features from productFeatureCategoryId, group by productFeatureType -> List of productFeatures
     * If the parameter were productFeatureCategoryId, the results are from ProductFeatures.  If productFeatureCategoryId were null and there were a productFeatureGroupId,
     * the results are from ProductFeatureGroupAndAppl.  Otherwise, if there is a productId, the results are from ProductFeatureAndAppl.
     * The optional productFeatureApplTypeId causes results to be filtered by this parameter--only used in conjunction with productId.
     */
    public static Map<String, Object> getProductFeaturesByType(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> results = FastMap.newInstance();
        GenericDelegator delegator = dctx.getDelegator();

        /* because we might need to search either for product features or for product features of a product, the search code has to be generic.
         * we will determine which entity and field to search on based on what the user has supplied us with.
         */
        String valueToSearch = (String) context.get("productFeatureCategoryId");
        String productFeatureApplTypeId = (String) context.get("productFeatureApplTypeId");

        String entityToSearch = "ProductFeature";
        String fieldToSearch = "productFeatureCategoryId";
        List<String> orderBy = UtilMisc.toList("productFeatureTypeId", "description");

        if (valueToSearch == null && context.get("productFeatureGroupId") != null) {
            entityToSearch = "ProductFeatureGroupAndAppl";
            fieldToSearch = "productFeatureGroupId";
            valueToSearch = (String) context.get("productFeatureGroupId");
            // use same orderBy as with a productFeatureCategoryId search
        } else if (valueToSearch == null && context.get("productId") != null) {
            entityToSearch = "ProductFeatureAndAppl";
            fieldToSearch = "productId";
            valueToSearch = (String) context.get("productId");
            orderBy = UtilMisc.toList("sequenceNum", "productFeatureApplTypeId", "productFeatureTypeId", "description");
        }

        if (valueToSearch == null) {
            return ServiceUtil.returnError("This service requires a productId, a productFeatureGroupId, or a productFeatureCategoryId to run.");
        }

        try {
            // get all product features in this feature category
            List<GenericValue> allFeatures = delegator.findByAnd(entityToSearch, UtilMisc.toMap(fieldToSearch, valueToSearch), orderBy);

            if (entityToSearch.equals("ProductFeatureAndAppl") && productFeatureApplTypeId != null)
                allFeatures = EntityUtil.filterByAnd(allFeatures, UtilMisc.toMap("productFeatureApplTypeId", productFeatureApplTypeId));

            List<String> featureTypes = FastList.newInstance();
            Map<String, List<GenericValue>> featuresByType = new LinkedHashMap<String, List<GenericValue>>();
            for (GenericValue feature: allFeatures) {
                String featureType = feature.getString("productFeatureTypeId");
                if (!featureTypes.contains(featureType)) {
                    featureTypes.add(featureType);
                }
                List<GenericValue> features = featuresByType.get(featureType);
                if (features == null) {
                    features = FastList.newInstance();
                    featuresByType.put(featureType, features);
                }
                features.add(feature);
            }

            results = ServiceUtil.returnSuccess();
            results.put("productFeatureTypes", featureTypes);
            results.put("productFeaturesByType", featuresByType);
        } catch (GenericEntityException ex) {
            Debug.logError(ex, ex.getMessage(), module);
            return ServiceUtil.returnError(ex.getMessage());
        }
        return results;
    }

    /*
     * Parameter: productId, productFeatureAppls (a List of ProductFeatureAndAppl entities of features applied to productId)
     * Result: variantProductIds: a List of productIds of variants with those features
     */
    public static Map<String, Object> getAllExistingVariants(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> results = FastMap.newInstance();
        GenericDelegator delegator = dctx.getDelegator();

        String productId = (String) context.get("productId");
        List<String> curProductFeatureAndAppls = UtilGenerics.checkList(context.get("productFeatureAppls"));
        List<String> existingVariantProductIds = FastList.newInstance();

        try {
            /*
             * get a list of all products which are associated with the current one as PRODUCT_VARIANT and for each one,
             * see if it has every single feature in the list of productFeatureAppls as a STANDARD_FEATURE.  If so, then
             * it qualifies and add it to the list of existingVariantProductIds.
             */
            List<GenericValue> productAssocs = EntityUtil.filterByDate(delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productId", productId, "productAssocTypeId", "PRODUCT_VARIANT")));
            for (GenericValue productAssoc: productAssocs) {

                //for each associated product, if it has all standard features, display it's productId
                boolean hasAllFeatures = true;
                for (String productFeatureAndAppl: curProductFeatureAndAppls) {
                    Map<String, String> findByMap = UtilMisc.toMap("productId", productAssoc.getString("productIdTo"),
                            "productFeatureId", productFeatureAndAppl,
                            "productFeatureApplTypeId", "STANDARD_FEATURE");

                    //Debug.log("Using findByMap: " + findByMap);

                    List<GenericValue> standardProductFeatureAndAppls = EntityUtil.filterByDate(delegator.findByAnd("ProductFeatureAppl", findByMap));
                    if (UtilValidate.isEmpty(standardProductFeatureAndAppls)) {
                        // Debug.log("Does NOT have this standard feature");
                        hasAllFeatures = false;
                        break;
                    } else {
                        // Debug.log("DOES have this standard feature");
                    }
                }

                if (hasAllFeatures) {
                    // add to list of existing variants: productId=productAssoc.productIdTo
                    existingVariantProductIds.add(productAssoc.getString("productIdTo"));
                }
            }
            results = ServiceUtil.returnSuccess();
            results.put("variantProductIds", existingVariantProductIds);
        } catch (GenericEntityException ex) {
            Debug.logError(ex, ex.getMessage(), module);
            return ServiceUtil.returnError(ex.getMessage());
        }
    return results;
    }

    /*
     * Parameter: productId (of the parent product which has SELECTABLE features)
     * Result: featureCombinations, a List of Maps containing, for each possible variant of the productid:
     * {defaultVariantProductId: id of this variant; curProductFeatureAndAppls: features applied to this variant; existingVariantProductIds: List of productIds which are already variants with these features }
     */
    public static Map<String, Object> getVariantCombinations(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> results = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();

        String productId = (String) context.get("productId");

        try {
            Map<String, Object> featuresResults = dispatcher.runSync("getProductFeaturesByType", UtilMisc.toMap("productId", productId));
            Map<String, List<GenericValue>> features;

            if (featuresResults.get(ModelService.RESPONSE_MESSAGE).equals(ModelService.RESPOND_SUCCESS)) {
                features = UtilGenerics.checkMap(featuresResults.get("productFeaturesByType"));
            } else {
                return ServiceUtil.returnError((String) featuresResults.get(ModelService.ERROR_MESSAGE_LIST));
            }

            // need to keep 2 lists, oldCombinations and newCombinations, and keep swapping them after each looping.  Otherwise, you'll get a
            // concurrent modification exception
            List<Map<String, Object>> oldCombinations = FastList.newInstance();

            // loop through each feature type
            for (Map.Entry<String, List<GenericValue>> entry: features.entrySet()) {
                String currentFeatureType = entry.getKey();
                List<GenericValue> currentFeatures = entry.getValue();

                List<Map<String, Object>> newCombinations = FastList.newInstance();
                List<Map<String, Object>> combinations;

                // start with either existing combinations or from scratch
                if (oldCombinations.size() > 0) {
                    combinations = oldCombinations;
                } else {
                    combinations = FastList.newInstance();
                }

                // in both cases, use each feature of current feature type's idCode and
                // product feature and add it to the id code and product feature applications
                // of the next variant.  just a matter of whether we're starting with an
                // existing list of features and id code or from scratch.
                if (combinations.size()==0) {
                    for (GenericValue currentFeature: currentFeatures) {
                        if (currentFeature.getString("productFeatureApplTypeId").equals("SELECTABLE_FEATURE")) {
                            Map<String, Object> newCombination = FastMap.newInstance();
                            List<GenericValue> newFeatures = FastList.newInstance();
                            List<String> newFeatureIds = FastList.newInstance();
                            if (currentFeature.getString("idCode") != null) {
                                newCombination.put("defaultVariantProductId", productId + currentFeature.getString("idCode"));
                            } else {
                                newCombination.put("defaultVariantProductId", productId);
                            }
                            newFeatures.add(currentFeature);
                            newFeatureIds.add(currentFeature.getString("productFeatureId"));
                            newCombination.put("curProductFeatureAndAppls", newFeatures);
                            newCombination.put("curProductFeatureIds", newFeatureIds);
                            newCombinations.add(newCombination);
                        }
                    }
                } else {
                    for (Map<String, Object> combination: combinations) {
                        for (GenericValue currentFeature: currentFeatures) {
                            if (currentFeature.getString("productFeatureApplTypeId").equals("SELECTABLE_FEATURE")) {
                                Map<String, Object> newCombination = FastMap.newInstance();
                                // .clone() is important, or you'll keep adding to the same List for all the variants
                                // have to cast twice: once from get() and once from clone()
                                List<GenericValue> newFeatures = UtilMisc.makeListWritable(UtilGenerics.<GenericValue>checkList(combination.get("curProductFeatureAndAppls")));
                                List<String> newFeatureIds = UtilMisc.makeListWritable(UtilGenerics.<String>checkList(combination.get("curProductFeatureIds")));
                                if (currentFeature.getString("idCode") != null) {
                                    newCombination.put("defaultVariantProductId", combination.get("defaultVariantProductId") + currentFeature.getString("idCode"));
                                } else {
                                    newCombination.put("defaultVariantProductId", combination.get("defaultVariantProductId"));
                                }
                                newFeatures.add(currentFeature);
                                newFeatureIds.add(currentFeature.getString("productFeatureId"));
                                newCombination.put("curProductFeatureAndAppls", newFeatures);
                                newCombination.put("curProductFeatureIds", newFeatureIds);
                                newCombinations.add(newCombination);
                            }
                        }
                    }
                }
                if (newCombinations.size() >= oldCombinations.size()) {
                    oldCombinations = newCombinations; // save the newly expanded list as oldCombinations
                }
            }

            int defaultCodeCounter = 1;
            Set<String> defaultVariantProductIds = FastSet.newInstance(); // this map will contain the codes already used (as keys)
            defaultVariantProductIds.add(productId);

            // now figure out which of these combinations already have productIds associated with them
            for (Map<String, Object> combination: oldCombinations) {
                // Verify if the default code is already used, if so add a numeric suffix
                if (defaultVariantProductIds.contains((String) combination.get("defaultVariantProductId"))) {
                    combination.put("defaultVariantProductId", combination.get("defaultVariantProductId") + "-" + (defaultCodeCounter < 10? "0" + defaultCodeCounter: "" + defaultCodeCounter));
                    defaultCodeCounter++;
                }
                defaultVariantProductIds.add((String) combination.get("defaultVariantProductId"));
                results = dispatcher.runSync("getAllExistingVariants", UtilMisc.toMap("productId", productId,
                                             "productFeatureAppls", combination.get("curProductFeatureIds")));
                combination.put("existingVariantProductIds", results.get("variantProductIds"));
            }
            results = ServiceUtil.returnSuccess();
            results.put("featureCombinations", oldCombinations);
        } catch (GenericServiceException ex) {
            Debug.logError(ex, ex.getMessage(), module);
            return ServiceUtil.returnError(ex.getMessage());
        }

        return results;
    }

    /*
     * Parameters: productCategoryId (String) and productFeatures (a List of ProductFeature GenericValues)
     * Result: products (a List of Product GenericValues)
     */
    public static Map<String, Object> getCategoryVariantProducts(DispatchContext dctx, Map<String, ? extends Object> context) {
        Map<String, Object> results = FastMap.newInstance();
        LocalDispatcher dispatcher = dctx.getDispatcher();

        List<GenericValue> productFeatures = UtilGenerics.checkList(context.get("productFeatures"));
        String productCategoryId = (String) context.get("productCategoryId");

        // get all the product members of the product category
        Map result;
        try {
            result = dispatcher.runSync("getProductCategoryMembers", UtilMisc.toMap("categoryId", productCategoryId));
        } catch (GenericServiceException ex) {
            Debug.logError("Cannot get category memebers for " + productCategoryId + " due to error: " + ex.getMessage(), module);
            return ServiceUtil.returnError(ex.getMessage());
        }

        List<GenericValue> memberProducts = UtilGenerics.checkList(result.get("categoryMembers"));
        if ((memberProducts != null) && (memberProducts.size() > 0)) {
            // construct a Map of productFeatureTypeId -> productFeatureId from the productFeatures List
            Map<String, String> featuresByType = FastMap.newInstance();
            for (GenericValue nextFeature: productFeatures) {
                featuresByType.put(nextFeature.getString("productFeatureTypeId"), nextFeature.getString("productFeatureId"));
            }

            List<GenericValue> products = FastList.newInstance();  // final list of variant products
            for (GenericValue memberProduct: memberProducts) {
                // find variants for each member product of the category

                try {
                    result = dispatcher.runSync("getProductVariant", UtilMisc.toMap("productId", memberProduct.getString("productId"), "selectedFeatures", featuresByType));
                } catch (GenericServiceException ex) {
                    Debug.logError("Cannot get product variants for " + memberProduct.getString("productId") + " due to error: " + ex.getMessage(), module);
                    return ServiceUtil.returnError(ex.getMessage());
                }

                List<GenericValue> variantProducts = UtilGenerics.checkList(result.get("products"));
                if ((variantProducts != null) && (variantProducts.size() > 0)) {
                    products.addAll(variantProducts);
                } else {
                    Debug.logWarning("Product " + memberProduct.getString("productId") + " did not have any variants for the given features", module);
                }
            }

            if (products.size() == 0) {
                return ServiceUtil.returnError("No products which fit your requirements were found.");
            } else {
                results = ServiceUtil.returnSuccess();
                results.put("products", products);
            }

        } else {
            Debug.logWarning("No products found in " + productCategoryId, module);
        }

        return results;
    }
}
