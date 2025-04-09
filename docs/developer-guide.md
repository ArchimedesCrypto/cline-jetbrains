# Cline for JetBrains - Developer Guide

## Introduction

This guide provides information for developers looking to contribute to the Cline for JetBrains plugin or understand its internal workings.

## Project Setup

1.  **Prerequisites:**
    *   JDK 17+
    *   Gradle 8+
    *   IntelliJ IDEA (recommended) with the Gradle and Plugin DevKit plugins.
2.  **Clone:** Clone the repository.
3.  **Import:** Open the `cline-jetbrains-java` sub-project in IntelliJ IDEA as a Gradle project.
4.  **Build:** Run the `build` Gradle task (`./gradlew build`).
5.  **Run/Debug:** Use the `runIde` Gradle task to launch a sandboxed IDE instance with the plugin installed.

## Architecture Overview

The plugin follows a standard JetBrains plugin architecture:

*   **`com.cline.ui`:** Contains all Swing UI components (Views, Panels, Renderers).
    *   `ChatView`, `HistoryView`, `SettingsView`, etc.
    *   Overlays managed by `ClineToolWindowContent`.
*   **`com.cline.services`:** Provides application-level services.
    *   `ClineApiService`: Handles API communication.
    *   `ClineSettingsService`: Manages plugin settings.
    *   `ClineHistoryService`: Manages conversation history.
    *   `ClineTerminalService`: Executes terminal commands.
    *   `ClineFileService`: Handles file operations.
*   **`com.cline.core`:** Contains core domain logic and models.
    *   `model`: `Conversation`, `Message` classes.
    *   `tool`: `Tool`, `ToolExecutor`, `ToolRegistry`, specific tool implementations.
*   **`com.cline.actions`:** Defines UI actions (e.g., toolbar button actions).
*   **`com.cline.utils`:** Utility classes.

*(More sections to be added: Adding Tools, Adding API Providers)*