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
package labs.pm.app;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;

/**
 *
 * @author bhagc
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProductManager pm = ProductManager.getInstance();
        
        AtomicInteger clientCount=new AtomicInteger(0);
        Callable<String> client=()->{
            String clientId="Client"+clientCount.incrementAndGet();
            String threadName=Thread.currentThread().getName();
            int productId=ThreadLocalRandom.current().nextInt(3)+1;
            String languageTag=ProductManager.getSupportedLocale()
                    .stream()
                    .skip(ThreadLocalRandom.current().nextInt(4))
                    .findFirst().get();
            
            StringBuilder log=new StringBuilder();
            log.append(clientId).append("  ").append(threadName).append("\n\t start of log\t\n");
            log.append(pm.getDiscounts(languageTag)
                    .entrySet().stream()
                    .map(p->p.getKey()+"\t"+p.getValue())
                    .collect(Collectors.joining("\n")));
            Product product=pm.reviewProduct(productId, Rating.FOUR_STAR, 
                    "yet another review");
            log.append(product!=null?
                    "\nProduct"+productId+"\tReveiwed\n"
                    :"\nProduct"+productId+"\tNot Reviewed\n");
            pm.printProductReport(productId, languageTag, clientId);
            log.append(clientId).append(" generated report for ")
                    .append(productId).append(" product");
            log.append("\n\t end of log\t\n");
            return log.toString();
        };

        List<Callable<String>> clients=Stream.generate(()->client)
                .limit(3)
                .collect(Collectors.toList());
        ExecutorService es=Executors.newFixedThreadPool(2);
        try {
            List<Future<String>> list=es.invokeAll(clients,5,TimeUnit.SECONDS);
            list.forEach(e->{
                try {
                    System.out.println(e.get());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error retreving client log", ex);
                }
            });
            es.shutdown();
        } catch (InterruptedException ex) {
            Logger.getLogger(Shop.class.getName()).log(Level.SEVERE, "Error invoking clients", ex);
        } 
    }

}

