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
<#assign catalogCol = Static["org.ofbiz.product.catalog.CatalogWorker"].getCatalogIdsAvailable(request)?if_exists>
<#assign currentCatalogId = Static["org.ofbiz.product.catalog.CatalogWorker"].getCurrentCatalogId(request)?if_exists>
<#assign currentCatalogName = Static["org.ofbiz.product.catalog.CatalogWorker"].getCatalogName(request, currentCatalogId)?if_exists>

<#-- Only show if there is more than 1 (one) catalog, no sense selecting when there is only one option... -->
<#if (catalogCol?size > 1)>
<div id ="choosecatalog" class="screenlet">
    <div class="screenlet-header">
        <div class="boxhead">${uiLabelMap.ProductChooseCatalog}</div>
    </div>
    <div class="screenlet-body" style="text-align: center;">
        <form name="choosecatalogform" method="post" action="<@ofbizUrl>main</@ofbizUrl>" style='margin: 0;'>
          <select name='CURRENT_CATALOG_ID' class='selectBox' onchange="submit()">
            <option value='${currentCatalogId}'>${currentCatalogName}</option>
            <option value='${currentCatalogId}'></option>
            <#list catalogCol as catalogId>
              <#assign thisCatalogName = Static["org.ofbiz.product.catalog.CatalogWorker"].getCatalogName(request, catalogId)>
              <option value='${catalogId}'>${thisCatalogName}</option>
            </#list>
          </select>
        </form>
    </div>
</div>
</#if>
