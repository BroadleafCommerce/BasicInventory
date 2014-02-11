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
package org.broadleafcommerce.inventory.basic.test;

import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.workflow.Activity;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.core.workflow.state.RollbackFailureException;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryService;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryUnavailableException;
import org.broadleafcommerce.inventory.basic.service.workflow.BasicInventoryRollbackHandler;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

/**
 * 
 * @author Kelly Tisdell
 *
 */
public class BasicInventoryRollbackHandlerTest {

    @Before
    public void setup() {
    }

    @Test
    public void testRollbackNoState() throws RollbackFailureException {
        BasicInventoryRollbackHandler handler = new BasicInventoryRollbackHandler();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        handler.setInventoryService(service);
        @SuppressWarnings("unchecked")
        Activity<? extends ProcessContext<CheckoutSeed>> activity = EasyMock.createMock(Activity.class);
        @SuppressWarnings("unchecked")
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);
        handler.rollbackState(activity, context, null);
    }

    @Test
    public void testRollbackIncrement() throws RollbackFailureException {
        BasicInventoryRollbackHandler handler = new BasicInventoryRollbackHandler();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        service.incrementInventory(inventories);
        EasyMock.expectLastCall().once();
        handler.setInventoryService(service);
        EasyMock.replay(service);

        @SuppressWarnings("unchecked")
        Activity<? extends ProcessContext<CheckoutSeed>> activity = EasyMock.createMock(Activity.class);
        @SuppressWarnings("unchecked")
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);

        HashMap<String, Object> state = new HashMap<String, Object>();
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_ORDER_ID, 100L);
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_INVENTORY_DECREMENTED, inventories);
        handler.rollbackState(activity, context, state);
        EasyMock.verify(service);
    }

    @Test(expected = RollbackFailureException.class)
    public void testRollbackIncrementException() throws RollbackFailureException {
        BasicInventoryRollbackHandler handler = new BasicInventoryRollbackHandler();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        service.incrementInventory(inventories);
        EasyMock.expectLastCall().andThrow(new NullPointerException());
        handler.setInventoryService(service);
        EasyMock.replay(service);

        @SuppressWarnings("unchecked")
        Activity<? extends ProcessContext<CheckoutSeed>> activity = EasyMock.createMock(Activity.class);
        @SuppressWarnings("unchecked")
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);

        HashMap<String, Object> state = new HashMap<String, Object>();
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_ORDER_ID, 100L);
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_INVENTORY_DECREMENTED, inventories);
        handler.rollbackState(activity, context, state);
    }

    @Test
    public void testRollbackDecrement() throws RollbackFailureException, BasicInventoryUnavailableException {
        BasicInventoryRollbackHandler handler = new BasicInventoryRollbackHandler();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        service.decrementInventory(inventories);
        EasyMock.expectLastCall().once();
        handler.setInventoryService(service);
        EasyMock.replay(service);

        @SuppressWarnings("unchecked")
        Activity<? extends ProcessContext<CheckoutSeed>> activity = EasyMock.createMock(Activity.class);
        @SuppressWarnings("unchecked")
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);

        HashMap<String, Object> state = new HashMap<String, Object>();
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_ORDER_ID, 100L);
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_INVENTORY_INCREMENTED, inventories);
        handler.rollbackState(activity, context, state);
        EasyMock.verify(service);
    }

    @Test(expected = RollbackFailureException.class)
    public void testRollbackDecrementException() throws RollbackFailureException, BasicInventoryUnavailableException {
        BasicInventoryRollbackHandler handler = new BasicInventoryRollbackHandler();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        service.decrementInventory(inventories);
        EasyMock.expectLastCall().andThrow(new BasicInventoryUnavailableException(1L, 10, 9));
        handler.setInventoryService(service);
        EasyMock.replay(service);

        @SuppressWarnings("unchecked")
        Activity<? extends ProcessContext<CheckoutSeed>> activity = EasyMock.createMock(Activity.class);
        @SuppressWarnings("unchecked")
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);

        HashMap<String, Object> state = new HashMap<String, Object>();
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_ORDER_ID, 100L);
        state.put(BasicInventoryRollbackHandler.ROLLBACK_BLC_INVENTORY_INCREMENTED, inventories);
        handler.rollbackState(activity, context, state);
    }
}
