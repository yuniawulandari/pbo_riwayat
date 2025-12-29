package amodels;

public enum OrderStatus {
    PENDING,
    SUCCESS;

    public static OrderStatus fromString(String text) {
        if (text == null) {
            return null;
        }
        for (OrderStatus status : OrderStatus.values()) {
            if (status.name().equalsIgnoreCase(text)) {
                return status;
            }
        }
        return PENDING; 
    }
}