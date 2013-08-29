# Data Setup

The data setup for the Basic Inventory Module is straight forward.  However, there are a few things to understand. When 
using this module, there is a new field that is dynamically woven into Broadleaf's ```SkuImpl``` class. This field 
maps to a column called ```BASIC_INVENTORY_QTY_AVAIL``` in the ```BLC_SKU``` table.  This is the field that stores 
inventory quantities.  There are several other fields that are evaluated in addition to this. In particular the following 
will be evaluated:

- ```BLC_SKU.AVAILABLE_FLAG (Sku.isAvailable())```
- ```BLC_SKU.ACTIVE_START_DATE (Sku.isActive())```
- ```BLC_SKU.ACTIVE_END_DATE (Sku.isActive())```
- ```BLC_SKU.INVENTORY_TYPE (Sku.getInventoryType())```

If the ```Sku.isAvailable()``` method returns false, then the Sku is determined to be unavailable. There are 3 acceptable 
values for this field: 'Y', 'N', or null.

If the ```Sku.isActive()``` method returns false, then the Sku is determined to be unavailable. If the 
```Sku.getActiveStartDate()``` method returns a non null date in the past, and ```Sku.getActiveEndDate()``` returns 
a null value _or_ a date in the future, then the Sku is considered generally available. Otherwise, it is considered 
generally unavailable.

The ```INVENTORY_TYPE``` field must be one of three acceptable values: 'BASIC', 'NONE', or null.  If the inventory type 
is 'BASIC', the ```BASIC_INVENTORY_QTY_AVAIL``` is used to determine availability. If it is 'NONE' or null, then 
the Basic Inventory module assumes that the Sku is available, but will not return an actual inventory value. Inventory 
values that are returned in this case will be null.  This is useful when you want to allow a Sku to be sold, but 
you do not wish to maintain inventory on the Sku (e.g. digital products or products that have 'unlimited' inventory.

So, to summarize, the Sku must be available (flag can be null), active, have an inventory type of 'BASIC' or 'NONE', 
and have a quantity available greater than zero (null values in this field will be treated as zero).
