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
package org.ofbiz.webpos.session;

import java.util.Locale;
import java.util.Map;

import javolution.util.FastMap;

import org.ofbiz.base.util.Debug;
import org.ofbiz.base.util.GeneralException;
import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.base.util.UtilProperties;
import org.ofbiz.base.util.UtilValidate;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.order.shoppingcart.ShoppingCart;
import org.ofbiz.service.GenericServiceException;
import org.ofbiz.service.LocalDispatcher;
import org.ofbiz.service.ServiceUtil;
import org.ofbiz.webapp.control.LoginWorker;
import org.ofbiz.webpos.transaction.WebPosTransaction;

public class WebPosSession {

    public static final String module = WebPosSession.class.getName();

    private String id = null;
    private Map<String, Object> attributes = FastMap.newInstance();
    private GenericValue userLogin = null;
    private Locale locale = null;
    private String productStoreId = null;
    private String facilityId = null;
    private String currencyUomId = null;
    private transient GenericDelegator delegator = null;
    private String delegatorName = null;
    private LocalDispatcher dispatcher = null;
    private Boolean mgrLoggedIn = null;
    private WebPosTransaction webPosTransaction = null;
    private ShoppingCart cart = null;

    public WebPosSession(String id, Map<String, Object> attributes, GenericValue userLogin, Locale locale, String productStoreId, String facilityId, String currencyUomId, GenericDelegator delegator, LocalDispatcher dispatcher, ShoppingCart cart) {
        this.id = id;
        this.attributes = attributes;
        this.userLogin = userLogin;
        this.locale = locale;
        this.productStoreId = productStoreId;
        this.facilityId = facilityId;
        this.currencyUomId = currencyUomId;

        if (UtilValidate.isNotEmpty(delegator))
        {
            this.delegator = delegator;
            this.delegatorName = delegator.getDelegatorName();
        } else {
            this.delegator = this.getDelegator();
            this.delegatorName = delegator.getDelegatorName();
        }

        this.dispatcher = dispatcher;
        this.cart = cart;
        Debug.logInfo("Created WebPosSession [" + id + "]", module);
    }

    public GenericValue getUserLogin() {
        return this.userLogin;
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    public String getId() {
        return this.id;
    }

    public String getUserLoginId() {
        if (UtilValidate.isEmpty(getUserLogin())) {
            return null;
        } else {
            return this.getUserLogin().getString("userLoginId");
        }
    }

    public String getUserPartyId() {
        if (UtilValidate.isEmpty(getUserLogin())) {
            return null;
        } else {
            return this.getUserLogin().getString("partyId");
        }
    }

    public Locale getLocale() {
        return (Locale) this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getProductStoreId() {
        return this.productStoreId;
    }

    public void setProductStoreId(String productStoreId) {
        this.productStoreId = productStoreId;
    }

    public String getFacilityId() {
        return this.facilityId;
    }

    public void setFacilityId(String facilityId) {
        this.facilityId = facilityId;
    }

    public String getCurrencyUomId() {
        return this.currencyUomId;
    }

    public void setCurrencyUomId(String currencyUomId) {
        this.currencyUomId = currencyUomId;
    }

    public GenericDelegator getDelegator() {
        if (UtilValidate.isEmpty(delegator)) {
            delegator = GenericDelegator.getGenericDelegator(delegatorName);
        }
        return delegator;
    }

    public LocalDispatcher getDispatcher() {
        return dispatcher;
    }

    public ShoppingCart getCart() {
        return this.cart;
    }

    public void logout() {
        if (UtilValidate.isNotEmpty(webPosTransaction)) {
            webPosTransaction.closeTx();
            webPosTransaction = null;
        }

        if (UtilValidate.isNotEmpty(getUserLogin())) {
            LoginWorker.setLoggedOut(this.getUserLogin().getString("userLoginId"), this.getDelegator());
        }
    }

    public void login(String username, String password, LocalDispatcher dispatcher) throws UserLoginFailure {
        this.checkLogin(username, password, dispatcher);
    }

    public GenericValue checkLogin(String username, String password, LocalDispatcher dispatcher) throws UserLoginFailure {
        // check the required parameters and objects
        if (UtilValidate.isEmpty(dispatcher)) {
            throw new UserLoginFailure(UtilProperties.getMessage("WebPosUiLabels", "WebPosUnableToLogIn", getLocale()));
        }
        if (UtilValidate.isEmpty(username)) {
            throw new UserLoginFailure(UtilProperties.getMessage("PartyUiLabels", "PartyUserNameMissing", getLocale()));
        }
        if (UtilValidate.isEmpty(password)) {
            throw new UserLoginFailure(UtilProperties.getMessage("PartyUiLabels", "PartyPasswordMissing", getLocale()));
        }

        // call the login service
        Map<String, Object> result = null;
        try {
            result = dispatcher.runSync("userLogin", UtilMisc.toMap("login.username", username, "login.password", password));
        } catch (GenericServiceException e) {
            Debug.logError(e, module);
            throw new UserLoginFailure(e);
        } catch (Throwable t) {
            Debug.logError(t, "Throwable caught!", module);
        }

        // check for errors
        if (ServiceUtil.isError(result)) {
            throw new UserLoginFailure(ServiceUtil.getErrorMessage(result));
        } else {
            GenericValue ul = (GenericValue) result.get("userLogin");
            if (ul == null) {
                throw new UserLoginFailure(UtilProperties.getMessage("WebPosUiLabels", "WebPosUserLoginNotValid", getLocale()));
            }
            return ul;
        }
    }

    public boolean hasRole(GenericValue userLogin, String roleTypeId) {
        if (UtilValidate.isEmpty(userLogin) || UtilValidate.isEmpty(roleTypeId)) {
            return false;
        }
        String partyId = userLogin.getString("partyId");
        GenericValue partyRole = null;
        try {
            partyRole = getDelegator().findOne("PartyRole", false, "partyId", partyId, "roleTypeId", roleTypeId);
        } catch (GenericEntityException e) {
            Debug.logError(e, module);
            return false;
        }

        if (UtilValidate.isEmpty(partyRole)) {
            return false;
        }

        return true;
    }

    public boolean isManagerLoggedIn() {
        if (UtilValidate.isEmpty(mgrLoggedIn)) {
            mgrLoggedIn = new Boolean(hasRole(getUserLogin(), "MANAGER"));
        }
        return mgrLoggedIn.booleanValue();
    }

    public WebPosTransaction getCurrentTransaction() {
        if (UtilValidate.isEmpty(webPosTransaction)) {
            webPosTransaction = new WebPosTransaction(this);
        }
        return webPosTransaction;
    }

    public void setCurrentTransaction(WebPosTransaction webPosTransaction) {
        this.webPosTransaction = webPosTransaction;
    }

    public class UserLoginFailure extends GeneralException {
        public UserLoginFailure() {
            super();
        }

        public UserLoginFailure(String str) {
            super(str);
        }

        public UserLoginFailure(String str, Throwable nested) {
            super(str, nested);
        }

        public UserLoginFailure(Throwable nested) {
            super(nested);
        }
    }
}