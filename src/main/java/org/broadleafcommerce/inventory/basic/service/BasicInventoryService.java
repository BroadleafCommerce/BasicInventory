/*
 * #%L
 * Basic Inventory Module
 * %%
 * Copyright (C) 2009 - 2014 Broadleaf Commerce
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.broadleafcommerce.inventory.basic.service;

import java.util.Map;
import java.util.Set;

/**
 * This basic inventory service checks and adjusts the current inventory of a sku. All Skus will be considered 
 * generally unavailable from an inventory perspective if Sku.isAvaliable() returns false or if Sku.isActive() returns 
 * false.
 * 
 * Skus with an InventoryType of null or 'NONE' will be considered undefined from an inventory perspective, and will generally 
 * be considered available.  However, a request for available quantities of Skus with a null or 'NONE' inventory type will 
 * return null (as the sku is available but no inventory strategy is defined).
 * 
 * @author Kelly Tisdell
 *
 */
public interface BasicInventoryService {

    /**
     * Retrieves the quantity available for a particular sku.  May return null if no inventory is maintained 
     * for the given sku. This is the case when Sku.getInventoryType() == null 
     * or not Sku.getInventoryType().equals(InventoryType.BASIC). Effectively, if the quantity returned is null, inventory is 
     * undefined, which most likely means it is available.  However, rather than returning an arbitrary integer values (like Integer.MAX_VALUE), 
     * which has specific meaning, we return null as this can be interpreted by the client to mean whatever they define it as (including 
     * infinitely available), which is the most likely scenario.
     * 
     * @param skuId
     * @return
     */
    public Integer retrieveQuantityAvailable(Long skuId);

    /**
     * Retrieves the quantities available for a set of sku ids.  The values in the map returned may be null if no inventory is maintained 
     * for the given sku. This is the case when Sku.getInventoryType() == null 
     * or not Sku.getInventoryType().equals(InventoryType.BASIC).  Effectively, if a quantity returned is null, inventory is 
     * undefined, which most likely means it is available.  However, rather than returning an arbitrary integer values (like Integer.MAX_VALUE), 
     * which has specific meaning, we return null as this can be interpreted by the client to mean whatever they define it as (including 
     * infinitely available), which is the most likely scenario.
     * 
     * @param skuIds
     * @return
     */
    public Map<Long, Integer> retrieveQuantitiesAvailable(Set<Long> skuIds);

    /**
     * Indicates whether the given quantity is available for the particular skuId. The result will be 
     * true if Sku.getInventoryType() == null or not Sku.getInventoryType().equals(InventoryType.BASIC).
     * 
     * The result will be false if the Sku is inactive, if the Sku.getAvaialbe() == false, if the quantity 
     * field is null, or if the quantity requested exceeds the quantity available.
     * 
     * @param skuId
     * @param quantity
     * @return
     */
    public boolean isAvailable(Long skuId, int quantity);

    /**
     * Attempts to decrement inventory if it is available.
     * 
     * @param skuId
     * @param quantity
     * @throws BasicInventoryUnavailableException
     */
    public void decrementInventory(Long skuId, int quantity) throws BasicInventoryUnavailableException;

    /**
     * Attempts to decrement inventory for a map of Sku ids and quantities.
     * 
     * Quantities must be greater than zero or an IllegalArgumentException will be thrown.
     * 
     * @param skuQuantities
     * @throws BasicInventoryUnavailableException
     */
    public void decrementInventory(Map<Long, Integer> skuQuantities) throws BasicInventoryUnavailableException;

    /**
     * Attempts to increment inventory.
     * 
     * Quantity must be greater than zero or an IllegalArgumentException will be thrown.
     * 
     * @param skuId
     * @param quantity
     */
    public void incrementInventory(Long skuId, int quantity);

    /**
     * Attempts to increment inventory for a map of Sku ids and quantities.
     * 
     * Quantities must not be null and must be greater than zero or an IllegalArgumentException will be thrown.
     * 
     * @param skuQuantities
     */
    public void incrementInventory(Map<Long, Integer> skuQuantities);

}
