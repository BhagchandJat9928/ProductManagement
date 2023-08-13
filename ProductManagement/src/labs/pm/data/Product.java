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
package labs.pm.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import static labs.pm.data.Rating.*;

/**
 * {@code Product} class represents properties and behaviors of product objects
 * int the Product Management System.
 * <br>
 * Each product has an id, name and price
 * <br>
 * Each product can have a discount , calculated based on a
 * {@link  DISCOUNT_RATE discount rate}
 *
 * @version 4.0
 * @author bhagc
 */
public abstract class Product implements Rateable<Product>, Serializable {

    /**
     * A constant that defines a {@link java.math.BigDecimal} value of the
     * discount rate
     * <br>
     * Discount Rate is 60%
     */
    public static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(.06);
    private final int id;
    private final String name;
    private final BigDecimal price;
    private Rating rating;

    Product() {
        this(0, "NO Product", BigDecimal.ZERO);
    }

    Product(int id, String name, BigDecimal price, Rating rating) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.rating = rating;
    }

    Product(int id, String name, BigDecimal price) {
        this(id, name, price, NO_STAR);

    }

    public LocalDate getBestBefore() {
        return LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public Rating getRating() {
        return rating;
    }

    /**
     * Calculates discount based on a product price and
     * {@link DISCOUNT_RATE discount rate}
     *
     * @return a {@link java.math.BigDecimal BigDecimal} value of the discount
     */
    public BigDecimal getDiscount() {
        return price.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Product product) {
            return product.getId() == this.id;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Product{" + "id=" + id + ", name=" + name + ", price=" + price + ", rating=" + rating.getStars() + '}';
    }

}
