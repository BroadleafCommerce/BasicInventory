package org.broadleafcommerce.inventory.basic.dao;


public interface BasicInventoryDao {

    public int readInventory(Long skuId);

    public int readInventoryForUpdate(Long skuId);

    public void updateInventory(Long skuId, int quanity);

}
