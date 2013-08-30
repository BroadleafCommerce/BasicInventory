package org.broadleafcommerce.inventory.basic.test;

import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.catalog.service.CatalogService;
import org.broadleafcommerce.core.inventory.service.type.InventoryType;
import org.broadleafcommerce.inventory.basic.dao.BasicInventoryDao;
import org.broadleafcommerce.inventory.basic.domain.ext.BasicInventoryContainer;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryServiceImpl;
import org.broadleafcommerce.inventory.basic.service.BasicInventoryUnavailableException;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Assert;

public class BasicInventoryTest {

    private final BasicInventoryServiceImpl inventoryService = new BasicInventoryServiceImpl();

    @Before
    public void setup() {
    }

    @Test
    public void testRetrieveQuanityAvailable() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(true, 100, true, InventoryType.BASIC, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);
        
        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        Integer inventory = inventoryService.retrieveQuantityAvailable(1L);
        Assert.assertEquals(100, inventory.intValue());
    }

    @Test
    public void testRetrieveQuanityActiveNull() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(true, 100, null, InventoryType.BASIC, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        Integer inventory = inventoryService.retrieveQuantityAvailable(1L);
        Assert.assertEquals(100, inventory.intValue());
    }

    @Test
    public void testRetrieveQuanityInventoryTypeNull() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(true, 100, null, null, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        Integer inventory = inventoryService.retrieveQuantityAvailable(1L);
        Assert.assertNull(inventory);
    }

    @Test
    public void testRetrieveQuantitiesAvailable() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku1 = createSku(true, 100, null, InventoryType.BASIC, 1L);
        Sku sku2 = createSku(true, 0, null, InventoryType.NONE, 2L);//This sku should have a null inventory
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku1).anyTimes();
        EasyMock.expect(catalogService.findSkuById(2L)).andReturn(sku2).anyTimes();

        HashSet<Long> ids = new HashSet<Long>();
        ids.add(1L);
        ids.add(2L);

        Map<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 100);
        inventories.put(2L, 100);

        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(ids)).andReturn(inventories).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);

        inventories = inventoryService.retrieveQuantitiesAvailable(ids);
        Assert.assertEquals(100, inventories.get(1L).intValue());
        Assert.assertNull(inventories.get(2L));
    }

    @Test
    public void isAvailable() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(true, 100, true, InventoryType.BASIC, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        boolean available = inventoryService.isAvailable(1L, 100);
        Assert.assertEquals(true, available);
    }

    @Test
    public void isNotAvailableNotActive() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(false, 100, true, InventoryType.BASIC, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        boolean available = inventoryService.isAvailable(1L, 1);
        Assert.assertEquals(false, available);
    }

    @Test
    public void isAvailableNoneInventoryType() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(true, null, true, InventoryType.NONE, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        boolean available = inventoryService.isAvailable(1L, 1);
        Assert.assertEquals(true, available);
    }

    @Test
    public void isNotAvailable() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku = createSku(true, 100, true, InventoryType.BASIC, 1L);
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku).anyTimes();
        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andReturn(100).anyTimes();
        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);
        boolean available = inventoryService.isAvailable(1L, 101);
        Assert.assertEquals(false, available);
    }

    @Test
    public void testDecrementInventories() throws BasicInventoryUnavailableException {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku1 = createSku(true, 100, null, InventoryType.BASIC, 1L);
        Sku sku2 = createSku(true, 0, null, InventoryType.NONE, 2L);//This sku should have a null inventory
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku1).anyTimes();
        EasyMock.expect(catalogService.findSkuById(2L)).andReturn(sku2).anyTimes();

        //Since one sku has inventory type of none, only one sku's inventory should be updated.
        EasyMock.expect(catalogService.saveSku(EasyMock.anyObject(Sku.class))).andReturn(EasyMock.anyObject(Sku.class)).once();

        HashSet<Long> ids = new HashSet<Long>();
        ids.add(1L);
        ids.add(2L);

        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andAnswer(new IAnswer<Integer>() {
            @Override
            public Integer answer() throws Throwable {
                return 100;
            }
        }).once();

        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        inventoryService.decrementInventory(inventories);

        EasyMock.verify(dao);
        EasyMock.verify(catalogService);

    }

    @Test(expected = BasicInventoryUnavailableException.class)
    public void testDecrementInventoriesUnavailable() throws BasicInventoryUnavailableException {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku1 = createSku(true, 100, null, InventoryType.BASIC, 1L);
        Sku sku2 = createSku(true, 0, null, InventoryType.BASIC, 2L);//This sku should have a null inventory
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku1).anyTimes();
        EasyMock.expect(catalogService.findSkuById(2L)).andReturn(sku2).anyTimes();

        //Since one sku has inventory type of none, only one sku's inventory should be updated.
        EasyMock.expect(catalogService.saveSku(EasyMock.anyObject(Sku.class))).andReturn(EasyMock.anyObject(Sku.class)).once();

        HashSet<Long> ids = new HashSet<Long>();
        ids.add(1L);
        ids.add(2L);

        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andAnswer(new IAnswer<Integer>() {
            @Override
            public Integer answer() throws Throwable {
                return 100;
            }
        }).once();

        EasyMock.expect(dao.readInventory(2L)).andAnswer(new IAnswer<Integer>() {
            @Override
            public Integer answer() throws Throwable {
                return 0;
            }
        }).once();

        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        inventoryService.decrementInventory(inventories);

        EasyMock.verify(dao);
        EasyMock.verify(catalogService);
    }

    @Test
    public void testIncrementInventory() {
        CatalogService catalogService = EasyMock.createMock(CatalogService.class);
        Sku sku1 = createSku(true, 100, null, InventoryType.BASIC, 1L);
        Sku sku2 = createSku(true, 0, null, InventoryType.BASIC, 2L);//This sku should have a null inventory
        EasyMock.expect(catalogService.findSkuById(1L)).andReturn(sku1).anyTimes();
        EasyMock.expect(catalogService.findSkuById(2L)).andReturn(sku2).anyTimes();

        //Since one sku has inventory type of none, only one sku's inventory should be updated.
        EasyMock.expect(catalogService.saveSku(EasyMock.anyObject(Sku.class))).andReturn(EasyMock.anyObject(Sku.class)).times(2);

        HashSet<Long> ids = new HashSet<Long>();
        ids.add(1L);
        ids.add(2L);

        BasicInventoryDao dao = EasyMock.createMock(BasicInventoryDao.class);
        EasyMock.expect(dao.readInventory(1L)).andAnswer(new IAnswer<Integer>() {
            @Override
            public Integer answer() throws Throwable {
                return 100;
            }
        }).once();

        EasyMock.expect(dao.readInventory(2L)).andAnswer(new IAnswer<Integer>() {
            @Override
            public Integer answer() throws Throwable {
                return 100;
            }
        }).once();

        EasyMock.replay(catalogService, dao);

        inventoryService.setCatalogService(catalogService);
        inventoryService.setInventoryDao(dao);

        HashMap<Long, Integer> inventories = new HashMap<Long, Integer>();
        inventories.put(1L, 10);
        inventories.put(2L, 10);
        inventoryService.incrementInventory(inventories);

        EasyMock.verify(dao);
        EasyMock.verify(catalogService);
    }

    /*
     * We need to use this to create skus because we need to mock multiple interfaces.
     */
    private Sku createSku(final boolean active, final Integer inventoryCount, final Boolean available, final InventoryType type, final Long id) {
        InvocationHandler handler = new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("isActive".equals(method.getName()) || "getActive".equals(method.getName())) {
                    return active;
                }
                if ("getQuantityAvailable".equals(method.getName())) {
                    return inventoryCount;
                }
                if ("isAvailable".equals(method.getName())) {
                    return available;
                }
                if ("getInventoryType".equals(method.getName())) {
                    return type;
                }
                if ("getId".equals(method.getName())) {
                    return id;
                }

                return null;
            }
        };
        return (Sku) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[] { Sku.class, BasicInventoryContainer.class }, handler);
    }
}
