/*
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
 */

package org.ofbiz.product.test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javolution.util.FastList;
import javolution.util.FastMap;

import org.ofbiz.base.util.UtilMisc;
import org.ofbiz.entity.GenericValue;
import org.ofbiz.service.testtools.OFBizTestCase;

/**
 * Facility Tests
 */
public class StockMovesTest extends OFBizTestCase {

    protected GenericValue userLogin = null;

    public StockMovesTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        userLogin = delegator.findByPrimaryKey("UserLogin", UtilMisc.toMap("userLoginId", "system"));
    }

    protected void tearDown() throws Exception {
    }

    public void testStockMoves() throws Exception {
        Map<String, Object> fsmnCtx = FastMap.newInstance();
        Map stockMoveHandled = null;
        List warningList = FastList.newInstance();

        fsmnCtx.put("facilityId", "WebStoreWarehouse");
        fsmnCtx.put("userLogin", userLogin);
        Map<String, Object> respMap1 = dispatcher.runSync("findStockMovesNeeded", fsmnCtx);
        stockMoveHandled = (Map) respMap1.get("stockMoveHandled");
        warningList = (List) respMap1.get("warningMessageList");
        assertNull(warningList);

        if (stockMoveHandled != null) {
            fsmnCtx.put("stockMoveHandled", stockMoveHandled);
        }
        Map<String, Object> respMap2 = dispatcher.runSync("findStockMovesRecommended", fsmnCtx);
        warningList = (List) respMap2.get("warningMessageList");
        assertNull(warningList);

        Map<String, Object> ppsmCtx = FastMap.newInstance();
        ppsmCtx.put("productId", "GZ-2644");
        ppsmCtx.put("facilityId", "WebStoreWarehouse");
        ppsmCtx.put("locationSeqId","TLTLTLUL01" );
        ppsmCtx.put("targetLocationSeqId", "TLTLTLLL01");
        ppsmCtx.put("quantityMoved", new BigDecimal("5"));
        ppsmCtx.put("userLogin", userLogin);
        Map<String, Object> respMap3 = dispatcher.runSync("processPhysicalStockMove", ppsmCtx);
    }
}
