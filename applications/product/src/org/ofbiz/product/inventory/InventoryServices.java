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
package org.ofbiz.product.inventory;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.UtilDateTime;
import org.ofbiz.base.util.UtilGenerics;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityConditionList;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.model.DynamicViewEntity;
import org.ofbiz.entity.model.ModelKeyMap;
import org.ofbiz.entity.util.EntityListIterator;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * Inventory Services
 */
public class InventoryServices {

    public final static String module = InventoryServices.class.getName();

    public static final MathContext generalRounding = new MathContext(10);

    public static Map<String, Object> prepareInventoryTransfer(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        String inventoryItemId = (String) context.get("inventoryItemId");
        BigDecimal xferQty = (BigDecimal) context.get("xferQty");
        GenericValue inventoryItem = null;
        GenericValue newItem = null;
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        try {
            inventoryItem = delegator.findByPrimaryKey("InventoryItem", UtilMisc.toMap("inventoryItemId", inventoryItemId));
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Inventory item lookup problem [" + e.getMessage() + "]");
        }

        if (inventoryItem == null) {
            return ServiceUtil.returnError("Cannot locate inventory item.");
        }

        try {
            Map<String, Object> results = ServiceUtil.returnSuccess();

            String inventoryType = inventoryItem.getString("inventoryItemTypeId");
            if (inventoryType.equals("NON_SERIAL_INV_ITEM")) {
                BigDecimal atp = inventoryItem.getBigDecimal("availableToPromiseTotal");
                BigDecimal qoh = inventoryItem.getBigDecimal("quantityOnHandTotal");

                if (atp == null) {
                    return ServiceUtil.returnError("The request transfer amount is not available, there is no available to promise on the Inventory Item with ID " + inventoryItem.getString("inventoryItemId"));
                }
                if (qoh == null) {
                    qoh = atp;
                }

                // first make sure we have enough to cover the request transfer amount
                if (xferQty.compareTo(atp) > 0) {
                    return ServiceUtil.returnError("The request transfer amount is not available, the available to promise [" + atp + "] is not sufficient for the desired transfer quantity [" + xferQty + "] on the Inventory Item with ID " + inventoryItem.getString("inventoryItemId"));
                }

                /*
                 * atp < qoh - split and save the qoh - atp
                 * xferQty < atp - split and save atp - xferQty
                 * atp < qoh && xferQty < atp - split and save qoh - atp + atp - xferQty
                 */

                // at this point we have already made sure that the xferQty is less than or equals to the atp, so if less that just create a new inventory record for the quantity to be moved
                // NOTE: atp should always be <= qoh, so if xfer < atp, then xfer < qoh, so no need to check/handle that
                // however, if atp < qoh && atp == xferQty, then we still need to split; oh, but no need to check atp == xferQty in the second part because if it isn't greater and isn't less, then it is equal
                if (xferQty.compareTo(atp) < 0 || atp.compareTo(qoh) < 0) {
                    BigDecimal negXferQty = xferQty.negate();
                    // NOTE: new inventory items should always be created calling the
                    //       createInventoryItem service because in this way we are sure
                    //       that all the relevant fields are filled with default values.
                    //       However, the code here should work fine because all the values
                    //       for the new inventory item are inerited from the existing item.
                    newItem = GenericValue.create(inventoryItem);
                    newItem.set("availableToPromiseTotal", BigDecimal.ZERO);
                    newItem.set("quantityOnHandTotal", BigDecimal.ZERO);

                    delegator.createSetNextSeqId(newItem);

                    results.put("inventoryItemId", newItem.get("inventoryItemId"));

                    // TODO: how do we get this here: "inventoryTransferId", inventoryTransferId
                    Map<String, Object> createNewDetailMap = UtilMisc.toMap("availableToPromiseDiff", xferQty, "quantityOnHandDiff", xferQty,
                            "inventoryItemId", newItem.get("inventoryItemId"), "userLogin", userLogin);
                    Map<String, Object> createUpdateDetailMap = UtilMisc.toMap("availableToPromiseDiff", negXferQty, "quantityOnHandDiff", negXferQty,
                            "inventoryItemId", inventoryItem.get("inventoryItemId"), "userLogin", userLogin);

                    try {
                        Map<String, Object> resultNew = dctx.getDispatcher().runSync("createInventoryItemDetail", createNewDetailMap);
                        if (ServiceUtil.isError(resultNew)) {
                            return ServiceUtil.returnError("Inventory Item Detail create problem in prepare inventory transfer", null, null, resultNew);
                        }
                        Map<String, Object> resultUpdate = dctx.getDispatcher().runSync("createInventoryItemDetail", createUpdateDetailMap);
                        if (ServiceUtil.isError(resultNew)) {
                            return ServiceUtil.returnError("Inventory Item Detail create problem in prepare inventory transfer", null, null, resultUpdate);
                        }
                    } catch (GenericServiceException e1) {
                        return ServiceUtil.returnError("Inventory Item Detail create problem in prepare inventory transfer: [" + e1.getMessage() + "]");
                    }
                } else {
                    results.put("inventoryItemId", inventoryItem.get("inventoryItemId"));
                }
            } else if (inventoryType.equals("SERIALIZED_INV_ITEM")) {
                if (!"INV_AVAILABLE".equals(inventoryItem.getString("statusId"))) {
                    return ServiceUtil.returnError("Serialized inventory is not available for transfer.");
                }
            }

            // setup values so that no one will grab the inventory during the move
            // if newItem is not null, it is the item to be moved, otherwise the original inventoryItem is the one to be moved
            if (inventoryType.equals("NON_SERIAL_INV_ITEM")) {
                // set the transfered inventory item's atp to 0 and the qoh to the xferQty; at this point atp and qoh will always be the same, so we can safely zero the atp for now
                GenericValue inventoryItemToClear = newItem == null ? inventoryItem : newItem;

                inventoryItemToClear.refresh();
                BigDecimal atp = inventoryItemToClear.get("availableToPromiseTotal") == null ? BigDecimal.ZERO : inventoryItemToClear.getBigDecimal("availableToPromiseTotal");
                if (atp.compareTo(BigDecimal.ZERO) != 0) {
                    Map<String, Object> createDetailMap = UtilMisc.toMap("availableToPromiseDiff", atp.negate(),
                            "inventoryItemId", inventoryItemToClear.get("inventoryItemId"), "userLogin", userLogin);
                    try {
                        Map<String, Object> result = dctx.getDispatcher().runSync("createInventoryItemDetail", createDetailMap);
                        if (ServiceUtil.isError(result)) {
                            return ServiceUtil.returnError("Inventory Item Detail create problem in complete inventory transfer", null, null, result);
                        }
                    } catch (GenericServiceException e1) {
                        return ServiceUtil.returnError("Inventory Item Detail create problem in complete inventory transfer: [" + e1.getMessage() + "]");
                    }
                }
            } else if (inventoryType.equals("SERIALIZED_INV_ITEM")) {
                // set the status to avoid re-moving or something
              if (newItem != null) {
                    newItem.refresh();
                    newItem.set("statusId", "INV_BEING_TRANSFERED");
                    newItem.store();
                    results.put("inventoryItemId", newItem.get("inventoryItemId"));
              } else {
                    inventoryItem.refresh();
                    inventoryItem.set("statusId", "INV_BEING_TRANSFERED");
                    inventoryItem.store();
                    results.put("inventoryItemId", inventoryItem.get("inventoryItemId"));
              }
            }

            return results;
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Inventory store/create problem [" + e.getMessage() + "]");
        }
    }

