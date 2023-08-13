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

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import labs.pm.data.Product;
import labs.pm.data.Review;

/**
 *
 * @author bhagc
 */
public class ResourceFormatter {

    private final Locale locale;
    private final ResourceBundle resource;
    private final NumberFormat currencyFormat;
    private final DateTimeFormatter dateFormat;
    private static ResourceFormatter resourceFormatter;

    private ResourceFormatter(Locale locale) {
        this.locale = locale;
        this.resource = ResourceBundle.getBundle("labs.client.resources");
        this.currencyFormat = NumberFormat.getCurrencyInstance(this.locale);
        this.dateFormat = DateTimeFormatter.ofPattern("YYYY MM dd", locale);
    }

    public static ResourceFormatter getResourceFormatter(String languageTag) {
        resourceFormatter = formatters.getOrDefault(languageTag,
                formatters.get("en-US"));
        return resourceFormatter;
    }

    private static final Map<String, ResourceFormatter> formatters
            = Map.of("en-US", new ResourceFormatter(Locale.US),
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CH", new ResourceFormatter(Locale.CHINA),
                    "ru-RU", new ResourceFormatter(Locale.forLanguageTag("ru_RU")),
                    "en-IN", new ResourceFormatter(Locale.forLanguageTag("en_IN")));

    public static Set<String> getSupportedLocale() {
        return formatters.keySet();
    }

    public String formatProduct(Product product) {
        return MessageFormat.format(this.resource.getString("product"),
                product.getId(), product.getName(),
                currencyFormat.format(product.getPrice()),
                product.getRating().getStars(),
                product.getBestBefore().format(dateFormat));
    }

    public String formatReview(Review review) {
        return MessageFormat.format(this.resource.getString("review"),
                review.getRating().getStars(), review.getComments());
    }

    public String getText(String key) {
        return resource.getString(key);
    }
}
