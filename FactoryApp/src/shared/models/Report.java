package shared.models;

import java.io.Serializable;
import java.util.Date;

public class Report implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum ReportType {
        SALES("Отчет по продажам"),
        PRODUCTION("Отчет по производству"),
        INVENTORY("Отчет по остаткам"),
        FINANCIAL("Финансовый отчет"),
        ORDERS("Отчет по заказам"),
        PRODUCTS("Отчет по продукции"),
        RAW_MATERIALS("Отчет по сырью"),
        EMPLOYEE("Отчет по сотрудникам");
        
        private final String displayName;
        
        ReportType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private int id;
    private ReportType type;
    private Date startDate;
    private Date endDate;
    private Date generationDate;
    private String generatedBy;
    private String data; // JSON или другой формат с данными отчета
    private String summary;
    
    // Конструкторы
    public Report() {
        this.generationDate = new Date();
    }
    
    public Report(ReportType type, Date startDate, Date endDate, String generatedBy) {
        this();
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.generatedBy = generatedBy;
    }
    
    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public ReportType getType() { return type; }
    public void setType(ReportType type) { this.type = type; }
    
    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    
    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    
    public Date getGenerationDate() { return generationDate; }
    public void setGenerationDate(Date generationDate) { this.generationDate = generationDate; }
    
    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    @Override
    public String toString() {
        return type.getDisplayName() + " (" + startDate + " - " + endDate + ")";
    }
}