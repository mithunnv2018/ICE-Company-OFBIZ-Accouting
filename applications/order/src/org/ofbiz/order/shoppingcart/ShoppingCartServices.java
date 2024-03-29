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
package org.ofbiz.order.shoppingcart;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.lang.math.NumberUtils;
import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.util.EntityUtil;
import org.ofbiz.order.order.OrderReadHelper;
import org.ofbiz.product.config.ProductConfigWorker;
import org.ofbiz.product.config.ProductConfigWrapper;
import org.ofbiz.service.DispatchContext;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;

/**
 * Shopping Cart Services
 */
public class ShoppingCartServices {

    public static final String module = ShoppingCartServices.class.getName();
    public static final String resource = "OrderUiLabels";
    public static final String resource_error = "OrderErrorUiLabels";

    public static final MathContext generalRounding = new MathContext(10);
    public static Map<String, Object> assignItemShipGroup(DispatchContext dctx, Map<String, Object> context) {
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        Integer fromGroupIndex = (Integer) context.get("fromGroupIndex");
        Integer toGroupIndex = (Integer) context.get("toGroupIndex");
        Integer itemIndex = (Integer) context.get("itemIndex");
        BigDecimal quantity = (BigDecimal) context.get("quantity");
        Boolean clearEmptyGroups = (Boolean) context.get("clearEmptyGroups");

        if (clearEmptyGroups == null) {
            clearEmptyGroups = Boolean.TRUE;
        }

        Debug.log("From Group - " + fromGroupIndex + " To Group - " + toGroupIndex + "Item - " + itemIndex + "(" + quantity + ")", module);
        if (fromGroupIndex.equals(toGroupIndex)) {
            // nothing to do
            return ServiceUtil.returnSuccess();
        }

        cart.positionItemToGroup(itemIndex.intValue(), quantity,
                fromGroupIndex.intValue(), toGroupIndex.intValue(), clearEmptyGroups.booleanValue());
        Debug.log("Called cart.positionItemToGroup()", module);

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object>setShippingOptions(DispatchContext dctx, Map<String, Object> context) {
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        Integer groupIndex = (Integer) context.get("groupIndex");
        String shippingContactMechId = (String) context.get("shippingContactMechId");
        String shipmentMethodString = (String) context.get("shipmentMethodString");
        String shippingInstructions = (String) context.get("shippingInstructions");
        String giftMessage = (String) context.get("giftMessage");
        Boolean maySplit = (Boolean) context.get("maySplit");
        Boolean isGift = (Boolean) context.get("isGift");
        Locale locale = (Locale) context.get("locale");

        ShoppingCart.CartShipInfo csi = cart.getShipInfo(groupIndex.intValue());
        if (csi != null) {
            int idx = groupIndex.intValue();

            if (UtilValidate.isNotEmpty(shipmentMethodString)) {
                int delimiterPos = shipmentMethodString.indexOf('@');
                String shipmentMethodTypeId = null;
                String carrierPartyId = null;

                if (delimiterPos > 0) {
                    shipmentMethodTypeId = shipmentMethodString.substring(0, delimiterPos);
                    carrierPartyId = shipmentMethodString.substring(delimiterPos + 1);
                 }

                cart.setShipmentMethodTypeId(idx, shipmentMethodTypeId);
                cart.setCarrierPartyId(idx, carrierPartyId);
            }

            cart.setShippingInstructions(idx, shippingInstructions);
            cart.setShippingContactMechId(idx, shippingContactMechId);
            cart.setGiftMessage(idx, giftMessage);

            if (maySplit != null) {
                cart.setMaySplit(idx, maySplit);
            }
            if (isGift != null) {
                cart.setIsGift(idx, isGift);
            }
        } else {
            return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,"OrderCartShipGroupNotFound", UtilMisc.toMap("groupIndex",groupIndex), locale));
        }
        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object>setPaymentOptions(DispatchContext dctx, Map<String, Object> context) {
        Locale locale = (Locale) context.get("locale");

        return ServiceUtil.returnError(UtilProperties.getMessage(resource_error,"OrderServiceNotYetImplemented",locale));
    }

    public static Map<String, Object>setOtherOptions(DispatchContext dctx, Map<String, Object> context) {
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        String orderAdditionalEmails = (String) context.get("orderAdditionalEmails");
        String correspondingPoId = (String) context.get("correspondingPoId");

        cart.setOrderAdditionalEmails(orderAdditionalEmails);
        if (UtilValidate.isNotEmpty(correspondingPoId)) {
            cart.setPoNumber(correspondingPoId);
        } else {
            cart.setPoNumber(null);
        }

        return ServiceUtil.returnSuccess();
    }

