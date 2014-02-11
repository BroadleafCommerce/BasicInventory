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

import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.checkout.service.workflow.CheckoutSeed;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.domain.Order;
import org.broadleafcommerce.core.order.domain.OrderItem;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryService;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryUnavailableException;
import org.broadleafcommerce.inventory.basic.service.workflow.DecrementBasicInventoryActivity;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

/**
 * 
 * @author Kelly Tisdell
 *
 */
public class DecrementBasicInventoryWorkflowActivityTest {

    @Before
    public void setup() {
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecrementPositiveInventoryDiscreteItem() throws Exception {
        DecrementBasicInventoryActivity activity = new DecrementBasicInventoryActivity();
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);
        CheckoutSeed seed = EasyMock.createMock(CheckoutSeed.class);
        Order order = EasyMock.createMock(Order.class);
        DiscreteOrderItem discreteItem = EasyMock.createMock(DiscreteOrderItem.class);
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        Sku sku = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku.getId()).andReturn(1L).once();
        EasyMock.expect(discreteItem.getSku()).andReturn(sku).once();
        EasyMock.expect(discreteItem.getQuantity()).andReturn(1).once();
        EasyMock.expect(context.getSeedData()).andReturn(seed).once();
        EasyMock.expect(seed.getOrder()).andReturn(order).once();
        service.decrementInventory(EasyMock.anyObject(Map.class));
        EasyMock.expectLastCall().once();

        ArrayList<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(discreteItem);
        EasyMock.expect(order.getOrderItems()).andReturn(orderItems);

        EasyMock.replay(context, seed, order, discreteItem, service, sku);

        activity.setInventoryService(service);

        activity.execute(context);
        EasyMock.verify(context, seed, order, discreteItem, service, sku);
    }

    @SuppressWarnings("unchecked")
    @Test(expected = BasicInventoryUnavailableException.class)
    public void testDecrementNegativeInventoryDiscreteItem() throws Exception {
        DecrementBasicInventoryActivity activity = new DecrementBasicInventoryActivity();
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);
        CheckoutSeed seed = EasyMock.createMock(CheckoutSeed.class);
        Order order = EasyMock.createMock(Order.class);
        DiscreteOrderItem discreteItem = EasyMock.createMock(DiscreteOrderItem.class);
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        Sku sku = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku.getId()).andReturn(1L).once();
        EasyMock.expect(discreteItem.getSku()).andReturn(sku).once();
        EasyMock.expect(discreteItem.getQuantity()).andReturn(1).once();
        EasyMock.expect(context.getSeedData()).andReturn(seed).once();
        EasyMock.expect(seed.getOrder()).andReturn(order).once();
        service.decrementInventory(EasyMock.anyObject(Map.class));
        EasyMock.expectLastCall().andThrow(new BasicInventoryUnavailableException(1L, 1, 0));

        ArrayList<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(discreteItem);
        EasyMock.expect(order.getOrderItems()).andReturn(orderItems);

        EasyMock.replay(context, seed, order, discreteItem, service, sku);

        activity.setInventoryService(service);

        activity.execute(context);
        EasyMock.verify(context, seed, order, discreteItem, service, sku);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDecrementPositiveInventoryBundleItem() throws Exception {
        DecrementBasicInventoryActivity activity = new DecrementBasicInventoryActivity();
        ProcessContext<CheckoutSeed> context = EasyMock.createMock(ProcessContext.class);
        CheckoutSeed seed = EasyMock.createMock(CheckoutSeed.class);
        Order order = EasyMock.createMock(Order.class);

        BundleOrderItem bundleItem = EasyMock.createMock(BundleOrderItem.class);
        DiscreteOrderItem discreteItem = EasyMock.createMock(DiscreteOrderItem.class);
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        Sku sku1 = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku1.getId()).andReturn(1L).once();
        EasyMock.expect(bundleItem.getSku()).andReturn(sku1).once();
        EasyMock.expect(bundleItem.getQuantity()).andReturn(1).times(2);

        Sku sku2 = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku2.getId()).andReturn(2L).times(2);
        EasyMock.expect(discreteItem.getSku()).andReturn(sku2).times(2);
        EasyMock.expect(discreteItem.getQuantity()).andReturn(2).once();

        EasyMock.expect(context.getSeedData()).andReturn(seed).once();
        EasyMock.expect(seed.getOrder()).andReturn(order).once();
        service.decrementInventory(EasyMock.anyObject(Map.class));
        EasyMock.expectLastCall().once();

        ArrayList<DiscreteOrderItem> discreteItems = new ArrayList<DiscreteOrderItem>();
        discreteItems.add(discreteItem);

        EasyMock.expect(bundleItem.getDiscreteOrderItems()).andReturn(discreteItems);

        ArrayList<OrderItem> orderItems = new ArrayList<OrderItem>();
        orderItems.add(bundleItem);
        EasyMock.expect(order.getOrderItems()).andReturn(orderItems);

        EasyMock.replay(context, seed, order, bundleItem, discreteItem, service, sku1, sku2);

        activity.setInventoryService(service);

        activity.execute(context);
        EasyMock.verify(context, seed, order, discreteItem, service, sku1, sku2);
    }
}
