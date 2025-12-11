package shared;

import java.io.Serializable;

public class Protocol {
    
    public enum MessageType {
        // Аутентификация
        LOGIN_REQUEST,
        LOGIN_RESPONSE,
        LOGOUT,
        REGISTER_REQUEST,
        REGISTER_RESPONSE,
        
        // Продукты
        GET_PRODUCTS,
        ADD_PRODUCT,
        UPDATE_PRODUCT,
        DELETE_PRODUCT,
        
        // Заказы
        GET_ORDERS,
        ADD_ORDER,
        UPDATE_ORDER,
        DELETE_ORDER,
        UPDATE_ORDER_STATUS,
        
        // Сырье
        GET_RAW_MATERIALS,
        ADD_RAW_MATERIAL,
        UPDATE_RAW_MATERIAL,
        DELETE_RAW_MATERIAL,
        
        // Системные
        PING,
        ERROR
    }
    
    public enum UserRole {
        ADMIN("Администратор"),
        MANAGER("Менеджер"),
        TECHNOLOGIST("Технолог"),
        WAREHOUSE_MANAGER("Кладовщик"),
        CUSTOMER("Покупатель");
        
        private final String displayName;
        
        UserRole(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    public static class Message implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private MessageType type;
        private Object data;
        private boolean success;
        private String errorMessage;
        
        public Message() {}
        
        public Message(MessageType type, Object data) {
            this.type = type;
            this.data = data;
            this.success = true;
        }
        
        public Message(MessageType type, Object data, boolean success, String errorMessage) {
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
    }
}