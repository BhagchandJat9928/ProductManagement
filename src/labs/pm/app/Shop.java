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

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import labs.pm.data.Product;
import labs.pm.data.ProductManager;
import labs.pm.data.Rating;
import labs.pm.data.Review;

/**
 *
 * @author bhagc
 */
public class Shop {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        ProductManager pm = new ProductManager(Locale.US);
        pm.changeLocale("en-IN");
        pm.printProductReport(101);
        pm.createProduct(01, "Tea", BigDecimal.valueOf(10.7), Rating.TWO_STAR);
        pm.reviewProduct(01, Rating.TWO_STAR, "Looks like Teea but is it");
        pm.reviewProduct(01, Rating.FOUR_STAR, "Fine Tea");
        pm.reviewProduct(01, Rating.THREE_STAR, "This is not a Tea");
        pm.reviewProduct(01, Rating.FIVE_STAR, "Perfect");
        pm.printProductReport(101);
        pm.dumpData();
        Map<Product, List<Review>> list = pm.restoreData();
        list.forEach((key, value) -> System.out.println(key + " " + value));

    }

}
