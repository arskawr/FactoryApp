package shared.models;

import java.io.Serializable;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private String category;
    private double price;
    private double weight;
    private int stockQuantity;
    private String description;
    
    // Конструкторы
    public Product() {}
    
    public Product(String name, String category, double price, double weight) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.stockQuantity = 0;
    }
    
    public Product(int id, String name, String category, double price, double weight, int stockQuantity, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.stockQuantity = stockQuantity;
        this.description = description;
    }
    
// Добавьте в класс Product (после существующих конструкторов):
    public Product(int id, String name, String category, double price, double weight, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.stockQuantity = stockQuantity;
        this.description = "";
}

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }
    
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", weight=" + weight +
                ", stock=" + stockQuantity +
                '}';
    }
}