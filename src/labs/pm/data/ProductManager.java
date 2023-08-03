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
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bhagc
 */
public class ProductManager {

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private ResourceFormatter formatter;
    private Map<Product, List<Review>> products;
    private static final Map<String, ResourceFormatter> formatters
            = Map.of("en-US", new ResourceFormatter(Locale.US),
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CH", new ResourceFormatter(Locale.CHINA),
                    "ru-RU", new ResourceFormatter(new Locale("ru", "RU")),
                    "en-IN", new ResourceFormatter(new Locale("en", "IN")));

    public ProductManager(Locale locale) {
        this(locale.toLanguageTag());
    }

    public ProductManager(String languageTag) {
        products = new HashMap<>();
        this.formatter = formatters.getOrDefault(languageTag, formatters.get("en-IN"));
    }

    public void changeLocale(String languageTag) {
        this.formatter = formatters.getOrDefault(languageTag, formatters.get("en-IN"));
    }

    public static Set<String> getSupportedLocale() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {

        Product product = new Food(id, name, price, rating, bestBefore);
        this.products.putIfAbsent(product, new ArrayList<>());

        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {

        Product product = new Drink(id, name, price, rating);
        this.products.putIfAbsent(product, new ArrayList<>());

        return product;
    }

    public Map<String, String> getDiscounts() {
//       products.keySet().stream().collect(Collectors.groupingBy(p->p.getRating().getStars()),
//               Collectors.collectingAndThen(Collectors.summarizingDouble(),
//               discount->formatter.currencyFormat.format(discount)));
        return new HashMap<>();
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        }
        return null;
    }

    public Product reviewProduct(Product product, Rating rating, String comments) {

        List<Review> reviews = this.products.get(product);
        reviews.add(new Review(rating, comments));

//        for (Review review : reviews) {
//            sum += review.getRating().ordinal();
//        }
        product = product.applyRating(Rateable
                .convert((int) Math.round(
                        reviews.stream()
                                .mapToInt(r -> r.getRating().ordinal())
                                .average().orElse(0))));
        this.products.put(product, reviews);
        return product;

    }

    public Product findProduct(int id) throws ProductManagerException {

        //Product product = null;
//        for (Product pd : this.products.keySet()) {
//            if (pd.getId() == id) {
//                product = pd;
//                break;
//            }
//        }
        return products.keySet()
                .stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ProductManagerException("Product with this id: " + id + " is not Found"));
    }

    public void printProductReport(int id) {
        try {
            printProductReport(findProduct(id));
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        }
    }

    public void printProductReport(Product product) {
        StringBuilder sb = new StringBuilder();
        if (products.containsKey(product)) {

            try {
                product = findProduct(product.getId());

                sb.append(formatter.formatProduct(product));
                sb.append("\n");
                List<Review> reviews = this.products.get(product);
                Collections.sort(reviews);
                if (reviews.isEmpty()) {
                    sb.append(formatter.getText("no.reviews"));
                    sb.append("\n");
                } else {
//            for (Review review : reviews) {
//                sb.append(formatter.formatReview(review));
//                sb.append("\n");
//
//            }
                    reviews.stream()
                            .forEach(r -> sb.append(formatter.formatReview(r))
                            .append("\n"));
                }
            } catch (ProductManagerException ex) {
                logger.log(Level.INFO, ex.getMessage());
            }
        } else {
            sb.append(formatter.getText("error"))
                    .append("\n");

        }

        System.out.println(sb);

    }

    public void printProducts(Predicate<Product> filter, Comparator<Product> sorter) {
        //List<Product> list = new ArrayList<>(products.keySet());
        //list.sort(sorter);
        StringBuilder sb = new StringBuilder();
        if (products.isEmpty()) {
            sb.append(formatter.getText("error")).append("\n");
        } else {
            products.keySet().stream()
                    .sorted(sorter)
                    .filter(filter)
                    .forEach(p -> sb.append(formatter.formatProduct(p))
                    .append("\n"));
//         for(Product product : list){
//             sb.append(formatter.formatProduct(product));
//            sb.append("\n");
//         }
        }
        System.out.println(sb);
    }

    public void parseReview(String text) {
        try {
            Object[] values = reviewFormat.parse(text);
            reviewProduct(Integer.parseInt((String) values[0]),
                    Rateable.convert(Integer.parseInt((String) values[1])),
                    (String) values[2]);
        } catch (ParseException ex) {
            logger.log(Level.WARNING, "Error in parsing Review "+text,ex.getMessage());
        }catch(NumberFormatException ex){
            logger.log(Level.WARNING, "Error in parsing Wrong Type Data "+text,ex.getMessage());
        }
    }
    
    public void parseProduct(String text) {
        try {
            Object[] values = productFormat.parse(text);
            int id=Integer.parseInt((String) values[1]);
            String name=(String)values[2];
            BigDecimal price=BigDecimal.valueOf(Double.parseDouble((String)values[3]));
            Rating rating=Rating.values()[Integer.parseInt((String)values[4])];
            switch((String)values[0]){
                case "D" -> createProduct(id,name,price,rating);
                case "F" -> {
                    LocalDate bestBefore=LocalDate.parse((String)values[5]);
                    createProduct(id,name,price,rating,bestBefore);
                }
            }
                    
        } catch (ParseException |NumberFormatException | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error in parsing Review {0}", text);
        }
    }

    private static class ResourceFormatter {

        private final Locale locale;
        private final ResourceBundle resource;
        private final NumberFormat currencyFormat;
        private final DateTimeFormatter dateFormat;

        public ResourceFormatter(Locale locale) {
            this.locale = locale;
            this.resource = ResourceBundle.getBundle("labs.pm.data.resources");
            this.currencyFormat = NumberFormat.getCurrencyInstance(this.locale);
            this.currencyFormat.setCurrency(Currency.getInstance(locale));
//            this.dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
//                    .localizedBy(locale);
            this.dateFormat = DateTimeFormatter.ofPattern("YYYY MM dd", locale);
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

}
