# Initial Configuration

By following the steps below, you will be able to add basic inventory functionality to your Broadleaf site.

## Steps to enable this module

1. Add the following to the dependency management section to your **parent** `pom.xml`:

    ```xml
    <dependency>
        <groupId>org.broadleafcommerce</groupId>
        <artifactId>basic-inventory</artifactId>
        <version>1.0.0-SNAPSHOT</version>
        <type>jar</type>
        <scope>compile</scope>
    </dependency>
    ```
    
2. Pull this dependency into your `core/pom.xml`:
    
    ```xml
    <dependency>
        <groupId>org.broadleafcommerce</groupId>
        <artifactId>basic-inventory</artifactId>
    </dependency>
    ```

3. Include the necessary `patchConfigLocation` files in your `admin/web.xml`:
    
    ```xml
    classpath:/bl-basic-inventory-applicationContext.xml
    ```
    > Note: This lines should go before the `classpath:/applicationContext.xml` line

4. Include the necessary `patchConfigLocation` file in your `site/web.xml`:
    
    ```xml
    classpath:/bl-basic-inventory-applicationContext.xml
    ```
    > Note: This line should go before the `classpath:/applicationContext.xml` line

5. Ensure that you have a Spring Instrumentation jar provided as a javaagent in your server startup command.
    > Check your server documenation on how to do this. However, to run the basic Jetty server with Broadleaf, you can 
modify the Ant build files in site and admin. Add the following to the ```jetty-demo``` and/or ```jetty-demo-jrebel``` 
tasks:

    ```xml
    <jvmarg value="-javaagent:/path/to/my/spring/instrumentation/jar" />
    ```

6. Launch the server and begin adding products to the cart.
    > Note: This may cause InventoryUnavailbleExceptions to be thrown. See [[Data Setup]] for more information.
