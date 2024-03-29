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
/*
 * The first purpose of this script is to retrieve the orderId and paymentPreferenceId of this gateway response. It is
 * activated if orderPaymentPreferenceId is not supplied.
 *
 * The second purpose of this script is to determine the gateway response of a given capture or authorize action.
 * To activate this behavior, pass in an orderPaymentPreferenceId. It will set the orderId in the context so that
 * a backlink to the order can be created.
 */

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.condition.EntityExpr;
import org.ofbiz.entity.condition.EntityOperator;
import org.ofbiz.entity.condition.EntityCondition;
import org.ofbiz.entity.util.EntityUtil;

// get this field first, it determines which purpose this script satisfies
orderPaymentPreferenceId = context.orderPaymentPreferenceId;

// first purpose: retrieve orderId and pamentPreferenceId
if (!orderPaymentPreferenceId) {
  paymentGatewayResponse = context.paymentGatewayResponse;
  orderPaymentPreference = paymentGatewayResponse.getRelatedOne("OrderPaymentPreference");
  context.orderId = orderPaymentPreference.orderId;
  context.orderPaymentPreferenceId = orderPaymentPreference.orderPaymentPreferenceId;
  return;
}

// second purpose: grab the latest gateway response of the orderaymentpreferenceId
orderPaymentPreference = delegator.findByPrimaryKey("OrderPaymentPreference", [orderPaymentPreferenceId : orderPaymentPreferenceId]);
gatewayResponses = orderPaymentPreference.getRelated("PaymentGatewayResponse", ["transactionDate DESC"]);
EntityUtil.filterByCondition(gatewayResponses, EntityCondition.makeCondition("transCodeEnumId", EntityOperator.EQUALS, "PGT_AUTHORIZE"));

if (gatewayResponses) {
    latestAuth = gatewayResponses[0];
    context.paymentGatewayResponse = latestAuth;
} else {
    // todo: some kind of error telling user to re-authorize
}

context.orderId = orderPaymentPreference.orderId;