    public static Map<String, Object> completeInventoryTransfer(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        String inventoryTransferId = (String) context.get("inventoryTransferId");
        GenericValue inventoryTransfer = null;
        GenericValue inventoryItem = null;
        GenericValue destinationFacility = null;
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        try {
            inventoryTransfer = delegator.findByPrimaryKey("InventoryTransfer",
                    UtilMisc.toMap("inventoryTransferId", inventoryTransferId));
            inventoryItem = inventoryTransfer.getRelatedOne("InventoryItem");
            destinationFacility = inventoryTransfer.getRelatedOne("ToFacility");
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Inventory Item/Transfer lookup problem [" + e.getMessage() + "]");
        }

        if (inventoryTransfer == null || inventoryItem == null) {
            return ServiceUtil.returnError("ERROR: Lookup of InventoryTransfer and/or InventoryItem failed!");
        }

        String inventoryType = inventoryItem.getString("inventoryItemTypeId");

        // set the fields on the transfer record
        if (inventoryTransfer.get("receiveDate") == null) {
            inventoryTransfer.set("receiveDate", UtilDateTime.nowTimestamp());
        }

        if (inventoryType.equals("NON_SERIAL_INV_ITEM")) {
            // add an adjusting InventoryItemDetail so set ATP back to QOH: ATP = ATP + (QOH - ATP), diff = QOH - ATP
            BigDecimal atp = inventoryItem.get("availableToPromiseTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("availableToPromiseTotal");
            BigDecimal qoh = inventoryItem.get("quantityOnHandTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("quantityOnHandTotal");
            Map<String, Object> createDetailMap = UtilMisc.toMap("availableToPromiseDiff", qoh.subtract(atp),
                    "inventoryItemId", inventoryItem.get("inventoryItemId"), "userLogin", userLogin);
            try {
                Map<String, Object> result = dctx.getDispatcher().runSync("createInventoryItemDetail", createDetailMap);
                if (ServiceUtil.isError(result)) {
                    return ServiceUtil.returnError("Inventory Item Detail create problem in complete inventory transfer", null, null, result);
                }
            } catch (GenericServiceException e1) {
                return ServiceUtil.returnError("Inventory Item Detail create problem in complete inventory transfer: [" + e1.getMessage() + "]");
            }
            try {
                inventoryItem.refresh();
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError("Inventory refresh problem [" + e.getMessage() + "]");
            }
        }

        // set the fields on the item
        Map<String, Object> updateInventoryItemMap = UtilMisc.toMap("inventoryItemId", inventoryItem.getString("inventoryItemId"),
                                                    "facilityId", inventoryTransfer.get("facilityIdTo"),
                                                    "containerId", inventoryTransfer.get("containerIdTo"),
                                                    "locationSeqId", inventoryTransfer.get("locationSeqIdTo"),
                                                    "userLogin", userLogin);

        // for serialized items, automatically make them available
        if (inventoryType.equals("SERIALIZED_INV_ITEM")) {
            updateInventoryItemMap.put("statusId", "INV_AVAILABLE");
        }

        // if the destination facility's owner is different
        // from the inventory item's ownwer,
        // the inventory item is assigned to the new owner.
        if (destinationFacility != null && destinationFacility.get("ownerPartyId") != null) {
            String fromPartyId = inventoryItem.getString("ownerPartyId");
            String toPartyId = destinationFacility.getString("ownerPartyId");
            if (fromPartyId == null || !fromPartyId.equals(toPartyId)) {
                updateInventoryItemMap.put("ownerPartyId", toPartyId);
            }
        }
        try {
            Map<String, Object> result = dctx.getDispatcher().runSync("updateInventoryItem", updateInventoryItemMap);
            if (ServiceUtil.isError(result)) {
                return ServiceUtil.returnError("Inventory item store problem", null, null, result);
            }
        } catch (GenericServiceException exc) {
            return ServiceUtil.returnError("Inventory item store problem [" + exc.getMessage() + "]");
        }

        // set the inventory transfer record to complete
        inventoryTransfer.set("statusId", "IXF_COMPLETE");

        // store the entities
        try {
            inventoryTransfer.store();
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Inventory store problem [" + e.getMessage() + "]");
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object> cancelInventoryTransfer(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        String inventoryTransferId = (String) context.get("inventoryTransferId");
        GenericValue inventoryTransfer = null;
        GenericValue inventoryItem = null;
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        try {
            inventoryTransfer = delegator.findByPrimaryKey("InventoryTransfer",
                    UtilMisc.toMap("inventoryTransferId", inventoryTransferId));
            if (UtilValidate.isEmpty(inventoryTransfer)) {
                return ServiceUtil.returnError("Inventory transfer [" + inventoryTransferId + "] not found");
            }
            inventoryItem = inventoryTransfer.getRelatedOne("InventoryItem");
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Inventory Item/Transfer lookup problem [" + e.getMessage() + "]");
        }

        if (inventoryTransfer == null || inventoryItem == null) {
            return ServiceUtil.returnError("ERROR: Lookup of InventoryTransfer and/or InventoryItem failed!");
        }

        String inventoryType = inventoryItem.getString("inventoryItemTypeId");

        // re-set the fields on the item
        if (inventoryType.equals("NON_SERIAL_INV_ITEM")) {
            // add an adjusting InventoryItemDetail so set ATP back to QOH: ATP = ATP + (QOH - ATP), diff = QOH - ATP
            BigDecimal atp = inventoryItem.get("availableToPromiseTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("availableToPromiseTotal");
            BigDecimal qoh = inventoryItem.get("quantityOnHandTotal") == null ? BigDecimal.ZERO : inventoryItem.getBigDecimal("quantityOnHandTotal");
            Map<String, Object> createDetailMap = UtilMisc.toMap("availableToPromiseDiff", qoh.subtract(atp),
                                                 "inventoryItemId", inventoryItem.get("inventoryItemId"),
                                                 "userLogin", userLogin);
            try {
                Map<String, Object> result = dctx.getDispatcher().runSync("createInventoryItemDetail", createDetailMap);
                if (ServiceUtil.isError(result)) {
                    return ServiceUtil.returnError("Inventory Item Detail create problem in cancel inventory transfer", null, null, result);
                }
            } catch (GenericServiceException e1) {
                return ServiceUtil.returnError("Inventory Item Detail create problem in cancel inventory transfer: [" + e1.getMessage() + "]");
            }
        } else if (inventoryType.equals("SERIALIZED_INV_ITEM")) {
            inventoryItem.set("statusId", "INV_AVAILABLE");
            // store the entity
            try {
                inventoryItem.store();
            } catch (GenericEntityException e) {
                return ServiceUtil.returnError("Inventory item store problem in cancel inventory transfer: [" + e.getMessage() + "]");
            }
        }

        // set the inventory transfer record to complete
        inventoryTransfer.set("statusId", "IXF_CANCELLED");

        // store the entities
        try {
            inventoryTransfer.store();
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Inventory store problem [" + e.getMessage() + "]");
        }

        return ServiceUtil.returnSuccess();
    }

    /** In spite of the generic name this does the very specific task of checking availability of all back-ordered items and sends notices, etc */
    public static Map<String, Object> checkInventoryAvailability(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericValue userLogin = (GenericValue) context.get("userLogin");

        /* TODO: NOTE: This method has been updated, but testing requires many eyes. See http://jira.undersunconsulting.com/browse/OFBIZ-662
        boolean skipThisNeedsUpdating = true;
        if (skipThisNeedsUpdating) {
            Debug.logWarning("NOT Running the checkInventoryAvailability service, no backorders or such will be automatically created; the reason is that this serice needs to be updated to use OrderItemShipGroup instead of OrderShipmentPreference which it currently does.", module);
            return ServiceUtil.returnSuccess();
        }
        */

        Map<String, Map<String, Timestamp>> ordersToUpdate = FastMap.newInstance();
        Map<String, Map<String, Timestamp>> ordersToCancel = FastMap.newInstance();

        // find all inventory items w/ a negative ATP
        List<GenericValue> inventoryItems = null;
        try {
            EntityExpr ee = EntityCondition.makeCondition("availableToPromiseTotal", EntityOperator.LESS_THAN, BigDecimal.ZERO);
            inventoryItems = delegator.findList("InventoryItem", ee, null, null, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, "Trouble getting inventory items", module);
            return ServiceUtil.returnError("Problem getting InventoryItem records");
        }

        if (inventoryItems == null) {
            Debug.logInfo("No items out of stock; no backorders to worry about", module);
            return ServiceUtil.returnSuccess();
        }

        Debug.log("OOS Inventory Items: " + inventoryItems.size(), module);

        for (GenericValue inventoryItem: inventoryItems) {
            // get the incomming shipment information for the item
            List<GenericValue> shipmentAndItems = null;
            try {
                List<EntityExpr> exprs = FastList.newInstance();
                exprs.add(EntityCondition.makeCondition("productId", EntityOperator.EQUALS, inventoryItem.get("productId")));
                exprs.add(EntityCondition.makeCondition("destinationFacilityId", EntityOperator.EQUALS, inventoryItem.get("facilityId")));
                exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "SHIPMENT_DELIVERED"));
                exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "SHIPMENT_CANCELLED"));

                EntityConditionList<EntityExpr> ecl = EntityCondition.makeCondition(exprs, EntityOperator.AND);
                shipmentAndItems = delegator.findList("ShipmentAndItem", ecl, null, UtilMisc.toList("estimatedArrivalDate"), null, false);
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting ShipmentAndItem records", module);
                return ServiceUtil.returnError("Problem getting ShipmentAndItem records");
            }

            // get the reservations in order of newest first
            List<GenericValue> reservations = null;
            try {
                reservations = inventoryItem.getRelated("OrderItemShipGrpInvRes", null, UtilMisc.toList("-reservedDatetime"));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Problem getting related reservations", module);
                return ServiceUtil.returnError("Problem getting related reservations");
            }

            if (reservations == null) {
                Debug.logWarning("No outstanding reservations for this inventory item, why is it negative then?", module);
                continue;
            }

            Debug.log("Reservations for item: " + reservations.size(), module);

            // available at the time of order
            BigDecimal availableBeforeReserved = inventoryItem.getBigDecimal("availableToPromiseTotal");

            // go through all the reservations in order
            for (GenericValue reservation: reservations) {
                String orderId = reservation.getString("orderId");
                String orderItemSeqId = reservation.getString("orderItemSeqId");
                Timestamp promisedDate = reservation.getTimestamp("promisedDatetime");
                Timestamp currentPromiseDate = reservation.getTimestamp("currentPromisedDate");
                Timestamp actualPromiseDate = currentPromiseDate;
                if (actualPromiseDate == null) {
                    if (promisedDate != null) {
                        actualPromiseDate = promisedDate;
                    } else {
                        // fall back if there is no promised date stored
                        actualPromiseDate = reservation.getTimestamp("reservedDatetime");
                    }
                }

                Debug.log("Promised Date: " + actualPromiseDate, module);

                // find the next possible ship date
                Timestamp nextShipDate = null;
                BigDecimal availableAtTime = BigDecimal.ZERO;
                for (GenericValue shipmentItem: shipmentAndItems) {
                    availableAtTime = availableAtTime.add(shipmentItem.getBigDecimal("quantity"));
                    if (availableAtTime.compareTo(availableBeforeReserved) >= 0) {
                        nextShipDate = shipmentItem.getTimestamp("estimatedArrivalDate");
                        break;
                    }
                }

                Debug.log("Next Ship Date: " + nextShipDate, module);

                // create a modified promise date (promise date - 1 day)
                Calendar pCal = Calendar.getInstance();
                pCal.setTimeInMillis(actualPromiseDate.getTime());
                pCal.add(Calendar.DAY_OF_YEAR, -1);
                Timestamp modifiedPromisedDate = new Timestamp(pCal.getTimeInMillis());
                Timestamp now = UtilDateTime.nowTimestamp();

                Debug.log("Promised Date + 1: " + modifiedPromisedDate, module);
                Debug.log("Now: " + now, module);

                // check the promised date vs the next ship date
                if (nextShipDate == null || nextShipDate.after(actualPromiseDate)) {
                    if (nextShipDate == null && modifiedPromisedDate.after(now)) {
                        // do nothing; we are okay to assume it will be shipped on time
                        Debug.log("No ship date known yet, but promised date hasn't approached, assuming it will be here on time", module);
                    } else {
                        // we cannot ship by the promised date; need to notify the customer
                        Debug.log("We won't ship on time, getting notification info", module);
                        Map<String, Timestamp> notifyItems = ordersToUpdate.get(orderId);
                        if (notifyItems == null) {
                            notifyItems = FastMap.newInstance();
                        }
                        notifyItems.put(orderItemSeqId, nextShipDate);
                        ordersToUpdate.put(orderId, notifyItems);

                        // need to know if nextShipDate is more then 30 days after promised
                        Calendar sCal = Calendar.getInstance();
                        sCal.setTimeInMillis(actualPromiseDate.getTime());
                        sCal.add(Calendar.DAY_OF_YEAR, 30);
                        Timestamp farPastPromised = new Timestamp(sCal.getTimeInMillis());

                        // check to see if this is >30 days or second run, if so flag to cancel
                        boolean needToCancel = false;
                        if (nextShipDate == null || nextShipDate.after(farPastPromised)) {
                            // we cannot ship until >30 days after promised; using cancel rule
                            Debug.log("Ship date is >30 past the promised date", module);
                            needToCancel = true;
                        } else if (currentPromiseDate != null && actualPromiseDate.equals(currentPromiseDate)) {
                            // this is the second notification; using cancel rule
                            needToCancel = true;
                        }

                        // add the info to the cancel map if we need to schedule a cancel
                        if (needToCancel) {
                            // queue the item to be cancelled
                            Debug.log("Flagging the item to auto-cancel", module);
                            Map<String, Timestamp> cancelItems = ordersToCancel.get(orderId);
                            if (cancelItems == null) {
                                cancelItems = FastMap.newInstance();
                            }
                            cancelItems.put(orderItemSeqId, farPastPromised);
                            ordersToCancel.put(orderId, cancelItems);
                        }

                        // store the updated promiseDate as the nextShipDate
                        try {
                            reservation.set("currentPromisedDate", nextShipDate);
                            reservation.store();
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "Problem storing reservation : " + reservation, module);
                        }
                    }
                }

                // subtract our qty from reserved to get the next value
                availableBeforeReserved = availableBeforeReserved.subtract(reservation.getBigDecimal("quantity"));
            }
        }

        // all items to cancel will also be in the notify list so start with that
        List<String> ordersToNotify = FastList.newInstance();
        for (Map.Entry<String, Map<String, Timestamp>> entry: ordersToUpdate.entrySet()) {
            String orderId = entry.getKey();
            Map<String, Timestamp> backOrderedItems = entry.getValue();
            Map<String, Timestamp> cancelItems = ordersToCancel.get(orderId);
            boolean cancelAll = false;
            Timestamp cancelAllTime = null;

            List<GenericValue> orderItemShipGroups = null;
            try {
                orderItemShipGroups= delegator.findByAnd("OrderItemShipGroup",
                        UtilMisc.toMap("orderId", orderId));
            } catch (GenericEntityException e) {
                Debug.logError(e, "Cannot get OrderItemShipGroups from orderId" + orderId, module);
            }

            for (GenericValue orderItemShipGroup: orderItemShipGroups) {
                List<GenericValue> orderItems = FastList.newInstance();
                List<GenericValue> orderItemShipGroupAssoc = null;
                try {
                    orderItemShipGroupAssoc =
                        delegator.findByAnd("OrderItemShipGroupAssoc",
                                UtilMisc.toMap("shipGroupSeqId",
                                        orderItemShipGroup.get("shipGroupSeqId"),
                                        "orderId",
                                        orderId));

                    for (GenericValue assoc: orderItemShipGroupAssoc) {
                        GenericValue orderItem = assoc.getRelatedOne("OrderItem");
                        if (orderItem != null) {
                            orderItems.add(orderItem);
                        }
                    }
                } catch (GenericEntityException e) {
                     Debug.logError(e, "Problem fetching OrderItemShipGroupAssoc", module);
                }


                /* Check the split preference. */
                boolean maySplit = false;
                if (orderItemShipGroup != null && orderItemShipGroup.get("maySplit") != null) {
                    maySplit = orderItemShipGroup.getBoolean("maySplit").booleanValue();
                }

                /* Figure out if we must cancel all items. */
                if (!maySplit && cancelItems != null) {
                    cancelAll = true;
                    Set<String> cancelSet = cancelItems.keySet();
                    cancelAllTime = cancelItems.get(cancelSet.iterator().next());
                }

                // if there are none to cancel just create an empty map
                if (cancelItems == null) {
                    cancelItems = FastMap.newInstance();
                }

                if (orderItems != null) {
                    List<GenericValue> toBeStored = FastList.newInstance();
                    for (GenericValue orderItem: orderItems) {
                        String orderItemSeqId = orderItem.getString("orderItemSeqId");
                        Timestamp shipDate = (Timestamp) backOrderedItems.get(orderItemSeqId);
                        Timestamp cancelDate = (Timestamp) cancelItems.get(orderItemSeqId);
                        Timestamp currentCancelDate = orderItem.getTimestamp("autoCancelDate");

                        Debug.logError("OI: " + orderId + " SEQID: "+ orderItemSeqId + " cancelAll: " + cancelAll + " cancelDate: " + cancelDate, module);
                        if (backOrderedItems.containsKey(orderItemSeqId)) {
                            orderItem.set("estimatedShipDate", shipDate);

                            if (currentCancelDate == null) {
                                if (cancelAll || cancelDate != null) {
                                    if (orderItem.get("dontCancelSetUserLogin") == null && orderItem.get("dontCancelSetDate") == null) {
                                        if (cancelAllTime != null) {
                                            orderItem.set("autoCancelDate", cancelAllTime);
                                        } else {
                                            orderItem.set("autoCancelDate", cancelDate);
                                        }
                                    }
                                }
                                // only notify orders which have not already sent the final notice
                                ordersToNotify.add(orderId);
                            }
                            toBeStored.add(orderItem);
                        }
                    }
                    if (toBeStored.size() > 0) {
                        try {
                            delegator.storeAll(toBeStored);
                        } catch (GenericEntityException e) {
                            Debug.logError(e, "Problem storing order items", module);
                        }
                    }
                }


            }
        }

        // send off a notification for each order
        for (String orderId: ordersToNotify) {
            try {
                dispatcher.runAsync("sendOrderBackorderNotification", UtilMisc.<String, Object>toMap("orderId", orderId, "userLogin", userLogin));
            } catch (GenericServiceException e) {
                Debug.logError(e, "Problems sending off the notification", module);
                continue;
            }
        }

        return ServiceUtil.returnSuccess();
    }

