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

import org.ofbiz.entity.*;
import org.ofbiz.base.util.*;

inventoryStock = [:];
shipmentId = parameters.shipmentId;
shipment = delegator.findByPrimaryKey("Shipment", [shipmentId : shipmentId]);

context.shipmentIdPar = shipment.shipmentId;
context.estimatedReadyDatePar = shipment.estimatedReadyDate;
context.estimatedShipDatePar = shipment.estimatedShipDate;

if (shipment) {
    shipmentPlans = delegator.findByAnd("OrderShipment", [shipmentId : shipmentId]);
    shipmentPlans.each { shipmentPlan ->
        orderLine = delegator.findByPrimaryKey("OrderItem", [orderId : shipmentPlan.orderId , orderItemSeqId : shipmentPlan.orderItemSeqId]);
        recordGroup = [:];
        recordGroup.ORDER_ID = shipmentPlan.orderId;
        recordGroup.ORDER_ITEM_SEQ_ID = shipmentPlan.orderItemSeqId;
        recordGroup.SHIPMENT_ID = shipmentPlan.shipmentId;
        recordGroup.SHIPMENT_ITEM_SEQ_ID = shipmentPlan.shipmentItemSeqId;

        recordGroup.PRODUCT_ID = orderLine.productId;
        recordGroup.QUANTITY = shipmentPlan.quantity;
        product = delegator.findByPrimaryKey("Product", [productId : orderLine.productId]);
        recordGroup.PRODUCT_NAME = product.internalName;

        inputPar = [productId : orderLine.productId,
                                     quantity : shipmentPlan.quantity,
                                     fromDate : "" + new Date(),
                                     userLogin: userLogin];

        result = [:];
        result = dispatcher.runSync("getNotAssembledComponents",inputPar);
        if (result)
            components = (List)result.get("notAssembledComponents");
        componentsIt = components.iterator();
        while (componentsIt) {
            oneComponent = (org.ofbiz.manufacturing.bom.BOMNode)componentsIt.next();
            record = new HashMap(recordGroup);
            record.componentId = oneComponent.getProduct().productId;
            record.componentName = oneComponent.getProduct().internalName;
            record.componentQuantity = new Float(oneComponent.getQuantity());
            facilityId = shipment.originFacilityId;
            float qty = 0;
            if (facilityId) {
                if (!inventoryStock.containsKey(oneComponent.getProduct().productId)) {
                    serviceInput = [productId : oneComponent.getProduct().productId , facilityId : facilityId];
                    serviceOutput = dispatcher.runSync("getInventoryAvailableByFacility",serviceInput);
                    qha = serviceOutput.quantityOnHandTotal ?: 0.0;
                    inventoryStock.oneComponent.getProduct().productId = qha;
                }
                qty = inventoryStock[oneComponent.getProduct().productId];
                qty = qty - oneComponent.getQuantity();
                inventoryStock.oneComponent.getProduct().productId = qty;
            }
            record.componentOnHand = qty;
            // Now we get the products qty already reserved by production runs
            serviceInput = [productId : oneComponent.getProduct().productId,
                                          userLogin : userLogin];
            serviceOutput = dispatcher.runSync("getProductionRunTotResQty", serviceInput);
            resQty = serviceOutput.reservedQuantity;
            record.reservedQuantity = resQty;
            records.add(record);
        }
    }
    context.records = records;

    // check permission
    hasPermission = false;
    if (security.hasEntityPermission("MANUFACTURING", "_VIEW", session)) {
        hasPermission = true;
    }
    context.hasPermission = hasPermission;
}

return "success";
