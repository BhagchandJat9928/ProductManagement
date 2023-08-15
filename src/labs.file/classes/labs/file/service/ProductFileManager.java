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
package labs.file.service;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import labs.pm.data.Drink;
import labs.pm.data.Food;
import labs.pm.data.Product;
import labs.pm.data.Rateable;
import labs.pm.data.Rating;
import labs.pm.data.Review;
import labs.pm.service.ProductManager;
import labs.pm.service.ProductManagerException;

/**
 *
 * @author bhagc
 */
public class ProductFileManager implements ProductManager {

    private Map<Product, List<Review>> products;

    private final ResourceBundle config = ResourceBundle.getBundle("labs.file.service.config");
    private final MessageFormat reviewFormat = new MessageFormat(config.getString("review.data.format"));
    private final MessageFormat productFormat = new MessageFormat(config.getString("product.data.format"));
    private final Path dataFolder = Path.of(config.getString("data.folder"));
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = readWriteLock.readLock();
    private final Lock writeLock = readWriteLock.writeLock();
    private final Charset charset = Charset.forName("UTF-8");
    private static final Logger logger = Logger.getLogger(ProductFileManager.class.getName());

    public ProductFileManager() {
        loadAllData();
    }

    @Override
    public Product createProduct(int id, String name, BigDecimal price, Rating rating) throws ProductManagerException {
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

    @Override
    public Product createProduct(int id, String name, BigDecimal price, Rating rating, LocalDate bestBefore) throws ProductManagerException {
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

    @Override
    public Product reviewProduct(int id, Rating rating, String comments) throws ProductManagerException {
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

    @Override
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

    @Override
    public List<Product> findProducts(Predicate<Product> filter) throws ProductManagerException {
        return products.keySet().stream()
                .filter(filter)
                .collect(Collectors.toList());

    }

    @Override
    public List<Review> findReviews(int id) throws ProductManagerException {
        List<Review> reviews = null;
        Path file = dataFolder.resolve(
                MessageFormat.format(config.getString("reviews.data.file"),
                        id));
        if (Files.notExists(file)) {
            reviews = new ArrayList<>();

        } else {
            try {
                reviews = Files.lines(file, charset)
                        .map(text -> parseReview(text)).filter(review -> review != null)
                        .collect(Collectors.toList());
            } catch (IOException ex) {
                logger.log(Level.INFO, ex.getMessage());
            }
        }
        return reviews;
    }

    @Override
    public Map<Rating, BigDecimal> getDiscounts() throws ProductManagerException {
        try {
            readLock.lock();
            return products.keySet().stream().collect(
                    Collectors.groupingBy(p -> p.getRating(),
                            Collectors.collectingAndThen(
                                    Collectors.summingDouble(p -> p.getDiscount().doubleValue()),
                                    discount -> BigDecimal.valueOf(discount))));
        } finally {
            readLock.unlock();
        }
    }

    private void addProductToFile(Product product) {
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
                        charset)
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

    private void addReviewToFile(int id, Review review) {
        if (Files.exists(dataFolder)) {
            Path reviewFile = dataFolder.resolve(
                    MessageFormat.format(config.getString("reviews.data.file"), id));
            try (PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(reviewFile,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.APPEND),
                            charset)
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
                            charset)
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
                reviews = Files.lines(file, charset)
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
                    product = new Drink(id, name, price, rating);
                case "F" -> {
                    LocalDate bestBefore = LocalDate.parse((String) values[5]);
                    product = new Food(id, name, price, rating, bestBefore);
                }
            }
        } catch (ParseException | NumberFormatException | DateTimeParseException ex) {
            logger.log(Level.WARNING, "Error in parsing Product {0}", text);
        }
        return product;
    }

}