    /**
     * Get Inventory Available for a Product based on the list of associated products.  The final ATP and QOH will
     * be the minimum of all the associated products' inventory divided by their ProductAssoc.quantity
     * */
    public static Map<String, Object> getProductInventoryAvailableFromAssocProducts(DispatchContext dctx, Map<String, ? extends Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        List<GenericValue> productAssocList = UtilGenerics.checkList(context.get("assocProducts"));
        String facilityId = (String)context.get("facilityId");
        String statusId = (String)context.get("statusId");

        BigDecimal availableToPromiseTotal = BigDecimal.ZERO;
        BigDecimal quantityOnHandTotal = BigDecimal.ZERO;

        if (UtilValidate.isNotEmpty(productAssocList)) {
            // minimum QOH and ATP encountered
            BigDecimal minQuantityOnHandTotal = null;
            BigDecimal minAvailableToPromiseTotal = null;

           // loop through each associated product.
           for (GenericValue productAssoc: productAssocList) {
               String productIdTo = productAssoc.getString("productIdTo");
               BigDecimal assocQuantity = productAssoc.getBigDecimal("quantity");

               // if there is no quantity for the associated product in ProductAssoc entity, default it to 1.0
               if (assocQuantity == null) {
                   Debug.logWarning("ProductAssoc from [" + productAssoc.getString("productId") + "] to [" + productAssoc.getString("productIdTo") + "] has no quantity, assuming 1.0", module);
                   assocQuantity = BigDecimal.ONE;
               }

               // figure out the inventory available for this associated product
               Map<String, Object> resultOutput = null;
               try {
                   Map<String, String> inputMap = UtilMisc.toMap("productId", productIdTo, "statusId", statusId);
                   if (facilityId != null) {
                       inputMap.put("facilityId", facilityId);
                       resultOutput = dispatcher.runSync("getInventoryAvailableByFacility", inputMap);
                   } else {
                       resultOutput = dispatcher.runSync("getProductInventoryAvailable", inputMap);
                   }
               } catch (GenericServiceException e) {
                  Debug.logError(e, "Problems getting inventory available by facility", module);
                  return ServiceUtil.returnError(e.getMessage());
               }

               // Figure out what the QOH and ATP inventory would be with this associated product
               BigDecimal currentQuantityOnHandTotal = (BigDecimal) resultOutput.get("quantityOnHandTotal");
               BigDecimal currentAvailableToPromiseTotal = (BigDecimal) resultOutput.get("availableToPromiseTotal");
               BigDecimal tmpQuantityOnHandTotal = currentQuantityOnHandTotal.divide(assocQuantity, generalRounding);
               BigDecimal tmpAvailableToPromiseTotal = currentAvailableToPromiseTotal.divide(assocQuantity, generalRounding);

               // reset the minimum QOH and ATP quantities if those quantities for this product are less
               if (minQuantityOnHandTotal == null || tmpQuantityOnHandTotal.compareTo(minQuantityOnHandTotal) < 0) {
                   minQuantityOnHandTotal = tmpQuantityOnHandTotal;
               }
               if (minAvailableToPromiseTotal == null || tmpAvailableToPromiseTotal.compareTo(minAvailableToPromiseTotal) < 0) {
                   minAvailableToPromiseTotal = tmpAvailableToPromiseTotal;
               }

               if (Debug.verboseOn()) {
                   Debug.logVerbose("productIdTo = " + productIdTo + " assocQuantity = " + assocQuantity + "current QOH " + currentQuantityOnHandTotal +
                        "currentATP = " + currentAvailableToPromiseTotal + " minQOH = " + minQuantityOnHandTotal + " minATP = " + minAvailableToPromiseTotal, module);
               }
           }
          // the final QOH and ATP quantities are the minimum of all the products
          quantityOnHandTotal = minQuantityOnHandTotal;
          availableToPromiseTotal = minAvailableToPromiseTotal;
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("availableToPromiseTotal", availableToPromiseTotal);
        result.put("quantityOnHandTotal", quantityOnHandTotal);
        return result;
    }


    public static Map<String, Object> getProductInventorySummaryForItems(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        List<GenericValue> orderItems = UtilGenerics.checkList(context.get("orderItems"));
        String facilityId = (String) context.get("facilityId");
        Map<String, BigDecimal> atpMap = FastMap.newInstance();
        Map<String, BigDecimal> qohMap = FastMap.newInstance();
        Map<String, BigDecimal> mktgPkgAtpMap = FastMap.newInstance();
        Map<String, BigDecimal> mktgPkgQohMap = FastMap.newInstance();
        Map<String, Object> results = ServiceUtil.returnSuccess();

        // get a list of all available facilities for looping
        List<GenericValue> facilities = null;
        try {
            if (facilityId != null) {
                facilities = delegator.findByAnd("Facility", UtilMisc.toMap("facilityId", facilityId));
            } else {
                facilities = delegator.findList("Facility", null, null, null, null, false);
            }
        } catch (GenericEntityException e) {
            return ServiceUtil.returnError("Unable to locate facilities." + e.getMessage());
        }

        // loop through all the order items
        for (GenericValue orderItem: orderItems) {
            String productId = orderItem.getString("productId");

            if ((productId == null) || productId.equals("")) continue;

            GenericValue product = null;
            try {
                product = orderItem.getRelatedOneCache("Product");
            } catch (GenericEntityException e) {
                Debug.logError(e, "Couldn't get product.", module);
                return ServiceUtil.returnError("Unable to retrive product with id [" + productId + "]");
            }

            BigDecimal atp = BigDecimal.ZERO;
            BigDecimal qoh = BigDecimal.ZERO;
            BigDecimal mktgPkgAtp = BigDecimal.ZERO;
            BigDecimal mktgPkgQoh = BigDecimal.ZERO;

            // loop through all the facilities
            for (GenericValue facility: facilities) {
                Map<String, Object> invResult = null;
                Map<String, Object> mktgPkgInvResult = null;

                // get both the real ATP/QOH available and the quantities available from marketing packages
                try {
                    if ("MARKETING_PKG_AUTO".equals(product.getString("productTypeId")) ||
                            "MARKETING_PKG_PICK".equals(product.getString("productTypeId"))) {
                        mktgPkgInvResult = dispatcher.runSync("getMktgPackagesAvailable", UtilMisc.toMap("productId", productId, "facilityId", facility.getString("facilityId")));
                    }
                    invResult = dispatcher.runSync("getInventoryAvailableByFacility", UtilMisc.toMap("productId", productId, "facilityId", facility.getString("facilityId")));
                } catch (GenericServiceException e) {
                    String msg = "Could not find inventory for facility [" + facility.getString("facilityId") + "]";
                    Debug.logError(e, msg, module);
                    return ServiceUtil.returnError(msg);
                }

                // add the results for this facility to the ATP/QOH counter for all facilities
                if (!ServiceUtil.isError(invResult)) {
                    BigDecimal fatp = (BigDecimal) invResult.get("availableToPromiseTotal");
                    BigDecimal fqoh = (BigDecimal) invResult.get("quantityOnHandTotal");
                    if (fatp != null) atp = atp.add(fatp);
                    if (fqoh != null) qoh = qoh.add(fqoh);
                }
                if (("MARKETING_PKG_AUTO".equals(product.getString("productTypeId")) || "MARKETING_PKG_PICK".equals(product.getString("productTypeId"))) && (!ServiceUtil.isError(mktgPkgInvResult))) {
                    BigDecimal fatp = (BigDecimal) mktgPkgInvResult.get("availableToPromiseTotal");
                    BigDecimal fqoh = (BigDecimal) mktgPkgInvResult.get("quantityOnHandTotal");
                    if (fatp != null) mktgPkgAtp = mktgPkgAtp.add(fatp);
                    if (fqoh != null) mktgPkgQoh = mktgPkgQoh.add(fqoh);
                }
            }

            atpMap.put(productId, atp);
            qohMap.put(productId, qoh);
            mktgPkgAtpMap.put(productId, mktgPkgAtp);
            mktgPkgQohMap.put(productId, mktgPkgQoh);
        }

        results.put("availableToPromiseMap", atpMap);
        results.put("quantityOnHandMap", qohMap);
        results.put("mktgPkgATPMap", mktgPkgAtpMap);
        results.put("mktgPkgQOHMap", mktgPkgQohMap);
        return results;
    }


    public static Map<String, Object> getProductInventoryAndFacilitySummary(DispatchContext dctx, Map<String, ? extends Object> context) {
        GenericDelegator delegator = dctx.getDelegator();
        LocalDispatcher dispatcher = dctx.getDispatcher();
        Timestamp checkTime = (Timestamp)context.get("checkTime");
        String facilityId = (String)context.get("facilityId");
        String productId = (String)context.get("productId");
        String minimumStockStr = (String)context.get("minimumStock");
        String statusId = (String)context.get("statusId");

        Map<String, Object> result = FastMap.newInstance();
        Map<String, Object> resultOutput = FastMap.newInstance();

        Map<String, String> contextInput = UtilMisc.toMap("productId", productId, "facilityId", facilityId, "statusId", statusId);
        GenericValue product = null;
        try {
            product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
        } catch (GenericEntityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if ("MARKETING_PKG_AUTO".equals(product.getString("productTypeId")) ||
                "MARKETING_PKG_PICK".equals(product.getString("productTypeId"))) {
            try {
                resultOutput = dispatcher.runSync("getMktgPackagesAvailable", contextInput);
            } catch (GenericServiceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                resultOutput = dispatcher.runSync("getInventoryAvailableByFacility", contextInput);
            } catch (GenericServiceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        // filter for quantities
        BigDecimal minimumStock = BigDecimal.ZERO;
        if (minimumStockStr != null) {
            minimumStock = new BigDecimal(minimumStockStr);
        }

        BigDecimal quantityOnHandTotal = BigDecimal.ZERO;
        if (resultOutput.get("quantityOnHandTotal") != null) {
            quantityOnHandTotal = (BigDecimal)resultOutput.get("quantityOnHandTotal");
        }
        BigDecimal offsetQOHQtyAvailable = quantityOnHandTotal.subtract(minimumStock);

        BigDecimal availableToPromiseTotal = BigDecimal.ZERO;
        if (resultOutput.get("availableToPromiseTotal") != null) {
            availableToPromiseTotal = (BigDecimal)resultOutput.get("availableToPromiseTotal");
        }
        BigDecimal offsetATPQtyAvailable = availableToPromiseTotal.subtract(minimumStock);

        BigDecimal quantityOnOrder = InventoryWorker.getOutstandingPurchasedQuantity(productId, delegator);
        result.put("totalQuantityOnHand", resultOutput.get("quantityOnHandTotal").toString());
        result.put("totalAvailableToPromise", resultOutput.get("availableToPromiseTotal").toString());
        result.put("quantityOnOrder", quantityOnOrder);

        result.put("offsetQOHQtyAvailable", offsetQOHQtyAvailable);
        result.put("offsetATPQtyAvailable", offsetATPQtyAvailable);

        List<GenericValue> productPrices = null;
        try {
            productPrices = delegator.findByAndCache("ProductPrice", UtilMisc.toMap("productId",productId), UtilMisc.toList("-fromDate"));
        } catch (GenericEntityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //change this for product price
        for (GenericValue onePrice: productPrices) {
            if (onePrice.getString("productPriceTypeId").equals("DEFAULT_PRICE")) { //defaultPrice
                result.put("defultPrice", onePrice.getBigDecimal("price"));
            } else if (onePrice.getString("productPriceTypeId").equals("WHOLESALE_PRICE")) {//
                result.put("wholeSalePrice", onePrice.getBigDecimal("price"));
            } else if (onePrice.getString("productPriceTypeId").equals("LIST_PRICE")) {//listPrice
                result.put("listPrice", onePrice.getBigDecimal("price"));
            } else {
                result.put("defultPrice", onePrice.getBigDecimal("price"));
                result.put("listPrice", onePrice.getBigDecimal("price"));
                result.put("wholeSalePrice", onePrice.getBigDecimal("price"));
            }
        }

        DynamicViewEntity salesUsageViewEntity = new DynamicViewEntity();
        DynamicViewEntity productionUsageViewEntity = new DynamicViewEntity();
        if (! UtilValidate.isEmpty(checkTime)) {

            // Construct a dynamic view entity to search against for sales usage quantities
            salesUsageViewEntity.addMemberEntity("OI", "OrderItem");
            salesUsageViewEntity.addMemberEntity("OH", "OrderHeader");
            salesUsageViewEntity.addMemberEntity("ItIss", "ItemIssuance");
            salesUsageViewEntity.addMemberEntity("InvIt", "InventoryItem");
            salesUsageViewEntity.addViewLink("OI", "OH", Boolean.valueOf(false), ModelKeyMap.makeKeyMapList("orderId"));
            salesUsageViewEntity.addViewLink("OI", "ItIss", Boolean.valueOf(false), ModelKeyMap.makeKeyMapList("orderId", "orderId", "orderItemSeqId", "orderItemSeqId"));
            salesUsageViewEntity.addViewLink("ItIss", "InvIt", Boolean.valueOf(false), ModelKeyMap.makeKeyMapList("inventoryItemId"));
            salesUsageViewEntity.addAlias("OI", "productId");
            salesUsageViewEntity.addAlias("OH", "statusId");
            salesUsageViewEntity.addAlias("OH", "orderTypeId");
            salesUsageViewEntity.addAlias("OH", "orderDate");
            salesUsageViewEntity.addAlias("ItIss", "inventoryItemId");
            salesUsageViewEntity.addAlias("ItIss", "quantity");
            salesUsageViewEntity.addAlias("InvIt", "facilityId");

            // Construct a dynamic view entity to search against for production usage quantities
            productionUsageViewEntity.addMemberEntity("WEIA", "WorkEffortInventoryAssign");
            productionUsageViewEntity.addMemberEntity("WE", "WorkEffort");
            productionUsageViewEntity.addMemberEntity("II", "InventoryItem");
            productionUsageViewEntity.addViewLink("WEIA", "WE", Boolean.valueOf(false), ModelKeyMap.makeKeyMapList("workEffortId"));
            productionUsageViewEntity.addViewLink("WEIA", "II", Boolean.valueOf(false), ModelKeyMap.makeKeyMapList("inventoryItemId"));
            productionUsageViewEntity.addAlias("WEIA", "quantity");
            productionUsageViewEntity.addAlias("WE", "actualCompletionDate");
            productionUsageViewEntity.addAlias("WE", "workEffortTypeId");
            productionUsageViewEntity.addAlias("II", "facilityId");
            productionUsageViewEntity.addAlias("II", "productId");

        }
        if (! UtilValidate.isEmpty(checkTime)) {

            // Make a query against the sales usage view entity
            EntityListIterator salesUsageIt = null;
            try {
                salesUsageIt = delegator.findListIteratorByCondition(salesUsageViewEntity,
                        EntityCondition.makeCondition(
                            UtilMisc.toList(
                                EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId),
                                EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
                                EntityCondition.makeCondition("statusId", EntityOperator.IN, UtilMisc.toList("ORDER_COMPLETED", "ORDER_APPROVED", "ORDER_HELD")),
                                EntityCondition.makeCondition("orderTypeId", EntityOperator.EQUALS, "SALES_ORDER"),
                                EntityCondition.makeCondition("orderDate", EntityOperator.GREATER_THAN_EQUAL_TO, checkTime)
                            ),
                        EntityOperator.AND),
                    null, null, null, null);
            } catch (GenericEntityException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            // Sum the sales usage quantities found
            BigDecimal salesUsageQuantity = BigDecimal.ZERO;
            GenericValue salesUsageItem = null;
            while ((salesUsageItem = salesUsageIt.next()) != null) {
                if (salesUsageItem.get("quantity") != null) {
                    try {
                        salesUsageQuantity = salesUsageQuantity.add(salesUsageItem.getBigDecimal("quantity"));
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
            try {
                salesUsageIt.close();
            } catch (GenericEntityException e2) {
                // TODO Auto-generated catch block
                e2.printStackTrace();
            }

            // Make a query against the production usage view entity
            EntityListIterator productionUsageIt = null;
            try {
                productionUsageIt = delegator.findListIteratorByCondition(productionUsageViewEntity,
                        EntityCondition.makeCondition(
                            UtilMisc.toList(
                                EntityCondition.makeCondition("facilityId", EntityOperator.EQUALS, facilityId),
                                EntityCondition.makeCondition("productId", EntityOperator.EQUALS, productId),
                                EntityCondition.makeCondition("workEffortTypeId", EntityOperator.EQUALS, "PROD_ORDER_TASK"),
                                EntityCondition.makeCondition("actualCompletionDate", EntityOperator.GREATER_THAN_EQUAL_TO, checkTime)
                            ),
                        EntityOperator.AND),
                    null, null, null, null);
            } catch (GenericEntityException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            // Sum the production usage quantities found
            BigDecimal productionUsageQuantity = BigDecimal.ZERO;
            GenericValue productionUsageItem = null;
            while ((productionUsageItem = productionUsageIt.next()) != null) {
                if (productionUsageItem.get("quantity") != null) {
                    try {
                        productionUsageQuantity = productionUsageQuantity.add(productionUsageItem.getBigDecimal("quantity"));
                    } catch (Exception e) {
                        // Ignore
                    }
                }
            }
            try {
                productionUsageIt.close();
            } catch (GenericEntityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            result.put("usageQuantity", salesUsageQuantity.add(productionUsageQuantity));

        }
        return result;
    }

}
