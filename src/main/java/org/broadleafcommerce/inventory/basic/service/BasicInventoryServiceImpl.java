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

import org.broadleafcommerce.inventory.basic.dao.BasicInventoryDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service("blBasicInventoryService")
@Transactional("blTransactionManager")
public class BasicInventoryServiceImpl implements BasicInventoryService {

    @Resource(name = "blBasicInventoryDao")
    protected BasicInventoryDao inventoryDao;
    
    @Override
    public boolean isQuantityAvailable(Long skuId, int quantity) {
        if (quantity < 1) {
            return true;
        }
        int inventory = inventoryDao.readInventory(skuId);
        if (inventory >= quantity) {
            return true;
        }
        return false;
    }

    @Override
    public int retrieveQuantityAvailable(Long skuId) {
        return inventoryDao.readInventory(skuId);
    }

    @Override
    public void decrementInventory(Long skuId, int quantity) {
        int inventory = inventoryDao.readInventoryForUpdate(skuId);
        int newInventory = inventory - quantity;
        inventoryDao.updateInventory(skuId, newInventory);
    }

    @Override
    public void incrementInventory(Long skuId, int quantity) {
        int inventory = inventoryDao.readInventoryForUpdate(skuId);
        int newInventory = inventory + quantity;
        inventoryDao.updateInventory(skuId, newInventory);
    }

}
