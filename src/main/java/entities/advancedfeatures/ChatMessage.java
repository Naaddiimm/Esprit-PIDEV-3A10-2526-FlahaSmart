package entities.advancedfeatures;

import java.time.LocalDateTime;

public class ChatMessage {
    private String message;
    private boolean isBot;
    private LocalDateTime timestamp;

    public ChatMessage(String message, boolean isBot) {
        this.message = message;
        this.isBot = isBot;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isBot() {
        return isBot;
    }

    public void setBot(boolean bot) {
        isBot = bot;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        return String.format("%02d:%02d", timestamp.getHour(), timestamp.getMinute());
    }
}

