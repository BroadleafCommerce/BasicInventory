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

package org.broadleafcommerce.inventory.basic.test;

import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.order.domain.BundleOrderItem;
import org.broadleafcommerce.core.order.domain.DiscreteOrderItem;
import org.broadleafcommerce.core.order.service.workflow.CartOperationRequest;
import org.broadleafcommerce.core.workflow.ProcessContext;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryService;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryUnavailableException;
import org.broadleafcommerce.inventory.basic.service.workflow.CheckBasicInventoryAvailabilityActivity;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * 
 * @author Kelly Tisdell
 *
 */
public class CheckBasicInventoryWorkflowActivityTest {

    @Before
    public void setup() {
    }

    @Test
    public void checkPositiveDiscreteAvailabilityTest() throws Exception {
        CheckBasicInventoryAvailabilityActivity activity = new CheckBasicInventoryAvailabilityActivity();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        EasyMock.expect(service.isAvailable(EasyMock.anyLong(), EasyMock.anyInt())).andReturn(true).once();

        @SuppressWarnings("unchecked")
        ProcessContext<CartOperationRequest> context = EasyMock.createMock(ProcessContext.class);
        CartOperationRequest request = EasyMock.createMock(CartOperationRequest.class);
        DiscreteOrderItem orderItem = EasyMock.createMock(DiscreteOrderItem.class);
        Sku sku = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku.getId()).andReturn(1L);
        EasyMock.expect(orderItem.getSku()).andReturn(sku);
        EasyMock.expect(orderItem.getQuantity()).andReturn(10);
        EasyMock.expect(request.getAddedOrderItem()).andReturn(orderItem);

        EasyMock.expect(context.getSeedData()).andReturn(request);
        
        EasyMock.replay(context, request, service, orderItem, sku);

        activity.setInventoryService(service);
        activity.execute(context);
        EasyMock.verify(service);
    }

    @Test
    public void checkPositiveBundleAvailabilityTest() throws Exception {
        CheckBasicInventoryAvailabilityActivity activity = new CheckBasicInventoryAvailabilityActivity();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        EasyMock.expect(service.isAvailable(EasyMock.anyLong(), EasyMock.anyInt())).andReturn(true).times(2);

        @SuppressWarnings("unchecked")
        ProcessContext<CartOperationRequest> context = EasyMock.createMock(ProcessContext.class);
        CartOperationRequest request = EasyMock.createMock(CartOperationRequest.class);
        BundleOrderItem bundleItem = EasyMock.createMock(BundleOrderItem.class);
        Sku sku1 = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku1.getId()).andReturn(1L).anyTimes();
        EasyMock.expect(bundleItem.getSku()).andReturn(sku1).anyTimes();
        EasyMock.expect(bundleItem.getQuantity()).andReturn(10).anyTimes();
        EasyMock.expect(request.getAddedOrderItem()).andReturn(bundleItem);

        Sku sku2 = EasyMock.createMock(Sku.class);
        DiscreteOrderItem discreteItem = EasyMock.createMock(DiscreteOrderItem.class);
        EasyMock.expect(sku2.getId()).andReturn(2L).anyTimes();
        EasyMock.expect(discreteItem.getSku()).andReturn(sku2).anyTimes();
        EasyMock.expect(discreteItem.getQuantity()).andReturn(10).anyTimes();
        ArrayList<DiscreteOrderItem> discreteItems = new ArrayList<DiscreteOrderItem>();
        discreteItems.add(discreteItem);
        EasyMock.expect(bundleItem.getDiscreteOrderItems()).andReturn(discreteItems).once();

        EasyMock.expect(context.getSeedData()).andReturn(request);

        EasyMock.replay(context, request, service, bundleItem, sku1, sku2, discreteItem);

        activity.setInventoryService(service);
        activity.execute(context);
        EasyMock.verify(service);
    }

    @Test(expected = BasicInventoryUnavailableException.class)
    public void checkNegativeDiscreteAvailabilityTest() throws Exception {
        CheckBasicInventoryAvailabilityActivity activity = new CheckBasicInventoryAvailabilityActivity();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        EasyMock.expect(service.isAvailable(EasyMock.anyLong(), EasyMock.anyInt())).andReturn(false).once();
        EasyMock.expect(service.retrieveQuantityAvailable(EasyMock.anyLong())).andReturn(0).once();

        @SuppressWarnings("unchecked")
        ProcessContext<CartOperationRequest> context = EasyMock.createMock(ProcessContext.class);
        CartOperationRequest request = EasyMock.createMock(CartOperationRequest.class);
        DiscreteOrderItem orderItem = EasyMock.createMock(DiscreteOrderItem.class);
        Sku sku = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku.getId()).andReturn(1L);
        EasyMock.expect(orderItem.getSku()).andReturn(sku);
        EasyMock.expect(orderItem.getQuantity()).andReturn(10).times(2);
        EasyMock.expect(request.getAddedOrderItem()).andReturn(orderItem).times(2);

        EasyMock.expect(context.getSeedData()).andReturn(request);

        EasyMock.replay(context, request, service, orderItem, sku);

        activity.setInventoryService(service);
        activity.execute(context);
    }

    @Test(expected = BasicInventoryUnavailableException.class)
    public void checkNegativeBundleAvailabilityTest() throws Exception {
        CheckBasicInventoryAvailabilityActivity activity = new CheckBasicInventoryAvailabilityActivity();
        BasicInventoryService service = EasyMock.createMock(BasicInventoryService.class);
        EasyMock.expect(service.isAvailable(EasyMock.anyLong(), EasyMock.anyInt())).andReturn(false);
        EasyMock.expect(service.retrieveQuantityAvailable(EasyMock.anyLong())).andReturn(0).once();

        @SuppressWarnings("unchecked")
        ProcessContext<CartOperationRequest> context = EasyMock.createMock(ProcessContext.class);
        CartOperationRequest request = EasyMock.createMock(CartOperationRequest.class);
        BundleOrderItem bundleItem = EasyMock.createMock(BundleOrderItem.class);
        Sku sku1 = EasyMock.createMock(Sku.class);
        EasyMock.expect(sku1.getId()).andReturn(1L).anyTimes();
        EasyMock.expect(bundleItem.getSku()).andReturn(sku1).anyTimes();
        EasyMock.expect(bundleItem.getQuantity()).andReturn(10).anyTimes();
        EasyMock.expect(request.getAddedOrderItem()).andReturn(bundleItem).times(2);

        Sku sku2 = EasyMock.createMock(Sku.class);
        DiscreteOrderItem discreteItem = EasyMock.createMock(DiscreteOrderItem.class);
        EasyMock.expect(sku2.getId()).andReturn(2L).anyTimes();
        EasyMock.expect(discreteItem.getSku()).andReturn(sku2).anyTimes();
        EasyMock.expect(discreteItem.getQuantity()).andReturn(10).anyTimes();
        ArrayList<DiscreteOrderItem> discreteItems = new ArrayList<DiscreteOrderItem>();
        discreteItems.add(discreteItem);
        EasyMock.expect(bundleItem.getDiscreteOrderItems()).andReturn(discreteItems).once();

        EasyMock.expect(context.getSeedData()).andReturn(request);

        EasyMock.replay(context, request, service, bundleItem, sku1, sku2, discreteItem);

        activity.setInventoryService(service);
        activity.execute(context);
    }
}
