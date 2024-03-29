<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->
<#assign searchOptionsHistoryList = Static["org.ofbiz.product.product.ProductSearchSession"].getSearchOptionsHistoryList(session)>
<#assign currentCatalogId = Static["org.ofbiz.product.catalog.CatalogWorker"].getCurrentCatalogId(request)>
<h1>${uiLabelMap.ProductAdvancedSearchInCategory}</h1>
<br/>
<form name="advtokeywordsearchform" method="post" action="<@ofbizUrl>keywordsearch</@ofbizUrl>" style="margin: 0;">
  <input type="hidden" name="VIEW_SIZE" value="10">
  <table border="0" wdith="100%">
    <input type="hidden" name="SEARCH_CATALOG_ID" value="${currentCatalogId}">
    <#if searchCategory?has_content>
        <input type="hidden" name="SEARCH_CATEGORY_ID" value="${searchCategoryId?if_exists}">
        <tr>
          <td align="right" valign="middle">
            <div>${uiLabelMap.ProductCategory}:</div>
          </td>
          <td valign="middle">
            <div>
              <b>"${(searchCategory.description)?if_exists}"</b>${uiLabelMap.ProductIncludeSubCategories}
              ${uiLabelMap.CommonYes}<input type="radio" name="SEARCH_SUB_CATEGORIES" value="Y" checked>
              ${uiLabelMap.CommonNo}<input type="radio" name="SEARCH_SUB_CATEGORIES" value="N">
            </div>
          </td>
        </tr>
    </#if>
    <tr>
      <td align="right" valign="middle">
        <div>${uiLabelMap.ProductKeywords}:</div>
      </td>
      <td valign="middle">
        <div>
          <input type="text" name="SEARCH_STRING" size="40" value="${requestParameters.SEARCH_STRING?if_exists}">&nbsp;
          ${uiLabelMap.CommonAny}<input type="radio" name="SEARCH_OPERATOR" value="OR" <#if searchOperator == "OR">checked</#if>>
          ${uiLabelMap.CommonAll}<input type="radio" name="SEARCH_OPERATOR" value="AND" <#if searchOperator == "AND">checked</#if>>
        </div>
      </td>
    </tr>
    <#list productFeatureTypeIdsOrdered as productFeatureTypeId>
      <#assign findPftMap = Static["org.ofbiz.base.util.UtilMisc"].toMap("productFeatureTypeId", productFeatureTypeId)>
      <#assign productFeatureType = delegator.findByPrimaryKeyCache("ProductFeatureType", findPftMap)>
      <#assign productFeatures = productFeaturesByTypeMap[productFeatureTypeId]>
      <tr>
        <td align="right" valign="middle">
          <div>${(productFeatureType.get("description",locale))?if_exists}:</div>
        </td>
        <td valign="middle">
          <div>
            <select name="pft_${productFeatureTypeId}">
              <option value="">- ${uiLabelMap.CommonSelectAny} -</option>
              <#list productFeatures as productFeature>
              <option value="${productFeature.productFeatureId}">${productFeature.get("description",locale)?default(productFeature.productFeatureId)}</option>
              </#list>
            </select>
          </div>
        </td>
      </tr>
    </#list>
    <tr>
      <td align="right" valign="middle">
        <div>${uiLabelMap.ProductSupplier}:</div>
      </td>
      <td valign="middle">
        <div>
          <select name="SEARCH_SUPPLIER_ID">
            <option value="">- ${uiLabelMap.CommonSelectAny} -</option>
            <#list supplerPartyRoleAndPartyDetails as supplerPartyRoleAndPartyDetail>
              <option value="${supplerPartyRoleAndPartyDetail.partyId}"<#if (sessionAttributes.orderPartyId?exists & sessionAttributes.orderPartyId = supplerPartyRoleAndPartyDetail.partyId)> selected</#if>>${supplerPartyRoleAndPartyDetail.groupName?if_exists} ${supplerPartyRoleAndPartyDetail.firstName?if_exists} ${supplerPartyRoleAndPartyDetail.lastName?if_exists} [${supplerPartyRoleAndPartyDetail.partyId}]</option>
            </#list>
          </select>
        </div>
      </td>
    </tr>
    <tr>
      <td align="right" valign="middle">
        <div>${uiLabelMap.CommonSortedBy}:</div>
      </td>
      <td valign="middle">
        <div>
          <select name="sortOrder">
            <option value="SortKeywordRelevancy">${uiLabelMap.ProductKeywordRelevancy}</option>
            <option value="SortProductField:productName">${uiLabelMap.ProductProductName}</option>
            <option value="SortProductField:internalName">${uiLabelMap.ProductInternalName}</option>
            <option value="SortProductField:totalQuantityOrdered">${uiLabelMap.ProductPopularityByOrders}</option>
            <option value="SortProductField:totalTimesViewed">${uiLabelMap.ProductPopularityByViews}</option>
            <option value="SortProductField:averageCustomerRating">${uiLabelMap.ProductCustomerRating}</option>
            <option value="SortProductPrice:LIST_PRICE">${uiLabelMap.ProductListPrice}</option>
            <option value="SortProductPrice:DEFAULT_PRICE">${uiLabelMap.ProductDefaultPrice}</option>
            <option value="SortProductPrice:AVERAGE_COST">${uiLabelMap.ProductAverageCost}</option>
          </select>
          ${uiLabelMap.ProductLowToHigh}<input type="radio" name="sortAscending" value="Y" checked>
          ${uiLabelMap.ProductHighToLow}<input type="radio" name="sortAscending" value="N">
        </div>
      </td>
    </tr>
    <#if searchConstraintStrings?has_content>
      <tr>
        <td align="right" valign="top">
          <div>${uiLabelMap.ProductLastSearch}:</div>
        </td>
        <td valign="top">
            <#list searchConstraintStrings as searchConstraintString>
                <div>&nbsp;-&nbsp;${searchConstraintString}</div>
            </#list>
            <div>${uiLabelMap.CommonSortedBy}: ${searchSortOrderString}</div>
            <div>
              ${uiLabelMap.ProductNewSearch}<input type="radio" name="clearSearch" value="Y" checked>
              ${uiLabelMap.ProductRefineSearch}<input type="radio" name="clearSearch" value="N">
            </div>
        </td>
      </tr>
    </#if>
    <tr>
      <td>
        <div>
          <a href="javascript:document.advtokeywordsearchform.submit()" class="buttontext">${uiLabelMap.CommonFind}</a>
        </div>
      </td>
    </tr>
  </table>

  <#if searchOptionsHistoryList?has_content>
    <hr/>

    <h2>${uiLabelMap.OrderLastSearches}...</h2>

    <div>
      <a href="<@ofbizUrl>clearSearchOptionsHistoryList</@ofbizUrl>" class="buttontext">${uiLabelMap.OrderClearSearchHistory}</a>
      ${uiLabelMap.OrderClearSearchHistoryNote}
    </div>
    <#list searchOptionsHistoryList as searchOptions>
    <#-- searchOptions type is ProductSearchSession.ProductSearchOptions -->
        <div>
          <b>${uiLabelMap.CommonSearch} #${searchOptions_index + 1}</b>
          <a href="<@ofbizUrl>setCurrentSearchFromHistoryAndSearch?searchHistoryIndex=${searchOptions_index}&clearSearch=N</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonSearch}</a>
          <a href="<@ofbizUrl>setCurrentSearchFromHistory?searchHistoryIndex=${searchOptions_index}</@ofbizUrl>" class="buttontext">${uiLabelMap.CommonRefine}</a>
        </div>
        <#assign constraintStrings = searchOptions.searchGetConstraintStrings(false, delegator, locale)>
        <#list constraintStrings as constraintString>
          <div>&nbsp;-&nbsp;${constraintString}</div>
        </#list>
        <#if searchOptions_has_next>
          <hr/>
        </#if>
    </#list>
  </#if>
</form>

