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
package org.ofbiz.entity.util;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.ofbiz.base.util.Debug;
import org.ofbiz.entity.GenericDelegator;
import org.ofbiz.entity.GenericEntityException;
import org.ofbiz.entity.GenericPK;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.entity.model.ModelEntity;
import org.xml.sax.SAXException;

/**
 * Some utility routines for loading seed data.
 */
public class EntityDataAssert {

    public static final String module = EntityDataAssert.class.getName();

    public static int assertData(URL dataUrl, GenericDelegator delegator, List<Object> errorMessages) throws GenericEntityException, SAXException, ParserConfigurationException, IOException {
        int rowsChecked = 0;

        if (dataUrl == null) {
            String errMsg = "Cannot assert/check data, dataUrl was null";
            errorMessages.add(errMsg);
            Debug.logError(errMsg, module);
            return 0;
        }

        Debug.logVerbose("Loading XML Resource: " + dataUrl.toExternalForm(), module);

        try {
            for (GenericValue checkValue: delegator.readXmlDocument(dataUrl)) {
                checkSingleValue(checkValue, delegator, errorMessages);
                rowsChecked++;
            }
        } catch (GenericEntityException e) {
            String xmlError = "Error checking/asserting XML Resource: " + dataUrl.toExternalForm() + "; Error was: " + e.getMessage();
            Debug.logError(e, xmlError, module);
            // instead of adding this as a message, throw the real exception; then caller has more control
            //errorMessages.add(xmlError);
            throw e;
        }

        return rowsChecked;
    }

    public static void checkValueList(List valueList, GenericDelegator delegator, List<Object> errorMessages) throws GenericEntityException {
        if (valueList == null) return;

        Iterator valueIter = valueList.iterator();
        while (valueIter.hasNext()) {
            GenericValue checkValue = (GenericValue) valueIter.next();
            checkSingleValue(checkValue, delegator, errorMessages);
        }
    }

    public static void checkSingleValue(GenericValue checkValue, GenericDelegator delegator, List<Object> errorMessages) throws GenericEntityException {
        if (checkValue == null) {
            errorMessages.add("Got a value to check was null");
            return;
        }
        // to check get the PK, find by that, compare all fields
        GenericPK checkPK = null;

        try {
            checkPK = checkValue.getPrimaryKey();
            GenericValue currentValue = delegator.findOne(checkPK.getEntityName(), checkPK, false);
            if (currentValue == null) {
                errorMessages.add("Entity [" + checkPK.getEntityName() + "] record not found for pk: " + checkPK);
                return;
            }

            ModelEntity modelEntity = checkValue.getModelEntity();
            for (String nonpkFieldName: modelEntity.getNoPkFieldNames()) {
                // skip the fields the entity engine maintains
                if (ModelEntity.CREATE_STAMP_FIELD.equals(nonpkFieldName) || ModelEntity.CREATE_STAMP_TX_FIELD.equals(nonpkFieldName) ||
                        ModelEntity.STAMP_FIELD.equals(nonpkFieldName) || ModelEntity.STAMP_TX_FIELD.equals(nonpkFieldName)) {
                    continue;
                }

                Object checkField = checkValue.get(nonpkFieldName);
                Object currentField = currentValue.get(nonpkFieldName);

                if (checkField != null && !checkField.equals(currentField)) {
                    errorMessages.add("Field [" + modelEntity.getEntityName() + "." + nonpkFieldName +
                            "] did not match; file value [" + checkField + "], db value [" + currentField + "] pk [" + checkPK + "]");
                }
            }
        } catch (GenericEntityException e) {
            throw e;
        } catch (Throwable t) {
            String errMsg;
            if (checkPK == null) {
                errMsg = "Error checking value [" + checkValue + "]: " + t.toString();
            } else {
                errMsg = "Error checking entity [" + checkPK.getEntityName() + "] with pk [" + checkPK.getAllFields() + "]: " + t.toString();
            }
            errorMessages.add(errMsg);
            Debug.logError(t, errMsg, module);
        }
    }
}
