package shared.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private int id;
    private String name;
    private String description;
    private int productId;
    private String productName;
    private double outputQuantity;
    private String unit;
    private List<RecipeIngredient> ingredients;
    private double productionTime; // Время производства в часах
    private String difficulty; // Сложность: легкая, средняя, сложная
    
    public static class RecipeIngredient implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private int rawMaterialId;
        private String rawMaterialName;
        private double quantity;
        private String unit;
        private String notes;
        
        public RecipeIngredient() {}
        
        public RecipeIngredient(int rawMaterialId, String rawMaterialName, double quantity, String unit) {
            this.rawMaterialId = rawMaterialId;
            this.rawMaterialName = rawMaterialName;
            this.quantity = quantity;
            this.unit = unit;
        }
        
        // Геттеры и сеттеры
        public int getRawMaterialId() { return rawMaterialId; }
        public void setRawMaterialId(int rawMaterialId) { this.rawMaterialId = rawMaterialId; }
        
        public String getRawMaterialName() { return rawMaterialName; }
        public void setRawMaterialName(String rawMaterialName) { this.rawMaterialName = rawMaterialName; }
        
        public double getQuantity() { return quantity; }
        public void setQuantity(double quantity) { this.quantity = quantity; }
        
        public String getUnit() { return unit; }
        public void setUnit(String unit) { this.unit = unit; }
        
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
        
        @Override
        public String toString() {
            return rawMaterialName + ": " + quantity + " " + unit + (notes != null ? " (" + notes + ")" : "");
        }
    }
    
    // Конструкторы
    public Recipe() {
        this.ingredients = new ArrayList<>();
        this.difficulty = "Средняя";
    }
    
    public Recipe(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public double getOutputQuantity() { return outputQuantity; }
    public void setOutputQuantity(double outputQuantity) { this.outputQuantity = outputQuantity; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public List<RecipeIngredient> getIngredients() { return ingredients; }
    public void setIngredients(List<RecipeIngredient> ingredients) { this.ingredients = ingredients; }
    
    public double getProductionTime() { return productionTime; }
    public void setProductionTime(double productionTime) { this.productionTime = productionTime; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    // Методы
    public void addIngredient(RecipeIngredient ingredient) {
        if (ingredients == null) {
            ingredients = new ArrayList<>();
        }
        ingredients.add(ingredient);
    }
    
    public double calculateCost(List<RawMaterial> rawMaterials) {
        double totalCost = 0;
        for (RecipeIngredient ingredient : ingredients) {
            for (RawMaterial material : rawMaterials) {
                if (material.getId() == ingredient.getRawMaterialId()) {
                    totalCost += material.getLastPurchasePrice() * ingredient.getQuantity();
                    break;
                }
            }
        }
        return totalCost;
    }
    
    @Override
    public String toString() {
        return name + " (Выход: " + outputQuantity + " " + unit + ", Ингредиентов: " + 
               (ingredients != null ? ingredients.size() : 0) + ")";
    }
}