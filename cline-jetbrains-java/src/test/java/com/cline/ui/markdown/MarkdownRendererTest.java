package com.cline.ui.markdown;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the MarkdownRenderer class.
 */
public class MarkdownRendererTest {
    
    private MarkdownRenderer markdownRenderer;
    private JTextPane textPane;
    
    @BeforeEach
    public void setUp() {
        markdownRenderer = new MarkdownRenderer();
        textPane = new JTextPane();
    }
    
    @Test
    public void testRenderBasicMarkdown() {
        // Basic markdown with headings, bold, italic, and links
        String markdown = "# Heading 1\n\n" +
                "## Heading 2\n\n" +
                "This is **bold** and *italic* text.\n\n" +
                "This is a [link](https://example.com).\n\n";
        
        // Render the markdown
        markdownRenderer.render(markdown, textPane);
        
        // Verify that the content type is set to HTML
        assertEquals("text/html", textPane.getContentType());
        
        // Get the document
        Document document = textPane.getDocument();
        assertTrue(document instanceof HTMLDocument);
        
        // Get the text content (without HTML tags)
        String text = textPane.getText();
        
        // Verify that the text contains the expected content
        assertTrue(text.contains("Heading 1"));
        assertTrue(text.contains("Heading 2"));
        assertTrue(text.contains("bold"));
        assertTrue(text.contains("italic"));
        assertTrue(text.contains("link"));
    }
    
    @Test
    public void testRenderCodeBlocks() {
        // Markdown with code blocks
        String markdown = "```java\n" +
                "public class Test {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, world!\");\n" +
                "    }\n" +
                "}\n" +
                "```\n\n" +
                "And inline `code` too.";
        
        // Render the markdown
        markdownRenderer.render(markdown, textPane);
        
        // Get the text content (without HTML tags)
        String text = textPane.getText();
        
        // Verify that the text contains the expected content
        assertTrue(text.contains("public class Test"));
        assertTrue(text.contains("System.out.println"));
        assertTrue(text.contains("code"));
    }
    
    @Test
    public void testRenderLists() {
        // Markdown with lists
        String markdown = "Unordered list:\n\n" +
                "* Item 1\n" +
                "* Item 2\n" +
                "* Item 3\n\n" +
                "Ordered list:\n\n" +
                "1. First item\n" +
                "2. Second item\n" +
                "3. Third item";
        
        // Render the markdown
        markdownRenderer.render(markdown, textPane);
        
        // Get the text content (without HTML tags)
        String text = textPane.getText();
        
        // Verify that the text contains the expected content
        assertTrue(text.contains("Unordered list"));
        assertTrue(text.contains("Item 1"));
        assertTrue(text.contains("Item 2"));
        assertTrue(text.contains("Item 3"));
        assertTrue(text.contains("Ordered list"));
        assertTrue(text.contains("First item"));
        assertTrue(text.contains("Second item"));
        assertTrue(text.contains("Third item"));
    }
    
    @Test
    public void testRenderBlockquotes() {
        // Markdown with blockquotes
        String markdown = "> This is a blockquote.\n" +
                "> It can span multiple lines.\n\n" +
                "Regular text.";
        
        // Render the markdown
        markdownRenderer.render(markdown, textPane);
        
        // Get the text content (without HTML tags)
        String text = textPane.getText();
        
        // Verify that the text contains the expected content
        assertTrue(text.contains("This is a blockquote"));
        assertTrue(text.contains("It can span multiple lines"));
        assertTrue(text.contains("Regular text"));
    }
    
    @Test
    public void testRenderTables() {
        // Markdown with tables
        String markdown = "| Header 1 | Header 2 |\n" +
                "|----------|----------|\n" +
                "| Cell 1   | Cell 2   |\n" +
                "| Cell 3   | Cell 4   |";
        
        // Render the markdown
        markdownRenderer.render(markdown, textPane);
        
        // Get the text content (without HTML tags)
        String text = textPane.getText();
        
        // Verify that the text contains the expected content
        assertTrue(text.contains("Header 1"));
        assertTrue(text.contains("Header 2"));
        assertTrue(text.contains("Cell 1"));
        assertTrue(text.contains("Cell 2"));
        assertTrue(text.contains("Cell 3"));
        assertTrue(text.contains("Cell 4"));
    }
    
    @Test
    public void testRenderInvalidMarkdown() {
        // Invalid markdown should not cause exceptions
        String markdown = "# Heading\n\n" +
                "This is [invalid markdown.";
        
        // Render the markdown (should not throw an exception)
        assertDoesNotThrow(() -> markdownRenderer.render(markdown, textPane));
        
        // Get the text content (without HTML tags)
        String text = textPane.getText();
        
        // Verify that the text contains the expected content
        assertTrue(text.contains("Heading"));
        assertTrue(text.contains("This is [invalid markdown"));
    }
}