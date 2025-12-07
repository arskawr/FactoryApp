package shared.models;

import java.io.Serializable;
import java.util.Date;

public class RawMaterial implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private String category;
    private String unit;
    private double currentStock;
    private double minStock;
    private double maxStock;
    private double lastPurchasePrice;
    private Date lastDeliveryDate;
    private String supplier;
    private String storageLocation;
    private String qualityGrade; // A, B, C
    private Date expirationDate;
    private String barcode;
    private String notes;
    
    // Конструкторы
    public RawMaterial() {
        this.lastDeliveryDate = new Date();
        this.qualityGrade = "A";
        this.currentStock = 0;
        this.minStock = 10;
        this.maxStock = 100;
    }
    
    public RawMaterial(String name, String category, String unit, double currentStock, double minStock) {
        this();
        this.name = name;
        this.category = category;
        this.unit = unit;
        this.currentStock = currentStock;
        this.minStock = minStock;
    }
    
    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public double getCurrentStock() { return currentStock; }
    public void setCurrentStock(double currentStock) { this.currentStock = currentStock; }
    
    public double getMinStock() { return minStock; }
    public void setMinStock(double minStock) { this.minStock = minStock; }
    
    public double getMaxStock() { return maxStock; }
    public void setMaxStock(double maxStock) { this.maxStock = maxStock; }
    
    public double getLastPurchasePrice() { return lastPurchasePrice; }
    public void setLastPurchasePrice(double lastPurchasePrice) { this.lastPurchasePrice = lastPurchasePrice; }
    
    public Date getLastDeliveryDate() { return lastDeliveryDate; }
    public void setLastDeliveryDate(Date lastDeliveryDate) { this.lastDeliveryDate = lastDeliveryDate; }
    
    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }
    
    public String getStorageLocation() { return storageLocation; }
    public void setStorageLocation(String storageLocation) { this.storageLocation = storageLocation; }
    
    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
    
    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    // Методы бизнес-логики
    public boolean isLowStock() {
        return currentStock <= minStock;
    }
    
    public boolean isOutOfStock() {
        return currentStock <= 0;
    }
    
    public boolean isExpired() {
        if (expirationDate == null) return false;
        return expirationDate.before(new Date());
    }
    
    public double getStockPercentage() {
        if (maxStock <= 0) return 0;
        return (currentStock / maxStock) * 100;
    }
    
    public String getStockStatus() {
        if (isOutOfStock()) return "Нет в наличии";
        if (isLowStock()) return "Требует пополнения";
        if (getStockPercentage() >= 90) return "Переизбыток";
        return "Норма";
    }
    
    public void addStock(double quantity) {
        if (quantity > 0) {
            this.currentStock += quantity;
            this.lastDeliveryDate = new Date();
        }
    }
    
    public boolean useStock(double quantity) {
        if (quantity <= 0 || quantity > currentStock) {
            return false;
        }
        this.currentStock -= quantity;
        return true;
    }
    
    @Override
    public String toString() {
        return name + " (" + currentStock + " " + unit + ", статус: " + getStockStatus() + ")";
    }
}