package shared;

import java.io.Serializable;

public class Protocol {
    
    public enum MessageType {
        // Аутентификация
        LOGIN_REQUEST,
        LOGIN_RESPONSE,
        LOGOUT,
        
        // Продукты
        GET_PRODUCTS,
        GET_PRODUCT_BY_ID,
        ADD_PRODUCT,
        UPDATE_PRODUCT,
        DELETE_PRODUCT,
        
        // Заказы
        GET_ORDERS,
        GET_ORDER_BY_ID,
        ADD_ORDER,
        UPDATE_ORDER,
        DELETE_ORDER,
        UPDATE_ORDER_STATUS,
        
        // Сырье
        GET_RAW_MATERIALS,
        ADD_RAW_MATERIAL,
        UPDATE_RAW_MATERIAL,
        DELETE_RAW_MATERIAL,
        
        // Рецепты
        GET_RECIPES,
        ADD_RECIPE,
        UPDATE_RECIPE,
        DELETE_RECIPE,
        
        // Отчеты
        GENERATE_REPORT,
        
        // Команды
        EXECUTE_COMMAND,
        BACKUP_DATABASE,
        EXPORT_DATA,
        
        // Системные
        PING,
        ERROR,
        INFO,
        NOTIFICATION
    }
    
    public enum UserRole {
        ADMIN("Администратор"),
        MANAGER("Менеджер"),
        TECHNOLOGIST("Технолог"),
        WAREHOUSE_MANAGER("Кладовщик"),
        GUEST("Гость");
        
        private final String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static UserRole fromString(String text) {
            for (UserRole role : UserRole.values()) {
                if (role.name().equalsIgnoreCase(text) || role.displayName.equalsIgnoreCase(text)) {
                    return role;
                }
            }
            return GUEST;
        }
    }
    
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private MessageType type;
        private Object data;
        private boolean success;
        private String errorMessage;
        private String timestamp;
        
        public Message() {
            this.timestamp = new java.util.Date().toString();
        }
        
        public Message(MessageType type, Object data) {
            this();
            this.type = type;
            this.data = data;
            this.success = true;
        }
        
        public Message(MessageType type, Object data, boolean success, String errorMessage) {
            this();
            this.type = type;
            this.data = data;
            this.success = success;
            this.errorMessage = errorMessage;
        }
        
        // Геттеры и сеттеры
        public MessageType getType() { return type; }
        public void setType(MessageType type) { this.type = type; }
        
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        @Override
        public String toString() {
            return "Message{" +
                    "type=" + type +
                    ", success=" + success +
                    ", timestamp='" + timestamp + '\'' +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}