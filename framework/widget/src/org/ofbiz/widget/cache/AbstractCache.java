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
package org.ofbiz.widget.cache;

import org.ofbiz.base.util.cache.UtilCache;

public abstract class AbstractCache {

    protected String id;

    protected AbstractCache(String id) {
        this.id = id;
    }

    public void remove(String widgetName) {
        UtilCache.clearCache(getCacheName(widgetName));
    }

    public void clear() {
        UtilCache.clearCachesThatStartWith(getCacheNamePrefix());
    }

    public String getCacheNamePrefix() {
        return "widgetcache." + id + ".";
    }

    public String getCacheName(String widgetName) {
        return getCacheNamePrefix() + widgetName;
    }

    protected UtilCache getCache(String widgetName) {
        return UtilCache.findCache(getCacheName(widgetName));
    }

    protected UtilCache<WidgetContextCacheKey, GenericWidgetOutput> getOrCreateCache(String widgetName) {
        synchronized (UtilCache.utilCacheTable) {
            String name = getCacheName(widgetName);
            UtilCache<WidgetContextCacheKey, GenericWidgetOutput> cache = UtilCache.findCache(name);
            if (cache == null) {
                cache = new UtilCache<WidgetContextCacheKey, GenericWidgetOutput>(name, 0, 0, true);
                cache.setPropertiesParams(new String[] {name});
            }
            return cache;
        }
    }
}
