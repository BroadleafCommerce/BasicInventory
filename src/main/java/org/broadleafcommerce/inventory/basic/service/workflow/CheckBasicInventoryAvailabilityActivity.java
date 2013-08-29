/*
 * Copyright 2008-2012 the original author or authors.
 *
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
 */

package org.broadleafcommerce.inventory.basic.service.workflow;

import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.order.service.workflow.CartOperationRequest;
import org.broadleafcommerce.core.workflow.BaseActivity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryService;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryUnavailableException;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

public class CheckBasicInventoryAvailabilityActivity extends BaseActivity<ProcessContext<CartOperationRequest>> {

    @Resource(name = "blBasicInventoryService")
    protected BasicInventoryService inventoryService;

    @Override
    public ProcessContext<CartOperationRequest> execute(ProcessContext<CartOperationRequest> context) throws Exception {

        CartOperationRequest request = context.getSeedData();
        HashMap<Long, Integer> skuQuantities = new HashMap<Long, Integer>();
        OrderItem orderItem = request.getAddedOrderItem();
        if (orderItem instanceof DiscreteOrderItem) {
            Long skuId = ((DiscreteOrderItem) orderItem).getSku().getId();
            skuQuantities.put(skuId, orderItem.getQuantity());
        } else if (orderItem instanceof BundleOrderItem) {
            BundleOrderItem bundleItem = (BundleOrderItem) orderItem;
            skuQuantities.put(bundleItem.getSku().getId(), bundleItem.getQuantity());
            List<DiscreteOrderItem> discreteItems = bundleItem.getDiscreteOrderItems();
            for (DiscreteOrderItem discreteItem : discreteItems) {
                Integer quantity = skuQuantities.get(discreteItem.getSku().getId());
                if (quantity == null) {
                    quantity = discreteItem.getQuantity();
                } else {
                    quantity += discreteItem.getQuantity();
                }
                skuQuantities.put(discreteItem.getSku().getId(), quantity);
            }
        } else {
            return context;
        }

        if (!skuQuantities.isEmpty()) {
            Set<Long> keys = skuQuantities.keySet();
            for (Long key : keys) {
                //Available inventory will not be decremented for this sku until checkout. This activity is assumed to be
                //part of the add to cart / update cart workflow
                boolean quantityAvailable = inventoryService.isAvailable(key, skuQuantities.get(key));

                if (!quantityAvailable) {
                    String errorMessage = "Error: Sku with id of " + key + " does not have " +
                            request.getItemRequest().getQuantity() + " items in available inventory.";
                    throw new BasicInventoryUnavailableException(errorMessage, key, skuQuantities.get(key), inventoryService.retrieveQuantityAvailable(key));
                }
            }
        }
        
        return context;
    }

}
