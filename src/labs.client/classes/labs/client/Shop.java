/*
 * Copyright (C) 2023 bhagc
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package labs.client;

import io.helidon.webserver.Routing;
import java.math.BigDecimal;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
//import labs.file.service.ProductFileManager;
import labs.pm.data.Product;
import labs.pm.data.Rating;
import labs.pm.service.ProductManagerException;
import labs.pm.service.ProductManager;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 *
 * @author bhagc
 */
public class Shop {

   private static final Logger logger=Logger.getLogger(Shop.class.getName());
    public static void main(String[] args) {
        try {
            ResourceFormatter formatter=ResourceFormatter.getResourceFormatter("fr-FR");
           ServiceLoader<ProductManager> serviceLoader=ServiceLoader.load(ProductManager.class);
            ProductManager pm=serviceLoader.findFirst().get();

     ServerConfiguration config=ServerConfiguration.builder()
             .bindAddress(InetAddress.getLocalHost())
             .port(8080).build();
     
     Routing routing =Routing.builder()
             .any("/",(request,response)->{
             response.send("Enter id in url to find Product");
             })
             .get("/{id}",(req,res)->{
                 String result=null;
                try {
                    int id=Integer.parseInt(req.path().param("id"));
                    Product pd=pm.findProduct(id);
                    result=pd.toString();
                } catch (ProductManagerException ex) {
                    logger.log(Level.INFO, ex.getMessage());
                    result="Product with this id "+req.path().param("id")+" not found";
                }
                res.send(result);
        }).build();
        
     WebServer server=WebServer.create(config, routing);
     server.start();
                    
        }catch(UnknownHostException ex){
            logger.log(Level.INFO, ex.getMessage());
        }
       
    }
    
}
