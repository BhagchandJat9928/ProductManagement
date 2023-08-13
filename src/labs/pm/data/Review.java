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

/**
 *
 * @author bhagc
 */
public class Review implements Comparable<Review>, Serializable {

    private String comments;
    private Rating rating;

    public Review(Rating rating, String comments) {
        this.rating = rating;
        this.comments = comments;
    }

    public String getComments() {
        return comments;
    }

    public Rating getRating() {
        return rating;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setRating(Rating rating) {
        this.rating = rating;
    }

    @Override
    public int compareTo(Review other) {
        return other.rating.ordinal() - this.rating.ordinal();
    }

    @Override
    public String toString() {
        return "Review{" + "comments=" + comments + ", rating=" + rating + '}';
    }

}
