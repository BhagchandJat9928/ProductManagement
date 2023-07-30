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
package lab.pm.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.Locale;
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
         ProductManager manager = new ProductManager(Locale.US);
        Product p1 = manager.createProduct(44, "Tea", BigDecimal.valueOf(10.0), Rating.FOUR_STAR);
        manager.reviewProduct(p1, Rating.ONE_STAR, "Really Nice");
        Product p2 = manager.createProduct(46, "Pizza", BigDecimal.valueOf(60.0), Rating.ONE_STAR, LocalDate.of(2023, Month.MARCH, 20));
        Product p3 = manager.createProduct(47, "Pizza", BigDecimal.valueOf(40.0), Rating.THREE_STAR, LocalDate.of(2024, Month.MARCH, 15));
        manager.reviewProduct(p2, Rating.TWO_STAR, "Nice design");
        manager.reviewProduct(45, Rating.FIVE_STAR, "Amazing");
        manager.reviewProduct(45, Rating.TWO_STAR, "Gorgeous");
        manager.printProductReport(102);
        manager.printProductReport(45);
        manager.changeLocale("en-IN");
        manager.printProductReport(45);
        Comparator<Product> ratingFilter = (a, b) -> a.getRating().ordinal() - b.getRating().ordinal();
        Comparator<Product> priceFilter = (a, b) -> b.getPrice().compareTo(a.getPrice());
        Comparator<Product> dateFilter = (a, b) -> a.getBestBefore().compareTo(b.getBestBefore());
        manager.printProducts(r -> r.getBestBefore()
                .isAfter(LocalDate.of(2023, Month.MARCH, 30)),
                ratingFilter);
        manager.printProducts(r -> r.getPrice().
                compareTo(BigDecimal.valueOf(11)) > 0,
                dateFilter);

//         Product p4=manager.createProduct(47,"Rice", BigDecimal.valueOf(60.0), Rating.ONE_STAR,LocalDate.of(2024, Month.MARCH, 15));
//         manager.reviewProduct(47, Rating.THREE_STAR, "YOu deserve only two rating");
//         manager.printProductReport(p4);
//        Product[] products = {
//            new Product(),
//            new Food(45,"Tea",BigDecimal.valueOf(90.9),LocalDate.of(2023,07,27)),
//            new Drink(45,"Tea",BigDecimal.valueOf(45.9),Rating.FOUR_STAR),
//            new Product(45,"Tea",BigDecimal.valueOf(55.0),Rating.FIVE_STAR),
//            new Product(45,"Tea",BigDecimal.valueOf(46.2),Rating.ONE_STAR)
//        };
//
//        
//        for (Product product : products) {
//            if(product instanceof Food ){
//            System.out.println(((Food) (product)).getBestBefore().toString());
//        }else{
//            System.out.println(product + " Discount: " + product.getDiscount());
//            }
//        }
//        
//     System.out.println(products[1].equals(products[2]));
    }
    
}
