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

<br />
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.PartyShoppingLists}</li>
      <li>
        <form id="createEmptyShoppingList" action="<@ofbizUrl>createEmptyShoppingList</@ofbizUrl>" method="post">
          <input type="hidden" name="partyId" value="${partyId?if_exists}" />
          <a href="javascript:$('createEmptyShoppingList').submit();"" class="buttontext">${uiLabelMap.CommonCreateNew}</a>
        </form>
      </li>      
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
    <#if shoppingLists?has_content>
      <form name="selectShoppingList" method="post" action="<@ofbizUrl>editShoppingList</@ofbizUrl>">
        <select name="shoppingListId">
          <#if shoppingList?has_content>
            <option value="${shoppingList.shoppingListId}">${shoppingList.listName}</option>
            <option value="${shoppingList.shoppingListId}">--</option>
          </#if>
          <#list allShoppingLists as list>
            <option value="${list.shoppingListId}">${list.listName}</option>
          </#list>
        </select>
        <a href="javascript:document.selectShoppingList.submit();" class="smallSubmit">${uiLabelMap.CommonEdit}</a>
      </form>
    <#else>
      ${uiLabelMap.PartyNoShoppingListsParty}.
    </#if>
  </div>
</div>
<br/>
<#if shoppingList?has_content>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.PartyShoppingListDetail} - ${shoppingList.listName}</li>
      <li><a href="javascript:document.updateList.submit();">${uiLabelMap.CommonSave}</a></li>
      <li><a href="/ordermgr/control/createQuoteFromShoppingList?shoppingListId=${shoppingList.shoppingListId?if_exists}&applyStorePromotions=N">${uiLabelMap.PartyCreateNewQuote}</a></li>
      <li><a href="/ordermgr/control/createCustRequestFromShoppingList?shoppingListId=${shoppingList.shoppingListId?if_exists}">${uiLabelMap.PartyCreateNewCustRequest}</a></li>
      <li><a href="/ordermgr/control/loadCartFromShoppingList?shoppingListId=${shoppingList.shoppingListId?if_exists}">${uiLabelMap.OrderNewOrder}</a></li>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
    <form name="updateList" method="post" action="<@ofbizUrl>updateShoppingList</@ofbizUrl>">
      <input type="hidden" name="shoppingListId" value="${shoppingList.shoppingListId}">
      <input type="hidden" name="partyId" value="${shoppingList.partyId?if_exists}">
      <table class="basic-table" cellspacing='0'>
        <tr>
          <td class="label">${uiLabelMap.PartyListName}</td>
          <td><input type="text" size="25" name="listName" value="${shoppingList.listName}" <#if shoppingList.listName?default("") == "auto-save">disabled</#if>>
        </tr>
        <tr>
          <td class="label">${uiLabelMap.CommonDescription}</td>
          <td><input type="text" size="70" name="description" value="${shoppingList.description?if_exists}" <#if shoppingList.listName?default("") == "auto-save">disabled</#if>>
        </tr>
        <tr>
          <td class="label">${uiLabelMap.PartyListType}</td>
          <td>
            <select name="shoppingListTypeId" <#if shoppingList.listName?default("") == "auto-save">disabled</#if>>
              <#if shoppingListType?exists>
                <option value="${shoppingListType.shoppingListTypeId}">${shoppingListType.get("description",locale)?default(shoppingListType.shoppingListTypeId)}</option>
                <option value="${shoppingListType.shoppingListTypeId}">--</option>
              </#if>
              <#list shoppingListTypes as shoppingListType>
                <option value="${shoppingListType.shoppingListTypeId}">${shoppingListType.get("description",locale)?default(shoppingListType.shoppingListTypeId)}</option>
              </#list>
            </select>
          </td>
        </tr>
        <tr>
          <td class="label">${uiLabelMap.PartyPublic}?</td>
          <td>
            <select name="isPublic" <#if shoppingList.listName?default("") == "auto-save">disabled</#if>>
              <option>${shoppingList.isPublic}</option>
              <option value="${shoppingList.isPublic}">--</option>
              <option>${uiLabelMap.CommonYes}</option>
              <option>${uiLabelMap.CommonNo}</option>
            </select>
          </td>
        </tr>
        <tr>
          <td class="label">${uiLabelMap.PartyParentList}</td>
          <td>
            <select name="parentShoppingListId" <#if shoppingList.listName?default("") == "auto-save">disabled</#if>>
              <#if parentShoppingList?exists>
                <option value="${parentShoppingList.shoppingListId}">${parentShoppingList.listName?default(parentShoppingList.shoppingListId)}</option>
              </#if>
              <option value="">${uiLabelMap.PartyNoParent}</option>
              <#list allShoppingLists as newParShoppingList>
                <option value="${newParShoppingList.shoppingListId}">${newParShoppingList.listName?default(newParShoppingList.shoppingListId)}</option>
              </#list>
            </select>
            <#if parentShoppingList?exists>
              <a href="<@ofbizUrl>editShoppingList?shoppingListId=${parentShoppingList.shoppingListId}</@ofbizUrl>" class="smallSubmit">${uiLabelMap.CommonGotoParent} (${parentShoppingList.listName?default(parentShoppingList.shoppingListId)})</a>
            </#if>
          </td>
        </tr>
        <#if shoppingList.listName?default("") != "auto-save">
          <tr>
            <td>&nbsp;</td>
            <td><a href="javascript:document.updateList.submit();" class="smallSubmit">${uiLabelMap.CommonSave}</a></td>
          </tr>
        </#if>
      </table>
    </form>
  </div>
