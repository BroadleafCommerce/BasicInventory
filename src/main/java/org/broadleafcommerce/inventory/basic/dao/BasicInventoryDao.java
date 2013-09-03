/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.inventory.basic.dao;

import java.util.Map;
import java.util.Set;

/**
 * Reads inventory from the database.
 * 
 * @author Kelly Tisdell
 *
 */
public interface BasicInventoryDao {

    /**
     * Reads an inventory value by the id of the sku.
     * 
     * @param skuId
     * @return
     */
    public int readInventory(Long skuId);

    /**
     * Reads a number of inventory values by ids of skus.
     * 
     * @param skuIds
     * @return
     */
    public Map<Long, Integer> readInventory(Set<Long> skuIds);

}
