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

package org.broadleafcommerce.inventory.basic.service;

import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.inventory.service.type.InventoryType;
import org.broadleafcommerce.inventory.basic.dao.BasicInventoryDao;
import org.broadleafcommerce.inventory.basic.domain.ext.BasicInventoryContainer;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

public class BasicInventoryServiceImpl implements BasicInventoryService {

    @Resource(name = "blBasicInventoryDao")
    protected BasicInventoryDao inventoryDao;
    
    @Resource(name = "blCatalogService")
    protected CatalogService catalogService;

    protected boolean checkBasicAvailablility(Sku sku) {
        Boolean available = sku.isAvailable();
        if (available == null) {
            available = true;
        }
        if (sku != null && available && sku.isActive()) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional("blTransactionManager")
    public Integer retrieveQuantityAvailable(Long skuId) {
        Sku sku = catalogService.findSkuById(skuId);
        if (checkBasicAvailablility(sku)) {
            if (sku.getInventoryType() == null || sku.getInventoryType().equals(InventoryType.NONE)) {
                return null;
            }
            return inventoryDao.readInventory(skuId);
        }
        return 0;
    }

    @Override
    @Transactional("blTransactionManager")
    public Map<Long, Integer> retrieveQuantitiesAvailable(Set<Long> skuIds) {
        Map<Long, Integer> inventories = inventoryDao.readInventory(skuIds);
        HashMap<Long, Integer> out = new HashMap<Long, Integer>();
        for (Long key : skuIds) {
            Sku sku = catalogService.findSkuById(key);
            if (checkBasicAvailablility(sku)) {
                if (InventoryType.BASIC.equals(sku.getInventoryType())) {
                    out.put(key, inventories.get(key));
                } else if (sku.getInventoryType() == null || InventoryType.NONE.equals(sku.getInventoryType())) {
                    out.put(key, null);
                } else {
                    out.put(key, 0);
                }
            } else {
                out.put(key, 0);
            }
        }

        return out;
    }

    @Override
    @Transactional("blTransactionManager")
    public boolean isAvailable(Long skuId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity " + quantity + " is not valid. Must be greater than zero.");
        }
        Sku sku = catalogService.findSkuById(skuId);
        if (checkBasicAvailablility(sku)) {
            if (InventoryType.BASIC.equals(sku.getInventoryType())) {
                Integer quantityAvailable = retrieveQuantityAvailable(skuId);
                if (quantityAvailable == null || quantity <= quantityAvailable) {
                    return true;
                }
            } else if (sku.getInventoryType() == null || InventoryType.NONE.equals(sku.getInventoryType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(value = "blTransactionManager", rollbackFor = { BasicInventoryUnavailableException.class })
    public void decrementInventory(Long skuId, int quantity) throws BasicInventoryUnavailableException {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity " + quantity + " is not valid. Must be greater than zero.");
        }
        Sku sku = catalogService.findSkuById(skuId);
        if (checkBasicAvailablility(sku) && InventoryType.BASIC.equals(sku.getInventoryType())) {
            if (sku instanceof BasicInventoryContainer) {
                BasicInventoryContainer container = (BasicInventoryContainer) sku;
                Integer inventoryAvailable = retrieveQuantityAvailable(skuId);
                if (inventoryAvailable == null) {
                    return;
                }
                if (inventoryAvailable < quantity) {
                    throw new BasicInventoryUnavailableException(
                            "There was not enough inventory to fulfill this request.", skuId, quantity, inventoryAvailable);
                }
                int newInventory = inventoryAvailable - quantity;
                container.setQuantityAvailable(newInventory);
                catalogService.saveSku(sku);
            }
        }
    }

    @Override
    @Transactional(value = "blTransactionManager", rollbackFor = { BasicInventoryUnavailableException.class })
    public void decrementInventory(Map<Long, Integer> skuQuantities) throws BasicInventoryUnavailableException {
        Set<Long> keys = skuQuantities.keySet();
        for (Long key : keys) {
            Integer quantity = skuQuantities.get(key);
            if (quantity == null) {
                throw new IllegalArgumentException("Quantity was null for skuId " + key);
            }
            decrementInventory(key, quantity);
        }
    }

    @Override
    @Transactional("blTransactionManager")
    public void incrementInventory(Long skuId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity " + quantity + " is not valid. Must be greater than zero.");
        }
        Sku sku = catalogService.findSkuById(skuId);
        if (sku instanceof BasicInventoryContainer && InventoryType.BASIC.equals(sku.getInventoryType())) {
            BasicInventoryContainer container = (BasicInventoryContainer) sku;
            int inventoryAvailable = retrieveQuantityAvailable(skuId);
            int newInventory = inventoryAvailable + quantity;
            container.setQuantityAvailable(newInventory);
            catalogService.saveSku(sku);
        }
    }

    @Override
    @Transactional("blTransactionManager")
    public void incrementInventory(Map<Long, Integer> skuQuantities) {
        Set<Long> keys = skuQuantities.keySet();
        for (Long key : keys) {
            Integer quantity = skuQuantities.get(key);
            if (quantity == null) {
                throw new IllegalArgumentException("Quantity was null for skuId " + key);
            }
            incrementInventory(key, quantity);
        }
    }
}
