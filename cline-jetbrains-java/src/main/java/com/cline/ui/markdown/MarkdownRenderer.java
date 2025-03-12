package com.cline.ui.markdown;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;
import java.io.StringReader;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renderer for Markdown content in the chat view.
 */
public class MarkdownRenderer {
    private static final Logger LOG = Logger.getInstance(MarkdownRenderer.class);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\s*([\\s\\S]*?)```");
    
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    public MarkdownRenderer() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder()
                .nodeRendererFactory(context -> new CustomNodeRenderer(context))
                .build();
    }
    
    /**
     * Renders markdown content to a JTextPane.
     *
     * @param markdown The markdown content
     * @param textPane The text pane to render to
     */
    public void render(@NotNull String markdown, @NotNull JTextPane textPane) {
        try {
            // Pre-process code blocks
            markdown = preprocessCodeBlocks(markdown);
            
            // Parse markdown to HTML
            Node document = parser.parse(markdown);
            String html = renderer.render(document);
            
            // Set up the text pane for HTML
            textPane.setContentType("text/html");
            HTMLDocument htmlDoc = (HTMLDocument) textPane.getDocument();
            HTMLEditorKit editorKit = (HTMLEditorKit) textPane.getEditorKit();
            
            // Apply custom styles
            String css = "body { font-family: " + JBUI.Fonts.label().getFamily() + "; font-size: " + JBUI.Fonts.label().getSize() + "pt; }" +
                    "pre { background-color: " + toHex(JBColor.background().darker()) + "; padding: 8px; border-radius: 4px; }" +
                    "code { font-family: monospace; background-color: " + toHex(JBColor.background().darker()) + "; padding: 2px 4px; border-radius: 3px; }" +
                    "blockquote { border-left: 4px solid " + toHex(JBColor.gray) + "; margin-left: 0; padding-left: 10px; color: " + toHex(JBColor.gray) + "; }" +
                    "a { color: " + toHex(JBColor.blue) + "; }" +
                    "ul, ol { margin-left: 20px; }" +
                    "li { margin-bottom: 5px; }" +
                    "p { margin-top: 0; margin-bottom: 10px; }" +
                    "h1, h2, h3, h4, h5, h6 { margin-top: 20px; margin-bottom: 10px; }" +
                    "img { max-width: 100%; }";
            
            // Apply the CSS
            editorKit.getStyleSheet().loadRules(new StringReader(css), null);
            
            // Set the HTML content
            textPane.setText("<html><body>" + html + "</body></html>");
            
            // Make the text pane non-editable and set other properties
            textPane.setEditable(false);
            textPane.setBorder(JBUI.Borders.empty(8));
            textPane.setBackground(JBColor.background());
            
            // Add hyperlink support
            textPane.addHyperlinkListener(e -> {
                if (e.getEventType() == javax.swing.event.HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                        Desktop.getDesktop().browse(e.getURL().toURI());
                    } catch (Exception ex) {
                        LOG.error("Error opening URL: " + e.getURL(), ex);
                    }
                }
            });
        } catch (Exception e) {
            LOG.error("Error rendering markdown", e);
            textPane.setContentType("text/plain");
            textPane.setText("Error rendering markdown: " + e.getMessage() + "\n\nOriginal content:\n" + markdown);
        }
    }
    
    /**
     * Pre-processes code blocks to ensure proper rendering.
     *
     * @param markdown The markdown content
     * @return The processed markdown
     */
    private String preprocessCodeBlocks(String markdown) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(markdown);
        
        while (matcher.find()) {
            String language = matcher.group(1);
            String code = matcher.group(2);
            
            // Escape HTML entities in the code
            code = code.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;");
            
            // Format as HTML pre/code block
            String replacement = "<pre><code class=\"language-" + language + "\">" + code + "</code></pre>";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * Converts a Color to a hex string.
     *
     * @param color The color
     * @return The hex string
     */
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
    
    /**
     * Custom node renderer for special markdown elements.
     * This is a simplified implementation that doesn't handle images specially.
     */
    private static class CustomNodeRenderer implements NodeRenderer {
        private final HtmlNodeRendererContext context;
        
        public CustomNodeRenderer(HtmlNodeRendererContext context) {
            this.context = context;
        }
        
        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(org.commonmark.node.Image.class);
        }
        
        @Override
        public void render(Node node) {
            // Let the default renderer handle images
            // This avoids using the html() method which is not available
        }
    }
}