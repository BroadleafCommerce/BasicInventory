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

package org.broadleafcommerce.inventory.basic.service.workflow;

import org.broadleafcommerce.core.checkout.service.workflow.CheckoutContext;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.state.ActivityStateManagerImpl;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

/**
 * Default workflow activity to decrement inventory.
 * 
 * @author Kelly Tisdell
 *
 */
public class DecrementBasicInventoryActivity extends BaseActivity<CheckoutContext> {

    @Resource(name = "blBasicInventoryService")
    protected BasicInventoryService inventoryService;

    public DecrementBasicInventoryActivity() {
        super();
        super.setAutomaticallyRegisterRollbackHandler(false);
    }

    @Override
    public CheckoutContext execute(CheckoutContext context) throws Exception {

        CheckoutSeed seed = context.getSeedData();
        List<OrderItem> orderItems = seed.getOrder().getOrderItems();

        //map to hold skus and quantity purchased
        HashMap<Long, Integer> skuInventoryMap = new HashMap<Long, Integer>();

        for (OrderItem orderItem : orderItems) {
            if (orderItem instanceof DiscreteOrderItem) {
                Long skuId = ((DiscreteOrderItem) orderItem).getSku().getId();
                Integer quantity = skuInventoryMap.get(skuId);
                if (quantity == null) {
                    quantity = orderItem.getQuantity();
                } else {
                    quantity += orderItem.getQuantity();
                }
                skuInventoryMap.put(skuId, quantity);
            } else if (orderItem instanceof BundleOrderItem) {
                BundleOrderItem bundleItem = (BundleOrderItem) orderItem;
                skuInventoryMap.put(bundleItem.getSku().getId(), bundleItem.getQuantity());
                List<DiscreteOrderItem> discreteItems = bundleItem.getDiscreteOrderItems();
                for (DiscreteOrderItem discreteItem : discreteItems) {
                    Integer quantity = skuInventoryMap.get(discreteItem.getSku().getId());
                    if (quantity == null) {
                        quantity = discreteItem.getQuantity();
                    } else {
                        quantity += discreteItem.getQuantity();
                    }
                    skuInventoryMap.put(discreteItem.getSku().getId(), quantity);
                }
            }
        }

        if (!skuInventoryMap.isEmpty()) {
            inventoryService.decrementInventory(skuInventoryMap);
        }

        if (getRollbackHandler() != null && !getAutomaticallyRegisterRollbackHandler()) {
            Map<String, Object> myState = new HashMap<String, Object>();
            if (getStateConfiguration() != null && !getStateConfiguration().isEmpty()) {
                myState.putAll(getStateConfiguration());
            }
            myState.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_INVENTORY_DECREMENTED, skuInventoryMap);
            myState.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_ORDER_ID, seed.getOrder().getId());
            ActivityStateManagerImpl.getStateManager().registerState(this, context, getRollbackRegion(), getRollbackHandler(), myState);
        }

        return context;

    }

}
