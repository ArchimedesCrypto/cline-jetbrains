package com.cline.ui.image;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

/**
 * Renderer for images in messages.
 */
public class ImageRenderer {
    private static final Logger LOG = Logger.getInstance(ImageRenderer.class);
    private static final int MAX_WIDTH = 600;
    private static final int MAX_HEIGHT = 400;
    
    /**
     * Creates a JComponent with the rendered image.
     *
     * @param source The image source (URL, file path, or base64 data)
     * @return A JComponent with the rendered image, or null if the image could not be loaded
     */
    @Nullable
    public static JComponent createImageComponent(@NotNull String source) {
        try {
            BufferedImage image = loadImage(source);
            if (image == null) {
                return createErrorLabel("Failed to load image");
            }
            
            // Scale the image if needed
            Dimension scaledSize = calculateScaledSize(image.getWidth(), image.getHeight());
            if (scaledSize.width != image.getWidth() || scaledSize.height != image.getHeight()) {
                image = scaleImage(image, scaledSize.width, scaledSize.height);
            }
            
            // Create an image icon
            ImageIcon icon = new ImageIcon(image);
            
            // Create a label with the image
            JLabel label = new JLabel(icon);
            label.setBorder(JBUI.Borders.empty(4));
            
            return label;
        } catch (Exception e) {
            LOG.error("Error creating image component", e);
            return createErrorLabel("Error: " + e.getMessage());
        }
    }
    
    /**
     * Loads an image from a source.
     *
     * @param source The image source (URL, file path, or base64 data)
     * @return The loaded image, or null if the image could not be loaded
     */
    @Nullable
    private static BufferedImage loadImage(@NotNull String source) {
        try {
            // Check if the source is a base64 data URL
            if (source.startsWith("data:image/")) {
                return loadBase64Image(source);
            }
            
            // Check if the source is a URL
            if (source.startsWith("http://") || source.startsWith("https://")) {
                return loadUrlImage(source);
            }
            
            // Assume the source is a file path
            return loadFileImage(source);
        } catch (Exception e) {
            LOG.error("Error loading image", e);
            return null;
        }
    }
    
    private static BufferedImage loadBase64Image(@NotNull String dataUrl) throws IOException {
        int commaIndex = dataUrl.indexOf(',');
        if (commaIndex == -1) {
            throw new IOException("Invalid data URL");
        }
        
        String base64Data = dataUrl.substring(commaIndex + 1);
        byte[] imageData = Base64.getDecoder().decode(base64Data);
        
        try (ByteArrayInputStream stream = new ByteArrayInputStream(imageData)) {
            return ImageIO.read(stream);
        }
    }
    
    private static BufferedImage loadUrlImage(@NotNull String url) throws IOException {
        return ImageIO.read(new URL(url));
    }
    
    private static BufferedImage loadFileImage(@NotNull String path) throws IOException {
        return ImageIO.read(new File(path));
    }
    
    private static BufferedImage scaleImage(@NotNull BufferedImage image, int width, int height) {
        BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaledImage.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        return scaledImage;
    }
    
    private static Dimension calculateScaledSize(int width, int height) {
        int scaledWidth = width;
        int scaledHeight = height;
        
        if (scaledWidth > MAX_WIDTH) {
            double ratio = (double) MAX_WIDTH / scaledWidth;
            scaledWidth = MAX_WIDTH;
            scaledHeight = (int) (scaledHeight * ratio);
        }
        
        if (scaledHeight > MAX_HEIGHT) {
            double ratio = (double) MAX_HEIGHT / scaledHeight;
            scaledHeight = MAX_HEIGHT;
            scaledWidth = (int) (scaledWidth * ratio);
        }
        
        return new Dimension(scaledWidth, scaledHeight);
    }
    
    private static JLabel createErrorLabel(@NotNull String message) {
        JLabel label = new JLabel(message);
        label.setForeground(JBColor.RED);
        label.setBorder(JBUI.Borders.empty(4));
        return label;
    }
}