</div>
<#if childShoppingListDatas?has_content>
<br/>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.PartyChildShoppingList} - ${shoppingList.listName}</li>
      <li><a href="<@ofbizUrl>addListToCart?shoppingListId=${shoppingList.shoppingListId}&includeChild=yes</@ofbizUrl>">${uiLabelMap.PartyAddChildListsToCart}</a></li>
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
    <table class="basic-table" cellspacing="0">
      <tr class="header-row">
        <td>${uiLabelMap.PartyListName}</td>
        <td>&nbsp;</td>
      </tr>
      <#list childShoppingListDatas as childShoppingListData>
        <#assign childShoppingList = childShoppingListData.childShoppingList>
        <tr>
          <td class="button-col"><a href="<@ofbizUrl>editShoppingList?shoppingListId=${childShoppingList.shoppingListId}</@ofbizUrl>">${childShoppingList.listName?default(childShoppingList.shoppingListId)}</a></li>
          <td class="button-col align-float">
            <a href="<@ofbizUrl>editShoppingList?shoppingListId=${childShoppingList.shoppingListId}</@ofbizUrl>">${uiLabelMap.PartyGotoList}</a>
            <a href="<@ofbizUrl>addListToCart?shoppingListId=${childShoppingList.shoppingListId}</@ofbizUrl>">${uiLabelMap.PartyAddListToCart}</a>
          </td>
        </tr>
      </#list>
    </table>
  </div>
