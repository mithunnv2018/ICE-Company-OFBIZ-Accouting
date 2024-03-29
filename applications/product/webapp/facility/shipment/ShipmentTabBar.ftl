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
<#if requestAttributes.uiLabelMap?exists>
<#assign uiLabelMap = requestAttributes.uiLabelMap>
</#if>
<#assign selected = tabButtonItem?default("void")>
<#if shipmentId?has_content>
    <div class="button-bar tab-bar">
        <ul>
            <li<#if selected="ViewShipment"> class="selected"</#if>><a href="<@ofbizUrl>ViewShipment?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.CommonView}</a></li>
            <li<#if selected="EditShipment"> class="selected"</#if>><a href="<@ofbizUrl>EditShipment?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.CommonEdit}</a></li>
        <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId='SALES_SHIPMENT'>
            <li<#if selected="EditShipmentPlan"> class="selected"</#if>><a href="<@ofbizUrl>EditShipmentPlan?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductShipmentPlan}</a></li>
        </#if>
        <#if (shipment.shipmentTypeId)?exists && (shipment.shipmentTypeId = "SALES_SHIPMENT" || shipment.shipmentTypeId = "PURCHASE_SHIPMENT")>
            <li<#if selected="AddItemsFromOrder"> class="selected"</#if>><a href="<@ofbizUrl>AddItemsFromOrder?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductOrderItems}</a></li>
        </#if>
        <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId="PURCHASE_RETURN">
            <li<#if selected="AddItemsFromInventory"> class="selected"</#if>><a href="<@ofbizUrl>AddItemsFromInventory?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductOrderItems}</a></li>
        </#if>
        <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId='PURCHASE_SHIPMENT' && shipment.destinationFacilityId?exists>
            <li<#if selected="ReceiveInventory"> class="selected"</#if>><a href="<@ofbizUrl>ReceiveInventory?shipmentId=${shipmentId}&facilityId=${shipment.destinationFacilityId?if_exists}<#if shipment.primaryOrderId?exists>&purchaseOrderId=${shipment.primaryOrderId}</#if></@ofbizUrl>">${uiLabelMap.ProductReceiveInventory}</a></li>
        </#if>
        <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId='PURCHASE_SHIPMENT' && shipment.destinationFacilityId?exists && shipment.primaryOrderId?exists>
            <li<#if selected="ProductReceiveInventoryAgainstPO"> class="selected"</#if>><a href="<@ofbizUrl>ReceiveInventoryAgainstPurchaseOrder?shipmentId=${shipmentId?if_exists}&purchaseOrderId=${shipment.primaryOrderId?if_exists}</@ofbizUrl>">${uiLabelMap.ProductReceiveInventoryAgainstPO}</a></li>
        </#if>
        <#if (shipment.shipmentTypeId)?exists && shipment.shipmentTypeId='SALES_SHIPMENT'>
            <li<#if selected="EditShipmentItems"> class="selected"</#if>><a href="<@ofbizUrl>EditShipmentItems?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductItems}</a></li>
            <li<#if selected="EditShipmentPackages"> class="selected"</#if>><a href="<@ofbizUrl>EditShipmentPackages?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductPackages}</a></li>
            <li<#if selected="EditShipmentRouteSegments"> class="selected"</#if>><a href="<@ofbizUrl>EditShipmentRouteSegments?shipmentId=${shipmentId}</@ofbizUrl>">${uiLabelMap.ProductRouteSegments}</a></li>
        </#if>
        </ul>
        <br/>
    </div>
</#if>
