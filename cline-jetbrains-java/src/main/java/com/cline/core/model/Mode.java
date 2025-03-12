package com.cline.core.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a mode for the assistant.
 */
public class Mode {
    private final String id;
    private final String slug;
    private final String name;
    private final String roleDefinition;
    private final List<String> groups;
    private final String customInstructions;
    
    /**
     * Creates a new mode.
     *
     * @param id                The mode ID (generated if null)
     * @param slug              The mode slug
     * @param name              The mode name
     * @param roleDefinition    The mode role definition
     * @param groups            The mode groups
     * @param customInstructions The mode custom instructions
     */
    public Mode(@Nullable String id,
                @NotNull String slug,
                @NotNull String name,
                @NotNull String roleDefinition,
                @NotNull List<String> groups,
                @Nullable String customInstructions) {
        this.id = id != null ? id : UUID.randomUUID().toString();
        this.slug = slug;
        this.name = name;
        this.roleDefinition = roleDefinition;
        this.groups = new ArrayList<>(groups);
        this.customInstructions = customInstructions;
    }
    
    /**
     * Creates a mode from a JSON object.
     *
     * @param json The JSON object
     * @return The mode
     */
    public static Mode fromJson(JsonObject json) {
        String id = json.has("id") ? json.get("id").getAsString() : null;
        String slug = json.get("slug").getAsString();
        String name = json.get("name").getAsString();
        String roleDefinition = json.get("roleDefinition").getAsString();
        
        List<String> groups = new ArrayList<>();
        JsonArray groupsArray = json.getAsJsonArray("groups");
        for (int i = 0; i < groupsArray.size(); i++) {
            groups.add(groupsArray.get(i).getAsString());
        }
        
        String customInstructions = json.has("customInstructions") ? json.get("customInstructions").getAsString() : null;
        
        return new Mode(id, slug, name, roleDefinition, groups, customInstructions);
    }
    
    /**
     * Converts the mode to a JSON object.
     *
     * @return The JSON object
     */
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("slug", slug);
        json.addProperty("name", name);
        json.addProperty("roleDefinition", roleDefinition);
        
        JsonArray groupsArray = new JsonArray();
        for (String group : groups) {
            groupsArray.add(group);
        }
        json.add("groups", groupsArray);
        
        if (customInstructions != null) {
            json.addProperty("customInstructions", customInstructions);
        }
        
        return json;
    }
    
    /**
     * Gets the mode ID.
     *
     * @return The mode ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the mode slug.
     *
     * @return The mode slug
     */
    public String getSlug() {
        return slug;
    }
    
    /**
     * Gets the mode name.
     *
     * @return The mode name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the mode role definition.
     *
     * @return The mode role definition
     */
    public String getRoleDefinition() {
        return roleDefinition;
    }
    
    /**
     * Gets the mode groups.
     *
     * @return The mode groups
     */
    public List<String> getGroups() {
        return new ArrayList<>(groups);
    }
    
    /**
     * Gets the mode custom instructions.
     *
     * @return The mode custom instructions
     */
    @Nullable
    public String getCustomInstructions() {
        return customInstructions;
    }
    
    /**
     * Creates a code mode.
     *
     * @return The code mode
     */
    public static Mode createCodeMode() {
        return new Mode(
                "code",
                "code",
                "Code",
                "You are Roo, a highly skilled software engineer with extensive knowledge in many programming languages, frameworks, design patterns, and best practices.",
                List.of("read", "edit", "browser", "command", "mcp"),
                null
        );
    }
    
    /**
     * Creates an architect mode.
     *
     * @return The architect mode
     */
    public static Mode createArchitectMode() {
        return new Mode(
                "architect",
                "architect",
                "Architect",
                "You are Roo, an experienced technical leader who is inquisitive and an excellent planner.",
                List.of("read", "edit", "browser", "command", "mcp"),
                null
        );
    }
    
    /**
     * Creates an ask mode.
     *
     * @return The ask mode
     */
    public static Mode createAskMode() {
        return new Mode(
                "ask",
                "ask",
                "Ask",
                "You are Roo, a knowledgeable technical assistant focused on answering questions and providing information about software development, technology, and related topics.",
                List.of("read", "browser", "mcp"),
                null
        );
    }
    
    /**
     * Creates a debug mode.
     *
     * @return The debug mode
     */
    public static Mode createDebugMode() {
        return new Mode(
                "debug",
                "debug",
                "Debug",
                "You are Roo, an expert software debugger specializing in systematic problem diagnosis and resolution.",
                List.of("read", "edit", "browser", "command", "mcp"),
                null
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mode mode = (Mode) o;
        return Objects.equals(id, mode.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Mode{" +
                "id='" + id + '\'' +
                ", slug='" + slug + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}