</div>
</#if>
<br/>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <ul>
      <li class="h3">${uiLabelMap.PartyListItems} - ${shoppingList.listName}</li>
        <#-- <li><a href="<@ofbizUrl>addListToCart?shoppingListId=${shoppingList.shoppingListId}</@ofbizUrl>">${uiLabelMap.PartyAddListToCart}</a></li> -->
    </ul>
    <br class="clear"/>
  </div>
  <div class="screenlet-body">
    <#if shoppingListItemDatas?has_content>
      <table class="basic-table" cellspacing="0">
        <tr class="header-row">
          <td>${uiLabelMap.PartyProduct}</td>
          <td>${uiLabelMap.PartyQuantity}</td>
          <td>${uiLabelMap.PartyQuantityPurchased}</td>
          <td>${uiLabelMap.PartyPrice}</td>
          <td>${uiLabelMap.PartyTotal}</td>
          <td>&nbsp;</td>
        </tr>
        <#assign alt_row = false>
        <#list shoppingListItemDatas as shoppingListItemData>
          <#assign shoppingListItem = shoppingListItemData.shoppingListItem>
          <#assign product = shoppingListItemData.product>
          <#assign productContentWrapper = Static["org.ofbiz.product.product.ProductContentWrapper"].makeProductContentWrapper(product, request)>
          <#assign unitPrice = shoppingListItemData.unitPrice>
          <#assign totalPrice = shoppingListItemData.totalPrice>
          <#assign productVariantAssocs = shoppingListItemData.productVariantAssocs?if_exists>
          <#assign isVirtual = product.isVirtual?exists && product.isVirtual.equals("Y")>
          <tr valign="middle"<#if alt_row> class="alternate-row"</#if>>
            <td><a href="/catalog/control/EditProduct?productId=${shoppingListItem.productId}&externalLoginKey=${requestAttributes.externalLoginKey}">${shoppingListItem.productId} -
              ${productContentWrapper.get("PRODUCT_NAME")?default("No Name")}</a> : ${productContentWrapper.get("DESCRIPTION")?if_exists}
            </td>
            <form method="post" action="<@ofbizUrl>updateShoppingListItem</@ofbizUrl>" name='listform_${shoppingListItem.shoppingListItemSeqId}'>
              <input type="hidden" name="shoppingListId" value="${shoppingListItem.shoppingListId}">
              <input type="hidden" name="shoppingListItemSeqId" value="${shoppingListItem.shoppingListItemSeqId}">
              <td>
                <input size="6" type="text" name="quantity" value="${shoppingListItem.quantity?string.number}">
              </td>
              <td>
                <input size="6" type="text" name="quantityPurchased"
                  <#if shoppingListItem.quantityPurchased?has_content>
                    value="${shoppingListItem.quantityPurchased?if_exists?string.number}"
                  </#if>>
              </td>
            </form>
            <td class="align-float"><@ofbizCurrency amount=unitPrice isoCode=currencyUomId/></td>
            <td class="align-float"><@ofbizCurrency amount=totalPrice isoCode=currencyUomId/></td>
            <td class="button-col align-float">
              <a href="javascript:document.listform_${shoppingListItem.shoppingListItemSeqId}.submit();">${uiLabelMap.CommonUpdate}</a>
              <a href="<@ofbizUrl>removeFromShoppingList?shoppingListId=${shoppingListItem.shoppingListId}&shoppingListItemSeqId=${shoppingListItem.shoppingListItemSeqId}</@ofbizUrl>">${uiLabelMap.CommonRemove}</a>
            </td>
          </tr>
          <#-- toggle the row color -->

          <#assign alt_row = !alt_row>
        </#list>
      </table>
    <#else>
      ${uiLabelMap.PartyShoppingListEmpty}.
    </#if>
  </div>
</div>
<br/>
<div class="screenlet">
  <div class="screenlet-title-bar">
    <h3>${uiLabelMap.PartyQuickAddList}</h3>
  </div>
  <div class="screenlet-body">
    <form name="addToShoppingList" method="post" action="<@ofbizUrl>addItemToShoppingList<#if requestAttributes._CURRENT_VIEW_?exists>/${requestAttributes._CURRENT_VIEW_}</#if></@ofbizUrl>">
      <input type="hidden" name="shoppingListId" value="${shoppingList.shoppingListId}">
      <input type="hidden" name="partyId" value="${shoppingList.partyId?if_exists}">
      <input type="text" name="productId" value="">
      <input type="text" size="5" name="quantity" value="${requestParameters.quantity?default("1")}">
      <input type="submit" value="${uiLabelMap.PartyAddToShoppingList}">
    </form>
  </div>
</div>
</#if>
<!-- begin editShoppingList.ftl -->