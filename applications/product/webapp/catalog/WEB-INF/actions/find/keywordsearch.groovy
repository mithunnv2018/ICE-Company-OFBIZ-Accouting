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

import org.ofbiz.entity.condition.*
import org.ofbiz.product.catalog.*
import org.ofbiz.product.product.*

module = "keywordsearch.groovy";

// note: this can be run multiple times in the same request without causing problems, will check to see on its own if it has run again
ProductSearchSession.processSearchParameters(parameters, request);
prodCatalogId = CatalogWorker.getCurrentCatalogId(request);
result = ProductSearchSession.getProductSearchResult(request, delegator, prodCatalogId);

applicationTypes = delegator.findList("ProductFeatureApplType", null, null, ['description'], null, false);

expr = EntityCondition.makeCondition(EntityCondition.makeCondition("showInSelect", EntityOperator.EQUALS, null),
                                     EntityOperator.OR,
                                     EntityCondition.makeCondition("showInSelect", EntityOperator.NOT_EQUAL, "N"));
productCategories = delegator.findList("ProductCategory", expr, null, ['description'], null, false);

context.applicationTypes = applicationTypes;
context.productCategories = productCategories;

context.productIds = result.productIds;
context.viewIndex = result.viewIndex;
context.viewSize = result.viewSize;
context.listSize = result.listSize;
context.lowIndex = result.lowIndex;
context.highIndex = result.highIndex;
context.paging = result.paging;
context.previousViewSize = result.previousViewSize;
context.searchCategory = result.searchCategory;
context.searchConstraintStrings = result.searchConstraintStrings;
context.searchSortOrderString = result.searchSortOrderString;