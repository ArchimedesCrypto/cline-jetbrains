package com.cline.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Service for file operations in the Cline plugin.
 */
@Service
public final class ClineFileService {
    private static final Logger LOG = Logger.getInstance(ClineFileService.class);
    private final Project project;

    public ClineFileService(Project project) {
        this.project = project;
    }

    public static ClineFileService getInstance(Project project) {
        return project.getService(ClineFileService.class);
    }

    /**
     * Read the contents of a file.
     *
     * @param filePath Path to the file
     * @return A CompletableFuture containing the file contents
     */
    public CompletableFuture<String> readFile(String filePath) {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                String content = ReadAction.compute(() -> {
                    try {
                        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath);
                        if (virtualFile == null || !virtualFile.exists()) {
                            throw new IOException("File not found: " + filePath);
                        }

                        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
                        if (document != null) {
                            return document.getText();
                        } else {
                            // Fallback to reading from disk if document is not available
                            return new String(virtualFile.contentsToByteArray(), StandardCharsets.UTF_8);
                        }
                    } catch (IOException e) {
                        LOG.error("Error reading file: " + filePath, e);
                        throw new RuntimeException("Error reading file: " + filePath, e);
                    }
                });
                
                future.complete(content);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        
        return future;
    }

    /**
     * Write content to a file.
     *
     * @param filePath Path to the file
     * @param content  Content to write
     * @return A CompletableFuture that completes when the write operation is done
     */
    public CompletableFuture<Void> writeFile(String filePath, String content) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        // First ensure parent directories exist (outside of write action)
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            
            ApplicationManager.getApplication().invokeLater(() -> {
                try {
                    WriteAction.runAndWait(() -> {
                        try {
                            // Get or create the virtual file
                            VirtualFile parentDir = LocalFileSystem.getInstance().refreshAndFindFileByPath(
                                    path.getParent().toString());
                            
                            if (parentDir == null) {
                                throw new IOException("Parent directory not found: " + path.getParent());
                            }

                            VirtualFile virtualFile = parentDir.findChild(path.getFileName().toString());
                            if (virtualFile == null) {
                                virtualFile = parentDir.createChildData(this, path.getFileName().toString());
                            }

                            // Write content to the file
                            virtualFile.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                        } catch (IOException e) {
                            throw new RuntimeException("Error writing to file: " + filePath, e);
                        }
                    });
                    
                    future.complete(null);
                } catch (Exception e) {
                    LOG.error("Error writing to file: " + filePath, e);
                    future.completeExceptionally(e);
                }
            });
        } catch (IOException e) {
            LOG.error("Error creating directories for file: " + filePath, e);
            future.completeExceptionally(e);
        }
        
        return future;
    }

    /**
     * Find files by name pattern.
     *
     * @param namePattern File name pattern
     * @return A list of file paths matching the pattern
     */
    public List<String> findFilesByName(String namePattern) {
        Collection<VirtualFile> files = FilenameIndex.getAllFilesByExt(
                project, 
                namePattern.replace("*.", ""), 
                GlobalSearchScope.projectScope(project)
        );
        
        List<String> result = new ArrayList<>();
        for (VirtualFile file : files) {
            result.add(file.getPath());
        }
        return result;
    }

    /**
     * Check if a file exists.
     *
     * @param filePath Path to the file
     * @return True if the file exists, false otherwise
     */
    public boolean fileExists(String filePath) {
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(filePath);
        return file != null && file.exists();
    }
}