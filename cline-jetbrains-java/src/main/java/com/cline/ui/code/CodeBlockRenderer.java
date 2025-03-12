package com.cline.ui.code;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Renderer for code blocks with syntax highlighting.
 */
public class CodeBlockRenderer {
    private static final Logger LOG = Logger.getInstance(CodeBlockRenderer.class);
    private static final Map<String, String> LANGUAGE_TO_EXTENSION = new HashMap<>();
    
    static {
        // Map common language names to file extensions
        LANGUAGE_TO_EXTENSION.put("java", "java");
        LANGUAGE_TO_EXTENSION.put("kotlin", "kt");
        LANGUAGE_TO_EXTENSION.put("python", "py");
        LANGUAGE_TO_EXTENSION.put("javascript", "js");
        LANGUAGE_TO_EXTENSION.put("typescript", "ts");
        LANGUAGE_TO_EXTENSION.put("html", "html");
        LANGUAGE_TO_EXTENSION.put("css", "css");
        LANGUAGE_TO_EXTENSION.put("xml", "xml");
        LANGUAGE_TO_EXTENSION.put("json", "json");
        LANGUAGE_TO_EXTENSION.put("yaml", "yaml");
        LANGUAGE_TO_EXTENSION.put("markdown", "md");
        LANGUAGE_TO_EXTENSION.put("bash", "sh");
        LANGUAGE_TO_EXTENSION.put("shell", "sh");
        LANGUAGE_TO_EXTENSION.put("sql", "sql");
    }
    
    private final Project project;
    
    public CodeBlockRenderer(Project project) {
        this.project = project;
    }
    
    /**
     * Creates a JComponent with syntax-highlighted code.
     *
     * @param code     The code to render
     * @param language The language of the code
     * @return A JComponent with the rendered code
     */
    public JComponent createCodeComponent(@NotNull String code, @Nullable String language) {
        try {
            // Determine the file type based on the language
            FileType fileType = getFileTypeForLanguage(language);
            
            // Create a document with the code
            Document document = EditorFactory.getInstance().createDocument(code);
            
            // Create an editor for the document
            EditorEx editor = (EditorEx) EditorFactory.getInstance().createViewer(document, project);
            
            // Configure the editor
            // Use EditorHighlighterFactory to set the highlighter
            editor.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(
                    project, fileType));
            
            editor.getSettings().setLineNumbersShown(true);
            editor.getSettings().setFoldingOutlineShown(false);
            editor.getSettings().setLineMarkerAreaShown(false);
            editor.getSettings().setIndentGuidesShown(true);
            editor.getSettings().setVirtualSpace(false);
            editor.getSettings().setWheelFontChangeEnabled(false);
            editor.getSettings().setAdditionalLinesCount(0);
            editor.getSettings().setAdditionalColumnsCount(0);
            editor.getSettings().setRightMarginShown(false);
            
            // Return the editor component
            return editor.getComponent();
        } catch (Exception e) {
            LOG.error("Error creating code component", e);
            
            // Fallback to a simple text area
            JTextArea textArea = new JTextArea(code);
            textArea.setEditable(false);
            textArea.setFont(new JTextArea().getFont().deriveFont(12f));
            return new JScrollPane(textArea);
        }
    }
    
    /**
     * Gets the file type for a language.
     *
     * @param language The language
     * @return The file type
     */
    private FileType getFileTypeForLanguage(@Nullable String language) {
        if (language == null || language.isEmpty()) {
            return PlainTextFileType.INSTANCE;
        }
        
        String extension = LANGUAGE_TO_EXTENSION.get(language.toLowerCase());
        if (extension == null) {
            return PlainTextFileType.INSTANCE;
        }
        
        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(extension);
        return fileType != null ? fileType : PlainTextFileType.INSTANCE;
    }
}