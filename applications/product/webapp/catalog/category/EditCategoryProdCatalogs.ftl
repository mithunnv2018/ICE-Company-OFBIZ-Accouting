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

<#if productCategoryId?exists && productCategory?exists>
    <div class="screenlet-title-bar">
        <h3>${uiLabelMap.PageTitleEditCategoryProductCatalogs}</h3>
    </div>
    <div class="screenlet">
        <div class="screenlet-body">
            <table cellspacing="0" class="basic-table">
            <tr class="header-row">
                <td><b>${uiLabelMap.ProductCatalogNameId}</b></td>
                <td><b>${uiLabelMap.CommonType}</b></td>
                <td><b>${uiLabelMap.CommonFromDateTime}</b></td>
                <td align="center"><b>${uiLabelMap.ProductThruDateTimeSequence}</b></td>
                <td><b>&nbsp;</b></td>
            </tr>
            <#assign line = 0>
            <#assign rowClass = "2">
            <#list prodCatalogCategories as prodCatalogCategory>
            <#assign line = line + 1>
            <#assign prodCatalog = prodCatalogCategory.getRelatedOne("ProdCatalog")>
            <#assign curProdCatalogCategoryType = prodCatalogCategory.getRelatedOneCache("ProdCatalogCategoryType")>
            <tr valign="middle"<#if rowClass == "1"> class="alternate-row"</#if>>
                <td><a href="<@ofbizUrl>EditProdCatalog?prodCatalogId=${(prodCatalogCategory.prodCatalogId)?if_exists}</@ofbizUrl>" class="buttontext"><#if prodCatalog?exists>${(prodCatalog.catalogName)?if_exists}</#if> [${(prodCatalogCategory.prodCatalogId)?if_exists}]</a></td>
                <td>
                    ${(curProdCatalogCategoryType.get("description",locale))?default(prodCatalogCategory.prodCatalogCategoryTypeId)}
                </td>
                <#assign hasntStarted = false>
                <#if (prodCatalogCategory.getTimestamp("fromDate"))?exists && nowTimestamp.before(prodCatalogCategory.getTimestamp("fromDate"))> <#assign hasntStarted = true></#if>
                <td <#if hasntStarted> style="color: red;"</#if>>${(prodCatalogCategory.fromDate)?if_exists}</td>
                <td align="center">
                    <form method="post" action="<@ofbizUrl>category_updateProductCategoryToProdCatalog</@ofbizUrl>" name="lineForm_update${line}">
                        <#assign hasExpired = false>
                        <#if (prodCatalogCategory.getTimestamp("thruDate"))?exists && nowTimestamp.after(prodCatalogCategory.getTimestamp("thruDate"))> <#assign hasExpired = true></#if>
                        <input type="hidden" name="prodCatalogId" value="${(prodCatalogCategory.prodCatalogId)?if_exists}"/>
                        <input type="hidden" name="productCategoryId" value="${(prodCatalogCategory.productCategoryId)?if_exists}"/>
                        <input type="hidden" name="prodCatalogCategoryTypeId" value="${prodCatalogCategory.prodCatalogCategoryTypeId}"/>
                        <input type="hidden" name="fromDate" value="${(prodCatalogCategory.fromDate)?if_exists}"/>
                        <input type="text" size="25" name="thruDate" value="${(prodCatalogCategory.thruDate)?if_exists}" style="<#if (hasExpired) >color: red;</#if>"/>
                        <a href="javascript:call_cal(document.lineForm_update${line}.thruDate, '${(prodCatalogCategory.thruDate)?default(nowTimestamp?string)}');"><img src="<@ofbizContentUrl>/images/cal.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="Calendar"/></a>
                        <input type="text" size="5" name="sequenceNum" value="${(prodCatalogCategory.sequenceNum)?if_exists}"/>
                        <#-- the prodCatalogCategoryTypeId field is now part of the PK, so it can't be changed, must be re-created
                        <select name="prodCatalogCategoryTypeId" size="1">
                            <#if (prodCatalogCategory.prodCatalogCategoryTypeId)?exists>
                                <option value="${prodCatalogCategory.prodCatalogCategoryTypeId}"><#if curProdCatalogCategoryType?exists>${(curProdCatalogCategoryType.description)?if_exists}<#else> [${(prodCatalogCategory.prodCatalogCategoryTypeId)}]</#if></option>
                                <option value="${prodCatalogCategory.prodCatalogCategoryTypeId}"></option>
                            <#else>
                                <option value="">&nbsp;</option>
                            </#if>
                            <#list prodCatalogCategoryTypes as prodCatalogCategoryType>
                            <option value="${(prodCatalogCategoryType.prodCatalogCategoryTypeId)?if_exists}">${(prodCatalogCategoryType.get("description",locale))?if_exists}</option>
                            </#list>
                        </select> -->
                        <input type="submit" value="${uiLabelMap.CommonUpdate}"/>
                    </form>
                </td>
                <td align="center">
                  <form method="post" action="<@ofbizUrl>category_removeProductCategoryFromProdCatalog</@ofbizUrl>" name="lineForm_delete${line}">
                    <input type="hidden" name="prodCatalogId" value="${(prodCatalogCategory.prodCatalogId)?if_exists}"/>
                    <input type="hidden" name="productCategoryId" value="${(prodCatalogCategory.productCategoryId)?if_exists}"/>
                    <input type="hidden" name="prodCatalogCategoryTypeId" value="${prodCatalogCategory.prodCatalogCategoryTypeId}"/>
                    <input type="hidden" name="fromDate" value="${(prodCatalogCategory.fromDate)?if_exists}"/>
                    <input type="submit" value="${uiLabelMap.CommonDelete}"/>
                  </form>
                </td>
            </tr>
            <#-- toggle the row color -->
            <#if rowClass == "2">
                <#assign rowClass = "1">
            <#else>
                <#assign rowClass = "2">
            </#if>
            </#list>
            </table>
            <br/>
        </div>
    </div>
    <div class="screenlet">
        <div class="screenlet-title-bar">
            <h3>${uiLabelMap.ProductAddCatalogProductCategory}</h3>
        </div>
        <div class="screenlet-body">
            <table cellspacing="0" class="basic-table">
                <tr><td>
                    <form method="post" action="<@ofbizUrl>category_addProductCategoryToProdCatalog</@ofbizUrl>" style="margin: 0;" name="addNewForm">
                        <input type="hidden" name="productCategoryId" value="${productCategoryId?if_exists}"/>
                        <select name="prodCatalogId">
                        <#list prodCatalogs as prodCatalog>
                            <option value="${(prodCatalog.prodCatalogId)?if_exists}">${(prodCatalog.catalogName)?if_exists} [${(prodCatalog.prodCatalogId)?if_exists}]</option>
                        </#list>
                        </select>
                        <select name="prodCatalogCategoryTypeId" size="1">
                        <#list prodCatalogCategoryTypes as prodCatalogCategoryType>
                            <option value="${(prodCatalogCategoryType.prodCatalogCategoryTypeId)?if_exists}">${(prodCatalogCategoryType.get("description",locale))?if_exists}</option>
                        </#list>
                        </select>
                        <input type="text" size="25" name="fromDate"/>
                        <a href="javascript:call_cal(document.addNewForm.fromDate, '${nowTimestamp?string}');"><img src="<@ofbizContentUrl>/images/cal.gif</@ofbizContentUrl>" width="16" height="16" border="0" alt="Calendar"/></a>
                        <input type="submit" value="${uiLabelMap.CommonAdd}"/>
                    </form>
                </td></tr>
            </table>
        </div>
    </div>
</#if>