    public static Map<String, Object>loadCartFromOrder(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericDelegator delegator = dctx.getDelegator();

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String orderId = (String) context.get("orderId");
        Boolean skipInventoryChecks = (Boolean) context.get("skipInventoryChecks");
        Boolean skipProductChecks = (Boolean) context.get("skipProductChecks");
        Locale locale = (Locale) context.get("locale");

        if (UtilValidate.isEmpty(skipInventoryChecks)) {
            skipInventoryChecks = Boolean.FALSE;
        }
        if (UtilValidate.isEmpty(skipProductChecks)) {
            skipProductChecks = Boolean.FALSE;
        }

        // get the order header
        GenericValue orderHeader = null;
        try {
            orderHeader = delegator.findByPrimaryKey("OrderHeader", UtilMisc.toMap("orderId", orderId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // initial require cart info
        OrderReadHelper orh = new OrderReadHelper(orderHeader);
        String productStoreId = orh.getProductStoreId();
        String orderTypeId = orh.getOrderTypeId();
        String currency = orh.getCurrency();
        String website = orh.getWebSiteId();
        String currentStatusString = orh.getCurrentStatusString();

        // create the cart
        ShoppingCart cart = new ShoppingCart(delegator, productStoreId, website, locale, currency);
        cart.setOrderType(orderTypeId);
        cart.setChannelType(orderHeader.getString("salesChannelEnumId"));
        cart.setInternalCode(orderHeader.getString("internalCode"));
        cart.setOrderDate(orderHeader.getTimestamp("orderDate"));
        cart.setOrderId(orderHeader.getString("orderId"));
        cart.setOrderName(orderHeader.getString("orderName"));
        cart.setOrderStatusId(orderHeader.getString("statusId"));
        cart.setOrderStatusString(currentStatusString);

        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (CartItemModifyException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // set the order name
        String orderName = orh.getOrderName();
        if (orderName != null) {
            cart.setOrderName(orderName);
        }

        // set the role information
        GenericValue placingParty = orh.getPlacingParty();
        if (placingParty != null) {
            cart.setPlacingCustomerPartyId(placingParty.getString("partyId"));
        }

        GenericValue billFromParty = orh.getBillFromParty();
        if (billFromParty != null) {
            cart.setBillFromVendorPartyId(billFromParty.getString("partyId"));
        }

        GenericValue billToParty = orh.getBillToParty();
        if (billToParty != null) {
            cart.setBillToCustomerPartyId(billToParty.getString("partyId"));
        }

        GenericValue shipToParty = orh.getShipToParty();
        if (shipToParty != null) {
            cart.setShipToCustomerPartyId(shipToParty.getString("partyId"));
        }

        GenericValue endUserParty = orh.getEndUserParty();
        if (endUserParty != null) {
            cart.setEndUserCustomerPartyId(endUserParty.getString("partyId"));
            cart.setOrderPartyId(endUserParty.getString("partyId"));
        }

        // load order attributes
        List<GenericValue> orderAttributesList = null;
        try {
            orderAttributesList = delegator.findByAnd("OrderAttribute", UtilMisc.toMap("orderId", orderId));
            if (UtilValidate.isNotEmpty(orderAttributesList)) {
                for (GenericValue orderAttr : orderAttributesList) {
                    String name = orderAttr.getString("attrName");
                    String value = orderAttr.getString("attrValue");
                    cart.setOrderAttribute(name, value);
                }
            }
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // load the payment infos
        List<GenericValue> orderPaymentPrefs = null;
        try {
            List<EntityExpr> exprs = UtilMisc.toList(EntityCondition.makeCondition("orderId", EntityOperator.EQUALS, orderId));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_RECEIVED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_CANCELLED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_DECLINED"));
            exprs.add(EntityCondition.makeCondition("statusId", EntityOperator.NOT_EQUAL, "PAYMENT_SETTLED"));
            EntityCondition cond = EntityCondition.makeCondition(exprs, EntityOperator.AND);
            orderPaymentPrefs = delegator.findList("OrderPaymentPreference", cond, null, null, null, false);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        if (UtilValidate.isNotEmpty(orderPaymentPrefs)) {
            Iterator<GenericValue> oppi = orderPaymentPrefs.iterator();
            while (oppi.hasNext()) {
                GenericValue opp = (GenericValue) oppi.next();
                String paymentId = opp.getString("paymentMethodId");
                if (paymentId == null) {
                    paymentId = opp.getString("paymentMethodTypeId");
                }
                BigDecimal maxAmount = opp.getBigDecimal("maxAmount");
                String overflow = opp.getString("overflowFlag");

                ShoppingCart.CartPaymentInfo cpi = null;

                if ((overflow == null || !"Y".equals(overflow)) && oppi.hasNext()) {
                    cpi = cart.addPaymentAmount(paymentId, maxAmount);
                    Debug.log("Added Payment: " + paymentId + " / " + maxAmount, module);
                } else {
                    cpi = cart.addPayment(paymentId);
                    Debug.log("Added Payment: " + paymentId + " / [no max]", module);
                }
                // for finance account the finAccountId needs to be set
                if ("FIN_ACCOUNT".equals(paymentId)) {
                    cpi.finAccountId = opp.getString("finAccountId");
                }
                // set the billing account and amount
                cart.setBillingAccount(orderHeader.getString("billingAccountId"), orh.getBillingAccountMaxAmount());
            }
        } else {
            Debug.log("No payment preferences found for order #" + orderId, module);
        }

        List<GenericValue> orderItems = orh.getValidOrderItems();
        long nextItemSeq = 0;
        if (UtilValidate.isNotEmpty(orderItems)) {
            for (GenericValue item : orderItems) {
                // get the next item sequence id
                String orderItemSeqId = item.getString("orderItemSeqId");
                orderItemSeqId = orderItemSeqId.replaceAll("\\P{Digit}", "");
                try {
                    long seq = Long.parseLong(orderItemSeqId);
                    if (seq > nextItemSeq) {
                        nextItemSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }

                // do not include PROMO items
                if (item.get("isPromo") != null && "Y".equals(item.getString("isPromo"))) {
                    continue;
                }

                // not a promo item; go ahead and add it in
                BigDecimal amount = item.getBigDecimal("selectedAmount");
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }
                BigDecimal quantity = item.getBigDecimal("quantity");
                if (quantity == null) {
                    quantity = BigDecimal.ZERO;
                }

                BigDecimal unitPrice = null;
                if ("Y".equals(item.getString("isModifiedPrice"))) {
                    unitPrice = item.getBigDecimal("unitPrice");
                }

                int itemIndex = -1;
                if (item.get("productId") == null) {
                    // non-product item
                    String itemType = item.getString("orderItemTypeId");
                    String desc = item.getString("itemDescription");
                    try {
                        // TODO: passing in null now for itemGroupNumber, but should reproduce from OrderItemGroup records
                        itemIndex = cart.addNonProductItem(itemType, desc, null, unitPrice, quantity, null, null, null, dispatcher);
                    } catch (CartItemModifyException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                } else {
                    // product item
                    String prodCatalogId = item.getString("prodCatalogId");
                    String productId = item.getString("productId");

                    //prepare the rental data
                    Timestamp reservStart = null;
                    BigDecimal reservLength = null;
                    BigDecimal reservPersons = null;
                    String accommodationMapId = null;
                    String accommodationSpotId = null;

                    GenericValue workEffort = null;
                    String workEffortId = orh.getCurrentOrderItemWorkEffort(item);
                    if (workEffortId != null) {
                        try {
                            workEffort = delegator.findByPrimaryKey("WorkEffort", UtilMisc.toMap("workEffortId", workEffortId));
                        } catch (GenericEntityException e) {
                            Debug.logError(e, module);
                        }
                    }
                    if (workEffort != null && "ASSET_USAGE".equals(workEffort.getString("workEffortTypeId"))) {
                        reservStart = workEffort.getTimestamp("estimatedStartDate");
                        reservLength = OrderReadHelper.getWorkEffortRentalLength(workEffort);
                        reservPersons = workEffort.getBigDecimal("reservPersons");
                        accommodationMapId = workEffort.getString("accommodationMapId");
                        accommodationSpotId = workEffort.getString("accommodationSpotId");

                    }    //end of rental data

                    //check for AGGREGATED products
                    ProductConfigWrapper configWrapper = null;
                    String configId = null;
                    try {
                        GenericValue product = delegator.findByPrimaryKey("Product", UtilMisc.toMap("productId", productId));
                        if ("AGGREGATED_CONF".equals(product.getString("productTypeId"))) {
                            List<GenericValue>productAssocs = delegator.findByAnd("ProductAssoc", UtilMisc.toMap("productAssocTypeId", "PRODUCT_CONF", "productIdTo", product.getString("productId")));
                            productAssocs = EntityUtil.filterByDate(productAssocs);
                            if (UtilValidate.isNotEmpty(productAssocs)) {
                                productId = EntityUtil.getFirst(productAssocs).getString("productId");
                                configId = product.getString("configId");
                            }
                        }
                    } catch (GenericEntityException e) {
                        Debug.logError(e, module);
                    }

                    if (UtilValidate.isNotEmpty(configId)) {
                        configWrapper = ProductConfigWorker.loadProductConfigWrapper(delegator, dispatcher, configId, productId, productStoreId, prodCatalogId, website, currency, locale, userLogin);
                    }
                    try {
                        itemIndex = cart.addItemToEnd(productId, amount, quantity, unitPrice, reservStart, reservLength, reservPersons,accommodationMapId,accommodationSpotId, null, null, prodCatalogId, configWrapper, item.getString("orderItemTypeId"), dispatcher, null, unitPrice == null ? null : false, skipInventoryChecks, skipProductChecks);
                    } catch (ItemNotFoundException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    } catch (CartItemModifyException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }

                // flag the item w/ the orderItemSeqId so we can reference it
                ShoppingCartItem cartItem = cart.findCartItem(itemIndex);
                cartItem.setOrderItemSeqId(item.getString("orderItemSeqId"));

                // attach addition item information
                cartItem.setStatusId(item.getString("statusId"));
                cartItem.setItemType(item.getString("orderItemTypeId"));
                cartItem.setItemComment(item.getString("comments"));
                cartItem.setQuoteId(item.getString("quoteId"));
                cartItem.setQuoteItemSeqId(item.getString("quoteItemSeqId"));
                cartItem.setProductCategoryId(item.getString("productCategoryId"));
                cartItem.setDesiredDeliveryDate(item.getTimestamp("estimatedDeliveryDate"));
                cartItem.setShipBeforeDate(item.getTimestamp("shipBeforeDate"));
                cartItem.setShipAfterDate(item.getTimestamp("shipAfterDate"));
                cartItem.setShoppingList(item.getString("shoppingListId"), item.getString("shoppingListItemSeqId"));
                cartItem.setIsModifiedPrice("Y".equals(item.getString("isModifiedPrice")));
                cartItem.setName(item.getString("itemDescription"));

                // load order item attributes
                List<GenericValue> orderItemAttributesList = null;
                try {
                    orderItemAttributesList = delegator.findByAnd("OrderItemAttribute", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId));
                    if (UtilValidate.isNotEmpty(orderAttributesList)) {
                        for(GenericValue orderItemAttr : orderItemAttributesList) {
                            String name = orderItemAttr.getString("attrName");
                            String value = orderItemAttr.getString("attrValue");
                            cartItem.setOrderItemAttribute(name, value);
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }

                // load order item contact mechs
                List<GenericValue> orderItemContactMechList = null;
                try {
                    orderItemContactMechList = delegator.findByAnd("OrderItemContactMech", UtilMisc.toMap("orderId", orderId, "orderItemSeqId", orderItemSeqId));
                    if (UtilValidate.isNotEmpty(orderItemContactMechList)) {
                        for (GenericValue orderItemContactMech : orderItemContactMechList) {
                            String contactMechPurposeTypeId = orderItemContactMech.getString("contactMechPurposeTypeId");
                            String contactMechId = orderItemContactMech.getString("contactMechId");
                            cartItem.addContactMech(contactMechPurposeTypeId, contactMechId);
                        }
                    }
                } catch (GenericEntityException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }

                // set the PO number on the cart
                cart.setPoNumber(item.getString("correspondingPoId"));

                List<GenericValue> itemAdjustments = orh.getOrderItemAdjustments(item);
                if (itemAdjustments != null) {
                    for(GenericValue itemAdjustment : itemAdjustments) {
                        cartItem.addAdjustment(itemAdjustment);
                    }
                }
            }

            if (UtilValidate.isNotEmpty(orderItems)) {
                int itemIndex = 0;
                for (GenericValue item : orderItems) {

                    // set the item's ship group info
                    List<GenericValue> shipGroups = orh.getOrderItemShipGroupAssocs(item);
                    for (int g = 0; g < shipGroups.size(); g++) {
                        GenericValue sgAssoc = (GenericValue) shipGroups.get(g);
                        BigDecimal shipGroupQty = OrderReadHelper.getOrderItemShipGroupQuantity(sgAssoc);
                        if (shipGroupQty == null) {
                            shipGroupQty = BigDecimal.ZERO;
                        }

                        GenericValue sg = null;
                        try {
                            sg = sgAssoc.getRelatedOne("OrderItemShipGroup");
                        } catch (GenericEntityException e) {
                            Debug.logError(e, module);
                            return ServiceUtil.returnError(e.getMessage());
                        }
                        String cartShipGroupIndexStr = sg.getString("shipGroupSeqId");
                        int cartShipGroupIndex = NumberUtils.toInt(cartShipGroupIndexStr);

                        if (cart.getShipGroupSize() < cartShipGroupIndex) {
                            int groupDiff = cartShipGroupIndex - cart.getShipGroupSize();
                            for (int i = 0; i < groupDiff; i++) {
                                cart.addShipInfo();
                            }
                        }

                        cartShipGroupIndex = cartShipGroupIndex - 1;
                        if (cartShipGroupIndex > 0) {
                            cart.positionItemToGroup(itemIndex, shipGroupQty, 0, cartShipGroupIndex, false);
                        }

                        cart.setShipAfterDate(cartShipGroupIndex, sg.getTimestamp("shipAfterDate"));
                        cart.setShipBeforeDate(cartShipGroupIndex, sg.getTimestamp("shipByDate"));
                        cart.setShipmentMethodTypeId(cartShipGroupIndex, sg.getString("shipmentMethodTypeId"));
                        cart.setCarrierPartyId(cartShipGroupIndex, sg.getString("carrierPartyId"));
                        cart.setSupplierPartyId(cartShipGroupIndex, sg.getString("supplierPartyId"));
                        cart.setMaySplit(cartShipGroupIndex, sg.getBoolean("maySplit"));
                        cart.setGiftMessage(cartShipGroupIndex, sg.getString("giftMessage"));
                        cart.setShippingContactMechId(cartShipGroupIndex, sg.getString("contactMechId"));
                        cart.setShippingInstructions(cartShipGroupIndex, sg.getString("shippingInstructions"));
                        cart.setShipGroupFacilityId(cartShipGroupIndex, sg.getString("facilityId"));
                        cart.setShipGroupVendorPartyId(cartShipGroupIndex, sg.getString("vendorPartyId"));
                        cart.setShipGroupSeqId(cartShipGroupIndex, sg.getString("shipGroupSeqId"));
                        cart.setItemShipGroupQty(itemIndex, shipGroupQty, cartShipGroupIndex);
                    }
                    itemIndex ++;
                }
            }

            // set the item seq in the cart
            if (nextItemSeq > 0) {
                try {
                    cart.setNextItemSeq(nextItemSeq);
                } catch (GeneralException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
            }
        }

        List adjustments = orh.getOrderHeaderAdjustments();
        // If applyQuoteAdjustments is set to false then standard cart adjustments are used.
        if (!adjustments.isEmpty()) {
            // The cart adjustments are added to the cart
            cart.getAdjustments().addAll(adjustments);
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        return result;
    }

    public static Map<String, Object> loadCartFromQuote(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericDelegator delegator = dctx.getDelegator();

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String quoteId = (String) context.get("quoteId");
        String applyQuoteAdjustmentsString = (String) context.get("applyQuoteAdjustments");
        Locale locale = (Locale) context.get("locale");

        boolean applyQuoteAdjustments = applyQuoteAdjustmentsString == null || "true".equals(applyQuoteAdjustmentsString);

        // get the quote header
        GenericValue quote = null;
        try {
            quote = delegator.findByPrimaryKey("Quote", UtilMisc.toMap("quoteId", quoteId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // initial require cart info
        String productStoreId = quote.getString("productStoreId");
        String currency = quote.getString("currencyUomId");

        // create the cart
        ShoppingCart cart = new ShoppingCart(delegator, productStoreId, locale, currency);
        // set shopping cart type
        if ("PURCHASE_QUOTE".equals(quote.getString("quoteTypeId"))) {
            cart.setOrderType("PURCHASE_ORDER");
            cart.setBillFromVendorPartyId(quote.getString("partyId"));
        }
        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (CartItemModifyException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        cart.setQuoteId(quoteId);
        cart.setOrderName(quote.getString("quoteName"));
        cart.setChannelType(quote.getString("salesChannelEnumId"));

        List<GenericValue>quoteItems = null;
        List<GenericValue>quoteAdjs = null;
        List<GenericValue>quoteRoles = null;
        List<GenericValue>quoteAttributes = null;
        try {
            quoteItems = quote.getRelated("QuoteItem", UtilMisc.toList("quoteItemSeqId"));
            quoteAdjs = quote.getRelated("QuoteAdjustment");
            quoteRoles = quote.getRelated("QuoteRole");
            quoteAttributes = quote.getRelated("QuoteAttribute");
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }
        // set the role information
        cart.setOrderPartyId(quote.getString("partyId"));
        if (UtilValidate.isNotEmpty(quoteRoles)) {
            for(GenericValue quoteRole : quoteRoles) {
                String quoteRoleTypeId = quoteRole.getString("roleTypeId");
                String quoteRolePartyId = quoteRole.getString("partyId");
                if ("PLACING_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setPlacingCustomerPartyId(quoteRolePartyId);
                } else if ("BILL_TO_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setBillToCustomerPartyId(quoteRolePartyId);
                } else if ("SHIP_TO_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setShipToCustomerPartyId(quoteRolePartyId);
                } else if ("END_USER_CUSTOMER".equals(quoteRoleTypeId)) {
                    cart.setEndUserCustomerPartyId(quoteRolePartyId);
                } else if ("BILL_FROM_VENDOR".equals(quoteRoleTypeId)) {
                    cart.setBillFromVendorPartyId(quoteRolePartyId);
                } else {
                    cart.addAdditionalPartyRole(quoteRolePartyId, quoteRoleTypeId);
                }
            }
        }

        // set the attribute information
        if (UtilValidate.isNotEmpty(quoteAttributes)) {
            for(GenericValue quoteAttribute : quoteAttributes) {
                cart.setOrderAttribute(quoteAttribute.getString("attrName"), quoteAttribute.getString("attrValue"));
            }
        }

        // Convert the quote adjustment to order header adjustments and
        // put them in a map: the key/values pairs are quoteItemSeqId/List of adjs
        Map<String, List<GenericValue>> orderAdjsMap = FastMap.newInstance() ;
        for (GenericValue quoteAdj : quoteAdjs) {
            List<GenericValue> orderAdjs = (List<GenericValue>)orderAdjsMap.get(UtilValidate.isNotEmpty(quoteAdj.getString("quoteItemSeqId")) ? quoteAdj.getString("quoteItemSeqId") : quoteId);
            if (orderAdjs == null) {
                orderAdjs = FastList.newInstance();
                orderAdjsMap.put(UtilValidate.isNotEmpty(quoteAdj.getString("quoteItemSeqId")) ? quoteAdj.getString("quoteItemSeqId") : quoteId, orderAdjs);
            }
            // convert quote adjustments to order adjustments
            GenericValue orderAdj = delegator.makeValue("OrderAdjustment");
            orderAdj.put("orderAdjustmentId", quoteAdj.get("quoteAdjustmentId"));
            orderAdj.put("orderAdjustmentTypeId", quoteAdj.get("quoteAdjustmentTypeId"));
            orderAdj.put("orderItemSeqId", quoteAdj.get("quoteItemSeqId"));
            orderAdj.put("comments", quoteAdj.get("comments"));
            orderAdj.put("description", quoteAdj.get("description"));
            orderAdj.put("amount", quoteAdj.get("amount"));
            orderAdj.put("productPromoId", quoteAdj.get("productPromoId"));
            orderAdj.put("productPromoRuleId", quoteAdj.get("productPromoRuleId"));
            orderAdj.put("productPromoActionSeqId", quoteAdj.get("productPromoActionSeqId"));
            orderAdj.put("productFeatureId", quoteAdj.get("productFeatureId"));
            orderAdj.put("correspondingProductId", quoteAdj.get("correspondingProductId"));
            orderAdj.put("sourceReferenceId", quoteAdj.get("sourceReferenceId"));
            orderAdj.put("sourcePercentage", quoteAdj.get("sourcePercentage"));
            orderAdj.put("customerReferenceId", quoteAdj.get("customerReferenceId"));
            orderAdj.put("primaryGeoId", quoteAdj.get("primaryGeoId"));
            orderAdj.put("secondaryGeoId", quoteAdj.get("secondaryGeoId"));
            orderAdj.put("exemptAmount", quoteAdj.get("exemptAmount"));
            orderAdj.put("taxAuthGeoId", quoteAdj.get("taxAuthGeoId"));
            orderAdj.put("taxAuthPartyId", quoteAdj.get("taxAuthPartyId"));
            orderAdj.put("overrideGlAccountId", quoteAdj.get("overrideGlAccountId"));
            orderAdj.put("includeInTax", quoteAdj.get("includeInTax"));
            orderAdj.put("includeInShipping", quoteAdj.get("includeInShipping"));
            orderAdj.put("createdDate", quoteAdj.get("createdDate"));
            orderAdj.put("createdByUserLogin", quoteAdj.get("createdByUserLogin"));
            orderAdjs.add(orderAdj);
        }

        long nextItemSeq = 0;
        if (UtilValidate.isNotEmpty(quoteItems)) {
            for(GenericValue quoteItem : quoteItems) {
                // get the next item sequence id
                String orderItemSeqId = quoteItem.getString("quoteItemSeqId");
                orderItemSeqId = orderItemSeqId.replaceAll("\\P{Digit}", "");
                try {
                    long seq = Long.parseLong(orderItemSeqId);
                    if (seq > nextItemSeq) {
                        nextItemSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }

                boolean isPromo = quoteItem.get("isPromo") != null && "Y".equals(quoteItem.getString("isPromo"));
                if (isPromo && !applyQuoteAdjustments) {
                    // do not include PROMO items
                    continue;
                }

                // not a promo item; go ahead and add it in
                BigDecimal amount = quoteItem.getBigDecimal("selectedAmount");
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }
                BigDecimal quantity = quoteItem.getBigDecimal("quantity");
                if (quantity == null) {
                    quantity = BigDecimal.ZERO;
                }
                BigDecimal quoteUnitPrice = quoteItem.getBigDecimal("quoteUnitPrice");
                if (quoteUnitPrice == null) {
                    quoteUnitPrice = BigDecimal.ZERO;
                }
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    // If, in the quote, an amount is set, we need to
                    // pass to the cart the quoteUnitPrice/amount value.
                    quoteUnitPrice = quoteUnitPrice.divide(amount, generalRounding);
                }

                //rental product data
                Timestamp reservStart = quoteItem.getTimestamp("reservStart");
                BigDecimal reservLength = quoteItem.getBigDecimal("reservLength");
                BigDecimal reservPersons = quoteItem.getBigDecimal("reservPersons");
                //String accommodationMapId = quoteItem.getString("accommodationMapId");
                //String accommodationSpotId = quoteItem.getString("accommodationSpotId");

                int itemIndex = -1;
                if (quoteItem.get("productId") == null) {
                    // non-product item
                    String desc = quoteItem.getString("comments");
                    try {
                        // note that passing in null for itemGroupNumber as there is no real grouping concept in the quotes right now
                        itemIndex = cart.addNonProductItem(null, desc, null, null, quantity, null, null, null, dispatcher);
                    } catch (CartItemModifyException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                } else {
                    // product item
                    String productId = quoteItem.getString("productId");
                    ProductConfigWrapper configWrapper = null;
                    if (UtilValidate.isNotEmpty(quoteItem.getString("configId"))) {
                        configWrapper = ProductConfigWorker.loadProductConfigWrapper(delegator, dispatcher, quoteItem.getString("configId"), productId, productStoreId, null, null, currency, locale, userLogin);
                    }
                    try {
                            itemIndex = cart.addItemToEnd(productId, amount, quantity, quoteUnitPrice, reservStart, reservLength, reservPersons,null,null, null, null, null, configWrapper, null, dispatcher, new Boolean(!applyQuoteAdjustments), new Boolean(quoteUnitPrice.compareTo(BigDecimal.ZERO) == 0), Boolean.FALSE, Boolean.FALSE);

                    } catch (ItemNotFoundException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    } catch (CartItemModifyException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }

                // flag the item w/ the orderItemSeqId so we can reference it
                ShoppingCartItem cartItem = cart.findCartItem(itemIndex);
                cartItem.setOrderItemSeqId(orderItemSeqId);
                // attach additional item information
                cartItem.setItemComment(quoteItem.getString("comments"));
                cartItem.setQuoteId(quoteItem.getString("quoteId"));
                cartItem.setQuoteItemSeqId(quoteItem.getString("quoteItemSeqId"));
                cartItem.setIsPromo(isPromo);
                //cartItem.setDesiredDeliveryDate(quoteItem.getTimestamp("estimatedDeliveryDate"));
                //cartItem.setStatusId(quoteItem.getString("statusId"));
                //cartItem.setItemType(quoteItem.getString("orderItemTypeId"));
                //cartItem.setProductCategoryId(quoteItem.getString("productCategoryId"));
                //cartItem.setShoppingList(quoteItem.getString("shoppingListId"), quoteItem.getString("shoppingListItemSeqId"));
            }

        }

        // If applyQuoteAdjustments is set to false then standard cart adjustments are used.
        if (applyQuoteAdjustments) {
            // The cart adjustments, derived from quote adjustments, are added to the cart
            List<GenericValue> adjs = (List<GenericValue>)orderAdjsMap.get(quoteId);
            if (adjs != null) {
                cart.getAdjustments().addAll(adjs);
            }
            // The cart item adjustments, derived from quote item adjustments, are added to the cart
            if (quoteItems != null) {
                Iterator i = cart.iterator();
                while (i.hasNext()) {
                    ShoppingCartItem item = (ShoppingCartItem) i.next();
                    adjs = (List)orderAdjsMap.get(item.getOrderItemSeqId());
                    if (adjs != null) {
                        item.getAdjustments().addAll(adjs);
                    }
                }
            }
        }
        // set the item seq in the cart
        if (nextItemSeq > 0) {
            try {
                cart.setNextItemSeq(nextItemSeq);
            } catch (GeneralException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        return result;
    }

    public static Map<String, Object>loadCartFromShoppingList(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericDelegator delegator = dctx.getDelegator();

        GenericValue userLogin = (GenericValue) context.get("userLogin");
        String shoppingListId = (String) context.get("shoppingListId");
        Locale locale = (Locale) context.get("locale");

        // get the shopping list header
        GenericValue shoppingList = null;
        try {
            shoppingList = delegator.findByPrimaryKey("ShoppingList", UtilMisc.toMap("shoppingListId", shoppingListId));
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // initial required cart info
        String productStoreId = shoppingList.getString("productStoreId");
        String currency = shoppingList.getString("currencyUom");
        // If no currency has been set in the ShoppingList, use the ProductStore default currency
        if (currency == null) {
            try {
                GenericValue productStore = shoppingList.getRelatedOne("ProductStore");
                if (productStore != null) {
                    currency = productStore.getString("defaultCurrencyUomId");
                }
            } catch (GenericEntityException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        }
        // If we still have no currency, use the default from general.properties.  Failing that, use USD
        if (currency == null) {
                currency = UtilProperties.getPropertyValue("general", "currency.uom.id.default", "USD");
        }

        // create the cart
        ShoppingCart cart = new ShoppingCart(delegator, productStoreId, locale, currency);

        try {
            cart.setUserLogin(userLogin, dispatcher);
        } catch (CartItemModifyException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        // set the role information
        cart.setOrderPartyId(shoppingList.getString("partyId"));

        List<GenericValue>shoppingListItems = null;
        try {
            shoppingListItems = shoppingList.getRelated("ShoppingListItem");
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return ServiceUtil.returnError(e.getMessage());
        }

        long nextItemSeq = 0;
        if (UtilValidate.isNotEmpty(shoppingListItems)) {
            for(GenericValue shoppingListItem : shoppingListItems) {
                // get the next item sequence id
                String orderItemSeqId = shoppingListItem.getString("shoppingListItemSeqId");
                orderItemSeqId = orderItemSeqId.replaceAll("\\P{Digit}", "");
                try {
                    long seq = Long.parseLong(orderItemSeqId);
                    if (seq > nextItemSeq) {
                        nextItemSeq = seq;
                    }
                } catch (NumberFormatException e) {
                    Debug.logError(e, module);
                    return ServiceUtil.returnError(e.getMessage());
                }
                /*
                BigDecimal amount = shoppingListItem.getBigDecimal("selectedAmount");
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }
                 */
                BigDecimal quantity = shoppingListItem.getBigDecimal("quantity");
                if (quantity == null) {
                    quantity = BigDecimal.ZERO;
                }
                int itemIndex = -1;
                if (shoppingListItem.get("productId") != null) {
                    // product item
                    String productId = shoppingListItem.getString("productId");
                    ProductConfigWrapper configWrapper = null;
                    if (UtilValidate.isNotEmpty(shoppingListItem.getString("configId"))) {
                        configWrapper = ProductConfigWorker.loadProductConfigWrapper(delegator, dispatcher, shoppingListItem.getString("configId"), productId, productStoreId, null, null, currency, locale, userLogin);
                    }
                    try {
                        itemIndex = cart.addItemToEnd(productId, null, quantity, null, null, null, null, null, configWrapper, dispatcher, Boolean.TRUE, Boolean.TRUE);
                    } catch (ItemNotFoundException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    } catch (CartItemModifyException e) {
                        Debug.logError(e, module);
                        return ServiceUtil.returnError(e.getMessage());
                    }
                }

                // flag the item w/ the orderItemSeqId so we can reference it
                ShoppingCartItem cartItem = cart.findCartItem(itemIndex);
                cartItem.setOrderItemSeqId(orderItemSeqId);
                // attach additional item information
                cartItem.setShoppingList(shoppingListItem.getString("shoppingListId"), shoppingListItem.getString("shoppingListItemSeqId"));
            }

        }

        // set the item seq in the cart
        if (nextItemSeq > 0) {
            try {
                cart.setNextItemSeq(nextItemSeq);
            } catch (GeneralException e) {
                Debug.logError(e, module);
                return ServiceUtil.returnError(e.getMessage());
            }
        }

        Map<String, Object> result = ServiceUtil.returnSuccess();
        result.put("shoppingCart", cart);
        return result;
    }

    public static Map<String, Object>getShoppingCartData(DispatchContext dctx, Map<String, Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        Locale locale = (Locale) context.get("locale");
        ShoppingCart shoppingCart = (ShoppingCart) context.get("shoppingCart");
        if (shoppingCart != null) {
            String isoCode = shoppingCart.getCurrency();
            result.put("totalQuantity", shoppingCart.getTotalQuantity());
            result.put("currencyIsoCode",isoCode);
            result.put("subTotal", shoppingCart.getSubTotal());
            result.put("subTotalCurrencyFormatted",org.ofbiz.base.util.UtilFormatOut.formatCurrency(shoppingCart.getSubTotal(), isoCode, locale));
            result.put("totalShipping", shoppingCart.getTotalShipping());
            result.put("totalShippingCurrencyFormatted",org.ofbiz.base.util.UtilFormatOut.formatCurrency(shoppingCart.getTotalShipping(), isoCode, locale));
            result.put("totalSalesTax",shoppingCart.getTotalSalesTax());
            result.put("totalSalesTaxCurrencyFormatted",org.ofbiz.base.util.UtilFormatOut.formatCurrency(shoppingCart.getTotalSalesTax(), isoCode, locale));
            result.put("displayGrandTotal", shoppingCart.getDisplayGrandTotal());
            result.put("displayGrandTotalCurrencyFormatted",org.ofbiz.base.util.UtilFormatOut.formatCurrency(shoppingCart.getDisplayGrandTotal(), isoCode, locale));
            BigDecimal orderAdjustmentsTotal = OrderReadHelper.calcOrderAdjustments(OrderReadHelper.getOrderHeaderAdjustments(shoppingCart.getAdjustments(), null), shoppingCart.getSubTotal(), true, true, true);
            result.put("displayOrderAdjustmentsTotalCurrencyFormatted", org.ofbiz.base.util.UtilFormatOut.formatCurrency(orderAdjustmentsTotal, isoCode, locale));
            Iterator i = shoppingCart.iterator();
            Map<String, Object> cartItemData = FastMap.newInstance();
            while (i.hasNext()) {
                ShoppingCartItem cartLine = (ShoppingCartItem) i.next();
                int cartLineIndex = shoppingCart.getItemIndex(cartLine);
                cartItemData.put("displayItemQty_" + cartLineIndex, cartLine.getQuantity());
                cartItemData.put("displayItemPrice_" + cartLineIndex, org.ofbiz.base.util.UtilFormatOut.formatCurrency(cartLine.getDisplayPrice(), isoCode, locale));
                cartItemData.put("displayItemSubTotal_" + cartLineIndex, cartLine.getDisplayItemSubTotal());
                cartItemData.put("displayItemSubTotalCurrencyFormatted_" + cartLineIndex ,org.ofbiz.base.util.UtilFormatOut.formatCurrency(cartLine.getDisplayItemSubTotal(), isoCode, locale));
                cartItemData.put("displayItemAdjustment_" + cartLineIndex ,org.ofbiz.base.util.UtilFormatOut.formatCurrency(cartLine.getOtherAdjustments(), isoCode, locale));
            }
            result.put("cartItemData",cartItemData);
        }
        return result;
    }

    public static Map<String, Object>getShoppingCartItemIndex(DispatchContext dctx, Map<String, Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        ShoppingCart shoppingCart = (ShoppingCart) context.get("shoppingCart");
        String productId = (String) context.get("productId");
        if (shoppingCart != null && UtilValidate.isNotEmpty(shoppingCart.items())) {
            List allItems = shoppingCart.items();
            List items = shoppingCart.findAllCartItems(productId);
            if (items.size() > 0) {
                ShoppingCartItem item = (ShoppingCartItem)items.get(0);
                int itemIndex = shoppingCart.getItemIndex(item);
                result.put("itemIndex", String.valueOf(itemIndex));
            }
        }
        return result;
    }

    public static Map<String, Object>resetShipGroupItems(DispatchContext dctx, Map<String, Object> context) {
        Map<String, Object> result = ServiceUtil.returnSuccess();
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        Iterator sciIter = cart.iterator();
        while (sciIter.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) sciIter.next();
            cart.clearItemShipInfo(item);
            cart.setItemShipGroupQty(item, item.getQuantity(), 0);
        }
        return result;
    }

    public static Map<String, Object>prepareVendorShipGroups(DispatchContext dctx, Map<String, Object> context) {
        LocalDispatcher dispatcher = dctx.getDispatcher();
        GenericDelegator delegator = dctx.getDelegator();
        ShoppingCart cart = (ShoppingCart) context.get("shoppingCart");
        Map<String, Object> result = ServiceUtil.returnSuccess();
        try {
            Map<String, Object> resp = dispatcher.runSync("resetShipGroupItems", context);
            if (ServiceUtil.isError(resp)) {
                return ServiceUtil.returnError(ServiceUtil.getErrorMessage(resp));
            }
        } catch (GenericServiceException e) {
            Debug.logError(e.toString(), module);
            return ServiceUtil.returnError(e.toString());
        }
        Map<String, Object> vendorMap = FastMap.newInstance();
        Iterator sciIter = cart.iterator();
        while (sciIter.hasNext()) {
            ShoppingCartItem item = (ShoppingCartItem) sciIter.next();
            GenericValue vendorProduct = null;
            String productId = item.getParentProductId();
            if (productId == null) {
                productId = item.getProductId();
            }
            int index = 0;
            try {
                vendorProduct = EntityUtil.getFirst(delegator.findByAnd("VendorProduct", UtilMisc.toMap("productId", productId, "productStoreGroupId", "_NA_")));
            } catch (GenericEntityException e) {
                Debug.logError(e.toString(), module);
            }

            if (UtilValidate.isEmpty(vendorProduct)) {
                if (vendorMap.containsKey("_NA_")) {
                    index = ((Integer) vendorMap.get("_NA_")).intValue();
                    cart.positionItemToGroup(item, item.getQuantity(), 0, index, true);
                } else {
                    index = cart.addShipInfo();
                    vendorMap.put("_NA_", index);

                    ShoppingCart.CartShipInfo info = cart.getShipInfo(index);
                    info.setVendorPartyId("_NA_");
                    info.setShipGroupSeqId(UtilFormatOut.formatPaddedNumber(index, 5));
                    cart.positionItemToGroup(item, item.getQuantity(), 0, index, true);
                }
            }
            if (UtilValidate.isNotEmpty(vendorProduct)) {
                String vendorPartyId = vendorProduct.getString("vendorPartyId");
                if (vendorMap.containsKey(vendorPartyId)) {
                    index = ((Integer) vendorMap.get(vendorPartyId)).intValue();
                    cart.positionItemToGroup(item, item.getQuantity(), 0, index, true);
                } else {
                    index = cart.addShipInfo();
                    vendorMap.put(vendorPartyId, index);

                    ShoppingCart.CartShipInfo info = cart.getShipInfo(index);
                    info.setVendorPartyId(vendorPartyId);
                    info.setShipGroupSeqId(UtilFormatOut.formatPaddedNumber(index, 5));
                    cart.positionItemToGroup(item, item.getQuantity(), 0, index, true);
                }
            }
        }
        return result;
    }
}
