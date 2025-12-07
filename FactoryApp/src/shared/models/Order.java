package shared.models;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String clientName;
    private String clientPhone;
    private String clientAddress;
    private Date orderDate;
    private Date deliveryDate;
    private String status; // НОВЫЙ, В_ПРОИЗВОДСТВЕ, ГОТОВ, ОТГРУЖЕН, ВЫПОЛНЕН, ОТМЕНЕН
    private double totalAmount;
    private String notes;
    private List<OrderItem> items;
    
    public enum OrderStatus {
        NEW("Новый"),
        IN_PRODUCTION("В производстве"),
        READY("Готов"),
        SHIPPED("Отгружен"),
        COMPLETED("Выполнен"),
        CANCELLED("Отменен");
        
        private final String displayName;
        
        OrderStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public static class OrderItem implements Serializable {
        private int productId;
        private String productName;
        private int quantity;
        private double price;
        private double total;
        
        public OrderItem() {}
        
        public OrderItem(int productId, String productName, int quantity, double price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
            this.total = quantity * price;
        }
        
        // Геттеры и сеттеры
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { 
            this.quantity = quantity; 
            this.total = this.quantity * this.price;
        }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { 
            this.price = price; 
            this.total = this.quantity * this.price;
        }
        
        public double getTotal() { return total; }
        
        @Override
        public String toString() {
            return productName + " x" + quantity + " = " + total + " руб.";
        }
    }
    
    // Конструкторы
    public Order() {
        this.orderDate = new Date();
        this.status = OrderStatus.NEW.getDisplayName();
    }
    
    public Order(String clientName, String clientPhone, String clientAddress) {
        this();
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.clientAddress = clientAddress;
    }
    
    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getClientPhone() { return clientPhone; }
    public void setClientPhone(String clientPhone) { this.clientPhone = clientPhone; }
    
    public String getClientAddress() { return clientAddress; }
    public void setClientAddress(String clientAddress) { this.clientAddress = clientAddress; }
    
    public Date getOrderDate() { return orderDate; }
    public void setOrderDate(Date orderDate) { this.orderDate = orderDate; }
    
    public Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Date deliveryDate) { this.deliveryDate = deliveryDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public double getTotalAmount() { 
        if (items != null && !items.isEmpty()) {
            return items.stream().mapToDouble(OrderItem::getTotal).sum();
        }
        return totalAmount; 
    }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    @Override
    public String toString() {
        return "Заказ #" + id + " от " + clientName + " (" + status + ")";
    }
}