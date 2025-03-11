package com.cline.core.model;

/**
 * Enum representing the role of a message in a conversation.
 */
public enum MessageRole {
    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system"),
    TOOL("tool");

    private final String value;

    MessageRole(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MessageRole fromString(String value) {
        for (MessageRole role : MessageRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown message role: " + value);
    }
}