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

import org.broadleafcommerce.common.logging.SupportLogManager;
import org.broadleafcommerce.common.logging.SupportLogger;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.workflow.Activity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.core.workflow.state.RollbackFailureException;
import org.broadleafcommerce.core.workflow.state.RollbackHandler;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryService;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryUnavailableException;

import java.util.Map;

import javax.annotation.Resource;

/**
 * Provides a standard rollback handler to ensure that in the event of something going wrong in a workflow, that 
 * 
 * @author Kelly Tisdell
 *
 */
public class BasicInventoryRollbackHandler implements RollbackHandler<CheckoutSeed> {

    private static final SupportLogger LOG = SupportLogManager.getLogger("broadleaf-basic-inventory", BasicInventoryRollbackHandler.class);
    
    public static final String ROLLBACK_BLC_INVENTORY_DECREMENTED = "ROLLBACK_BLC_INVENTORY_DECREMENTED";
    public static final String ROLLBACK_BLC_INVENTORY_INCREMENTED = "ROLLBACK_BLC_INVENTORY_INCREMENTED";
    public static final String ROLLBACK_BLC_ORDER_ID = "ROLLBACK_BLC_ORDER_ID";

    @Resource(name = "blBasicInventoryService")
    protected BasicInventoryService inventoryService;

    @Override
    public void rollbackState(Activity<? extends ProcessContext<CheckoutSeed>> activity, ProcessContext<CheckoutSeed> processContext, Map<String, Object> stateConfiguration)
            throws RollbackFailureException {

        if (stateConfiguration == null ||
                (stateConfiguration.get(ROLLBACK_BLC_INVENTORY_DECREMENTED) == null &&
                stateConfiguration.get(ROLLBACK_BLC_INVENTORY_INCREMENTED) == null)) {
            return;
        }

        String orderId = "(Not Known)";
        if (stateConfiguration.get(ROLLBACK_BLC_ORDER_ID) != null) {
            orderId = String.valueOf(stateConfiguration.get(ROLLBACK_BLC_ORDER_ID));
        }

        @SuppressWarnings("unchecked")
        Map<Long, Integer> inventoryToIncrement = (Map<Long, Integer>) stateConfiguration.get(ROLLBACK_BLC_INVENTORY_DECREMENTED);
        if (inventoryToIncrement != null && !inventoryToIncrement.isEmpty()) {
            try {
                inventoryService.incrementInventory(inventoryToIncrement);
            } catch (Exception ex) {
                RollbackFailureException rfe = new RollbackFailureException("An unexpected error occured in the error handler of the checkout workflow trying to compensate for inventory. This happend for order ID: " +
                        orderId + ". This should be corrected manually!", ex);
                rfe.setActivity(activity);
                rfe.setProcessContext(processContext);
                rfe.setStateItems(stateConfiguration);
                throw rfe;
            }
        }

        @SuppressWarnings("unchecked")
        Map<Long, Integer> inventoryToDecrement = (Map<Long, Integer>) stateConfiguration.get(ROLLBACK_BLC_INVENTORY_INCREMENTED);
        if (inventoryToDecrement != null && !inventoryToDecrement.isEmpty()) {
            try {
                inventoryService.decrementInventory(inventoryToDecrement);
            } catch (BasicInventoryUnavailableException e) {
                //This is an awkward, unlikely state.  I just added some inventory, but something happened, and I want to remove it, but it's already gone!
                RollbackFailureException rfe = new RollbackFailureException("While trying roll back (decrement) inventory, we found that there was none left decrement.", e);
                rfe.setActivity(activity);
                rfe.setProcessContext(processContext);
                rfe.setStateItems(stateConfiguration);
                throw rfe;
            } catch (RuntimeException ex) {
                LOG.error("An unexpected error occured in the error handler of the checkout workflow trying to compensate for inventory. This happend for order ID: " +
                        orderId + ". This should be corrected manually!", ex);
                RollbackFailureException rfe = new RollbackFailureException("An unexpected error occured in the error handler of the checkout workflow " +
                        "trying to compensate for inventory. This happend for order ID: " +
                        orderId + ". This should be corrected manually!", ex);
                rfe.setActivity(activity);
                rfe.setProcessContext(processContext);
                rfe.setStateItems(stateConfiguration);
                throw rfe;
            }
        }
    }

}
