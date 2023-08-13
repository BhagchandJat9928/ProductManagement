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

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 *
 * @author bhagc
 */
public class Food extends Product {
   private final LocalDate bestBefore;
   
   public Food(LocalDate bestBefore) {
        super();
        this.bestBefore = bestBefore;
    }

   public Food(int id, String name, BigDecimal price, Rating rating,LocalDate bestBefore ) {
        super(id, name, price, rating);
        this.bestBefore = bestBefore;
    }

   public Food( int id, String name, BigDecimal price,LocalDate bestBefore) {
        super(id, name, price);
        this.bestBefore = bestBefore;
    }
    @Override
    public Product applyRating(Rating rating) {
        return new Food(this.getId(),this.getName(),this.getPrice(),rating,this.bestBefore); 
    }

    @Override
    public LocalDate getBestBefore() {
        return this.bestBefore; 
    }
    
    

    @Override
    public String toString() {
        return super.toString()+ " BestBefore: "+this.getBestBefore(); 
    }

    
    
    
    
    
}
