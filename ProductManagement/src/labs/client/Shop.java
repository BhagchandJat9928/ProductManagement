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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.util.logging.Level;
import java.util.logging.Logger;
import labs.file.service.ProductFileManager;
import labs.pm.data.Product;
import labs.pm.data.Rating;
import labs.pm.service.ProductManager;
import labs.pm.service.ProductManagerException;

/**
 *
 * @author bhagc
 */
@BusinessPolicies({@BusinessPolicy(countries="India",value="hello")})
public class Shop {

   private static final Logger logger=Logger.getLogger(Shop.class.getName());
    public static void main(String[] args) {
        try {
            ResourceFormatter formatter=ResourceFormatter.getResourceFormatter("fr-FR");
            ProductManager pm=new ProductFileManager();
            pm.createProduct(6, "Churma", BigDecimal.valueOf(50.0), Rating.NO_STAR);
//       pm.reviewProduct(6, Rating.FIVE_STAR, "Perfect");
//       pm.reviewProduct(6, Rating.FOUR_STAR, "Extremely Tasty");
//       pm.reviewProduct(6, Rating.THREE_STAR, "Looks like tea but is it?");
//       pm.reviewProduct(6, Rating.TWO_STAR, "Fine Tea");
//       pm.reviewProduct(6, Rating.THREE_STAR, "Good Tea");

       pm.findProducts(p->p.getPrice().doubleValue()>2)
               .stream().forEach(p->System.out.println(formatter.formatProduct(p)));
       Product product=pm.findProduct(4);
       System.out.println(formatter.formatProduct(product));
       pm.findReviews(6).forEach(r->System.out.println(formatter.formatReview(r)));

       
        } catch (ProductManagerException ex) {
           logger.log(Level.WARNING, ex.getMessage(), ex);
        }

    }
    
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface BusinessPolicies{
     BusinessPolicy[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(BusinessPolicies.class)
@interface BusinessPolicy{
     String name() default "No Policy";
     String[] countries();
     String value();
}


