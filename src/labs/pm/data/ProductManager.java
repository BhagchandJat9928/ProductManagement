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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author bhagc
 */
public class ProductManager {

    private static final Logger logger = Logger.getLogger(ProductManager.class.getName());
    private final ResourceBundle config = ResourceBundle.getBundle("labs.pm.data.config");
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final Path reportsFolder = Path.of(config.getString("reports.folder"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final Path tempFolder = Path.of(config.getString("temp.folder"));
//    private ResourceFormatter formatter;
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private Map<Product, List<Review>> products;
    private static final Map<String, ResourceFormatter> formatters
            = Map.of("en-US", new ResourceFormatter(Locale.US),
                    "en-GB", new ResourceFormatter(Locale.UK),
                    "fr-FR", new ResourceFormatter(Locale.FRANCE),
                    "zh-CH", new ResourceFormatter(Locale.CHINA),
                    "ru-RU", new ResourceFormatter(Locale.forLanguageTag("ru_RU")),
                    "en-IN", new ResourceFormatter(Locale.forLanguageTag("en_IN")));

    public static final ProductManager pm = new ProductManager();

    private ProductManager() {
        // this(locale.toLanguageTag());
        products = new HashMap<>();
        loadAllData();
    }

    public static ProductManager getInstance() {
        return pm;
    }

//    public void changeLocale(String languageTag) {
//        this.formatter = formatters.getOrDefault(languageTag, formatters.get("en-IN"));
//    }
    public static Set<String> getSupportedLocale() {
        return formatters.keySet();
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Food(id, name, price, rating, bestBefore);
            if (!this.products.containsKey(product)) {
                addProductToFile(product);
            }
            this.products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error in adding Product", ex.getMessage());
            return null;
        } finally {
            writeLock.unlock();
        }
        return product;
    }

    public Product createProduct(int id, String name, BigDecimal price, Rating rating) {
        Product product = null;
        try {
            writeLock.lock();
            product = new Drink(id, name, price, rating);
            if (!this.products.containsKey(product)) {
                addProductToFile(product);
            }
            this.products.putIfAbsent(product, new ArrayList<>());
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error in adding Product", ex.getMessage());
            return null;
        } finally {
            writeLock.unlock();
        }
        return product;
    }

    public void addProductToFile(Product product) {
        if (Files.notExists(dataFolder)) {
            try {
                Files.createDirectories(dataFolder);
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
            }
        }
        Path productFile = dataFolder.resolve(
                MessageFormat.format(config.getString("product.data.file"),
                        product.getId()));
        try (PrintWriter out = new PrintWriter(
                new OutputStreamWriter(
                        Files.newOutputStream(productFile,
                                StandardOpenOption.CREATE),
                        Charset.forName("UTF-8"))
        )) {
            writeLock.lock();
            String type = product instanceof Food ? "F," : "D,";
            out.write(type
                    + product.getId()
                    + "," + product.getName()
                    + "," + product.getPrice().toString()
                    + "," + product.getRating().ordinal() + "," + product.getBestBefore());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    public void addReviewToFile(int id, Review review) {
        if (Files.exists(dataFolder)) {

            Path reviewFile = dataFolder.resolve(
                    MessageFormat.format(config.getString("reviews.data.file"),
                            id));
            try (PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(reviewFile,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND),
                            Charset.forName("UTF-8"))
            )) {
                writeLock.lock();
                out.append(review.getRating().ordinal() + "," + review.getComments())
                        .append("\n");
            } catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage());
            } finally {
                writeLock.unlock();
            }
        }
    }

    public Map<String, String> getDiscounts(String languageTag) {
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-IN"));
            return products.keySet().stream().collect(
                    Collectors.groupingBy(p -> p.getRating().getStars(),
                            Collectors.collectingAndThen(
                                    Collectors.summingDouble(p -> p.getDiscount().doubleValue()),
                                    discount -> formatter.currencyFormat.format(discount))));
        } finally {
            readLock.unlock();
        }
    }

    public Product reviewProduct(int id, Rating rating, String comments) {
        try {
            writeLock.lock();
            return reviewProduct(findProduct(id), rating, comments);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } finally {
            writeLock.unlock();
        }
        return null;
    }

    private Product reviewProduct(Product product, Rating rating, String comments) {

        List<Review> reviews = this.products.get(product);
        this.products.remove(product);
        reviews.add(new Review(rating, comments));
        addReviewToFile(product.getId(), new Review(rating, comments));

        product = product.applyRating(Rateable
                .convert((int) Math.round(
                        reviews.stream()
                                .mapToInt(r -> r.getRating().ordinal())
                                .average().orElse(0))));

        this.products.put(product, reviews);

        return product;

    }

    public Product findProduct(int id) throws ProductManagerException {
        try {
            readLock.lock();
            return products.keySet()
                    .stream()
                    .filter(p -> p.getId() == id)
                    .findFirst()
                    .orElseThrow(() -> new ProductManagerException("Product with this id: " + id + " is not Found"));
        } finally {
            readLock.unlock();
        }
    }

    public void printProductReport(int id, String languageTag, String client) {

        try {
            readLock.lock();
            printProductReport(findProduct(id), languageTag, client);
        } catch (ProductManagerException ex) {
            logger.log(Level.INFO, ex.getMessage());
        } finally {
            readLock.unlock();
        }
    }

    private void printProductReport(Product product, String languageTag, String client) {
        ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-IN"));
        if (!Files.exists(reportsFolder)) {
            try {
                Files.createDirectories(reportsFolder);
            } catch (IOException ex) {
                logger.log(Level.INFO, ex.getMessage());
            }
        }
        Path productFile = reportsFolder.resolve(
                MessageFormat.format(config.getString("report.file"),
                        product.getId(), client));
//        StringBuilder sb=new StringBuilder();
        try (PrintWriter out = new PrintWriter(new OutputStreamWriter(
                Files.newOutputStream(productFile, StandardOpenOption.CREATE),
                Charset.forName("UTF-8")))) {
 
            if (products.containsKey(product)) {
                product = findProduct(product.getId());
                out.append(formatter.formatProduct(product))
                        .append(System.lineSeparator());
                //   sb.append(formatter.formatProduct(product)).append("\n");
                List<Review> reviews = this.products.get(product);
                Collections.sort(reviews);
                if (reviews.isEmpty()) {
                    //      sb.append(formatter.getText("no.reviews")).append("\n");
                    out.append(formatter.getText("no.reviews"))
                            .append(System.lineSeparator());
                } else {
//            for (Review review : reviews) {
//                sb.append(formatter.formatReview(review)).append("\n");
//            }
                    reviews.stream()
                            .forEach(r
                                    -> //  sb.append(formatter.formatReview(r)).append("\n");
                                    out.append(formatter.formatReview(r))
                                    .append(System.lineSeparator()));
                }
            } else {
                out.append(formatter.getText("no.product"))
                        .append(System.lineSeparator());
//                sb.append(formatter.getText("no.product"))
//                        .append("\n");
            }
        } catch (IOException | ProductManagerException ex) {
            logger.log(Level.WARNING, "Error printing report to File{0}", ex.getMessage());
        }
    }

    public synchronized void printProducts(Predicate<Product> filter, Comparator<Product> sorter, String languageTag) {
        //List<Product> list = new ArrayList<>(products.keySet());
        //list.sort(sorter);
        try {
            readLock.lock();
            ResourceFormatter formatter = formatters.getOrDefault(languageTag, formatters.get("en-IN"));
            StringBuilder sb = new StringBuilder();
            if (products.isEmpty()) {
                sb.append(formatter.getText("no.product")).append("\n");
            } else {
                products.keySet().stream()
                        .sorted(sorter)
                        .filter(filter)
                        .forEach(p -> sb.append(formatter.formatProduct(p))
                        .append("\n"));
            }
            System.out.println(sb);
        } finally {
            readLock.unlock();
        }
    }

    public Map<Product, List<Review>> restoreData() {
        try {
            Path tempFile = Files.list(tempFolder)
                    .filter(path -> path.getFileName().toString().endsWith(".tmp"))
                    .findFirst().orElseThrow();
            try (ObjectInputStream in = new ObjectInputStream(
                    Files.newInputStream(tempFile, StandardOpenOption.DELETE_ON_CLOSE))) {
                readLock.lock();
                products = (HashMap) in.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        } finally {
            readLock.unlock();
        }
        return products;
    }

    public void dumpData() {
        try {
            if (Files.notExists(tempFolder)) {
                Files.createDirectories(tempFolder);
            }

            Path tempFile = tempFolder.resolve(
                    MessageFormat.format(config.getString("temp.file"), "TEMP"
                            + Math.random()));
            try (ObjectOutputStream out = new ObjectOutputStream(
                    Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
                writeLock.lock();
                out.writeObject(products);
                products = new HashMap<>();
            }
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    private void loadAllData() {
        try {

            products = Files.list(dataFolder)
                    .filter(path -> path.getFileName().toString().contains("product"))
                    .map(path -> loadProduct(path))
                    .filter(product -> product != null)
                    .collect(Collectors.toMap(product -> product,
                            product -> loadReviews(product)));

        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
    }

    private Product loadProduct(Path file) {
        Product product = null;
        try {
            product = parseProduct(
                    Files.lines(dataFolder.resolve(file),
                            Charset.forName("UTF-8"))
                            //    .forEach(p->System.out.println(p));
                            .findFirst().orElseThrow());
        } catch (IOException ex) {
            logger.log(Level.WARNING, ex.getMessage());
        }
        return product;
    }

    private List<Review> loadReviews(Product product) {
        List<Review> reviews = null;
        Path file = dataFolder.resolve(
                MessageFormat.format(config.getString("reviews.data.file"),
                        product.getId()));
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();

        } else {
            try {
                reviews = Files.lines(file, Charset.forName("UTF-8"))
                        .map(text -> parseReview(text)).filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.INFO, ex.getMessage());
            }
        }
        return reviews;
    }

    private Review parseReview(String text) {
        Review review = null;
        try {
            Object[] values = reviewFormat.parse(text);

            review = new Review(Rateable.convert(Integer.parseInt((String) values[0])),
                    (String) values[1]);
//            reviewProduct(Integer.parseInt((String) values[0]),
//                    Rateable.convert(Integer.parseInt((String) values[1])),
//                    (String) values[2]);
        } catch (ParseException ex) {
            logger.log(Level.WARNING, "Error in parsing Review " + text, ex.getMessage());
        } catch (NumberFormatException ex) {
            logger.log(Level.WARNING, "Error in parsing Wrong Type Data " + text, ex.getMessage());
        }
        return review;
    }

    private Product parseProduct(String text) {
        Product product = null;
        try {
            Object[] values = productFormat.parse(text);
            int id = Integer.parseInt((String) values[1]);
            String name = (String) values[2];
            BigDecimal price = new BigDecimal((String) values[3]);
            Rating rating = Rating.values()[Integer.parseInt((String) values[4])];
            switch ((String) values[0]) {
                case "D" ->
                    // createProduct(id, name, price, rating);
                    product = new Drink(id, name, price, rating);
                case "F" -> {
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    //  createProduct(id, name, price, rating, bestBefore);
                    product = new Food(id, name, price, rating, bestBefore);
                }
            }

        } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error in parsing Product {0}", text);
        }
        return product;
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
            // this.currencyFormat.setCurrency(Currency.getInstance(locale));
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
