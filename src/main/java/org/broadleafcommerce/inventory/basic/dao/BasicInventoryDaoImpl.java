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

import org.broadleafcommerce.common.persistence.EntityConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

public class BasicInventoryDaoImpl implements BasicInventoryDao {

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name = "blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Override
    public int readInventory(Long skuId) {
        Query query = em.createNamedQuery("BC_BASIC_INVENTORY_READ_INVENTORY_FOR_SKU");
        query.setParameter("skuId", skuId);
        @SuppressWarnings("unchecked")
        List<Integer> results = query.getResultList();
        if (results != null && !results.isEmpty()) {
            Integer inventory = results.get(0);
            if (inventory != null) {
                return results.get(0);
            }
        }
        return 0;
    }

    @Override
    public Map<Long, Integer> readInventory(Set<Long> skuIds) {
        HashMap<Long, Integer> out = new HashMap<Long, Integer>();
        Query query = em.createNamedQuery("BC_BASIC_INVENTORY_READ_INVENTORY_FOR_SKUS");
        query.setParameter("skuIds", skuIds);
        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        for (Object[] result : results) {
            out.put((Long) result[0], (Integer) result[1]);
        }

        for (Long skuId : skuIds) {
            if (out.get(skuId) == null) {
                out.put(skuId, 0);
            }
        }

        return out;
    }

}
