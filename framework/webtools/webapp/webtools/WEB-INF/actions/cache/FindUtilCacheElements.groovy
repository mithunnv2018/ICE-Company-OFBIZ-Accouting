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
import org.ofbiz.base.util.cache.UtilCache;
import org.ofbiz.base.util.cache.CacheLine;
import org.ofbiz.base.util.UtilFormatOut;
import org.ofbiz.security.Security;

context.hasUtilCacheEdit = security.hasEntityPermission("UTIL_CACHE", "_EDIT", session);

cacheName = parameters.UTIL_CACHE_NAME;
context.cacheName = cacheName;
context.now = (new Date()).toString();

totalSize = 0;

cacheElementsList = [];
if (cacheName) {
    utilCache = UtilCache.findCache(cacheName);
    if (utilCache) {
        int keyNum = 0;
        utilCache.cacheLineTable.keySet().each { key ->
            cacheElement = [:];
            line = utilCache.cacheLineTable.get(key);
            expireTime = "";
            if (line?.loadTime > 0) {
                expireTime = (new Date(line.loadTime + utilCache.getExpireTime())).toString();
            }
            lineSize = line.getSizeInBytes();
            totalSize += lineSize;

            cacheElement.elementKey = key;
            cacheElement.expireTime = expireTime;
            cacheElement.lineSize = UtilFormatOut.formatQuantity(lineSize);
            cacheElement.keyNum = keyNum;

            cacheElementsList.add(cacheElement);

            keyNum++;
        }
    }
}
context.totalSize = UtilFormatOut.formatQuantity(totalSize);
context.cacheElementsList = cacheElementsList;
