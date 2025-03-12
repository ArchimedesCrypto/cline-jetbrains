package com.cline.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service for file system operations in the Cline plugin.
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
     * Read the contents of a file synchronously.
     * This method is primarily for testing purposes.
     *
     * @param path The path of the file to read
     * @return The file contents
     * @throws IOException If an error occurs while reading the file
     */
    protected String readFileSync(String path) throws IOException {
        VirtualFile file = findFile(path);
        if (file == null) {
            throw new IOException("File not found: " + path);
        }
        
        return ReadAction.compute(() -> {
            try {
                return new String(file.contentsToByteArray(), file.getCharset());
            } catch (IOException e) {
                LOG.error("Error reading file: " + path, e);
                throw new RuntimeException("Error reading file: " + path, e);
            }
        });
    }

    /**
     * Write content to a file synchronously.
     * This method is primarily for testing purposes.
     *
     * @param path The path of the file to write
     * @param content The content to write
     * @return True if the file was written successfully, false otherwise
     */
    protected boolean writeFileSync(String path, String content) {
        try {
            File file = new File(path);
            
            // Create parent directories if they don't exist
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create directories for: " + path);
                }
            }
            
            // Write the file
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            
            // Refresh the VFS
            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file.getAbsolutePath());
            if (virtualFile != null) {
                virtualFile.refresh(false, false);
            }
            
            return true;
        } catch (IOException e) {
            LOG.error("Error writing file: " + path, e);
            return false;
        }
    }

    /**
     * Read the contents of a file.
     *
     * @param path The path of the file to read
     * @return A CompletableFuture containing the file contents
     */
    public CompletableFuture<String> readFile(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                VirtualFile file = findFile(path);
                if (file == null) {
                    throw new IOException("File not found: " + path);
                }
                
                return ReadAction.compute(() -> new String(file.contentsToByteArray(), file.getCharset()));
            } catch (IOException e) {
                LOG.error("Error reading file: " + path, e);
                throw new RuntimeException("Error reading file: " + path, e);
            }
        });
    }

    /**
     * Write content to a file.
     *
     * @param path The path of the file to write
     * @param content The content to write
     * @return A CompletableFuture that completes when the file is written
     */
    public CompletableFuture<Boolean> writeFile(String path, String content) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                File file = new File(path);
                
                // Create parent directories if they don't exist
                if (!file.getParentFile().exists()) {
                    if (!file.getParentFile().mkdirs()) {
                        throw new IOException("Failed to create directories for: " + path);
                    }
                }
                
                // Write the file
                Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
                
                // Refresh the VFS
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(file.getAbsolutePath());
                if (virtualFile != null) {
                    virtualFile.refresh(false, false);
                }
                
                return true;
            } catch (IOException e) {
                LOG.error("Error writing file: " + path, e);
                return false;
            }
        });
    }

    /**
     * Apply a diff to a file.
     *
     * @param path The path of the file to modify
     * @param startLine The start line of the diff (1-based)
     * @param endLine The end line of the diff (1-based)
     * @param replacement The replacement text
     * @return A CompletableFuture that completes when the diff is applied
     */
    public CompletableFuture<Void> applyDiff(String path, int startLine, int endLine, String replacement) {
        return readFile(path)
                .thenCompose(content -> {
                    String[] lines = content.split("\n", -1);
                    
                    // Validate line numbers
                    if (startLine < 1 || startLine > lines.length + 1 || endLine < startLine - 1 || endLine > lines.length) {
                        return CompletableFuture.failedFuture(
                                new IllegalArgumentException("Invalid line numbers: " + startLine + "-" + endLine)
                        );
                    }
                    
                    // Apply the diff
                    StringBuilder newContent = new StringBuilder();
                    
                    // Add lines before the diff
                    for (int i = 0; i < startLine - 1; i++) {
                        newContent.append(lines[i]).append("\n");
                    }
                    
                    // Add the replacement
                    newContent.append(replacement);
                    if (!replacement.endsWith("\n")) {
                        newContent.append("\n");
                    }
                    
                    // Add lines after the diff
                    for (int i = endLine; i < lines.length; i++) {
                        newContent.append(lines[i]);
                        if (i < lines.length - 1) {
                            newContent.append("\n");
                        }
                    }
                    
                    // Write the modified content
                    return writeFile(path, newContent.toString()).thenApply(success -> null);
                });
    }

    /**
     * Apply a diff to a file using the search/replace format.
     *
     * @param path The path of the file to modify
     * @param diff The diff in search/replace format
     * @param startLine The start line of the diff (1-based)
     * @param endLine The end line of the diff (1-based)
     * @return A CompletableFuture that completes with true if the diff was applied successfully
     */
    public CompletableFuture<Boolean> applyDiff(String path, String diff, int startLine, int endLine) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Parse the diff
                Pattern pattern = Pattern.compile("<<<<<<< SEARCH\\n(.*?)=======\\n(.*?)>>>>>>> REPLACE", Pattern.DOTALL);
                Matcher matcher = pattern.matcher(diff);
                
                if (!matcher.find()) {
                    LOG.error("Invalid diff format: " + diff);
                    return false;
                }
                
                String search = matcher.group(1);
                String replace = matcher.group(2);
                
                // Read the file
                String content = readFile(path).get();
                
                // Apply the diff
                String newContent = content.replace(search, replace);
                
                // Write the file
                return writeFile(path, newContent).get();
            } catch (Exception e) {
                LOG.error("Error applying diff to file: " + path, e);
                return false;
            }
        });
    }

    /**
     * List files in a directory.
     *
     * @param directory The directory to list
     * @param recursive Whether to list files recursively
     * @return A CompletableFuture containing the list of file paths
     */
    public CompletableFuture<List<String>> listFiles(String directory, boolean recursive) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path dir = Paths.get(directory);
                if (!Files.exists(dir)) {
                    throw new IOException("Directory not found: " + directory);
                }
                
                if (!Files.isDirectory(dir)) {
                    throw new IOException("Not a directory: " + directory);
                }
                
                try (Stream<Path> stream = recursive ? Files.walk(dir) : Files.list(dir)) {
                    return stream
                            .filter(path -> !Files.isDirectory(path))
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            } catch (IOException e) {
                LOG.error("Error listing files in directory: " + directory, e);
                throw new RuntimeException("Error listing files in directory: " + directory, e);
            }
        });
    }

    /**
     * List code definitions in a directory.
     *
     * @param directory The directory to list code definitions for
     * @return A CompletableFuture containing a map of file paths to lists of definition names
     */
    public CompletableFuture<Map<String, List<String>>> listCodeDefinitions(String directory) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path dir = Paths.get(directory);
                if (!Files.exists(dir)) {
                    throw new IOException("Directory not found: " + directory);
                }
                
                if (!Files.isDirectory(dir)) {
                    throw new IOException("Not a directory: " + directory);
                }
                
                Map<String, List<String>> result = new HashMap<>();
                
                // Get all files in the directory
                try (Stream<Path> stream = Files.walk(dir)) {
                    List<Path> files = stream
                            .filter(path -> !Files.isDirectory(path))
                            .collect(Collectors.toList());
                    
                    for (Path file : files) {
                        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(file.toString());
                        if (virtualFile == null) {
                            continue;
                        }
                        
                        // Get the PSI file
                        PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(project).findFile(virtualFile));
                        if (psiFile == null) {
                            continue;
                        }
                        
                        List<String> definitions = new ArrayList<>();
                        
                        // Extract definitions based on file type
                        ReadAction.run(() -> {
                            if (psiFile instanceof PsiJavaFile) {
                                // Java file
                                PsiJavaFile javaFile = (PsiJavaFile) psiFile;
                                for (PsiClass psiClass : javaFile.getClasses()) {
                                    definitions.add("class " + psiClass.getName());
                                    
                                    // Add methods
                                    for (PsiMethod method : psiClass.getMethods()) {
                                        definitions.add("method " + method.getName());
                                    }
                                    
                                    // Add fields
                                    for (PsiField field : psiClass.getFields()) {
                                        definitions.add("field " + field.getName());
                                    }
                                }
                            } else if (psiFile.getFileType().getName().equals("JavaScript") || 
                                    psiFile.getFileType().getName().equals("TypeScript")) {
                                // JavaScript/TypeScript file
                                // This is a simplified implementation
                                String content = psiFile.getText();
                                Pattern functionPattern = Pattern.compile("function\\s+(\\w+)\\s*\\(");
                                Matcher functionMatcher = functionPattern.matcher(content);
                                while (functionMatcher.find()) {
                                    definitions.add("function " + functionMatcher.group(1));
                                }
                                
                                Pattern classPattern = Pattern.compile("class\\s+(\\w+)");
                                Matcher classMatcher = classPattern.matcher(content);
                                while (classMatcher.find()) {
                                    definitions.add("class " + classMatcher.group(1));
                                }
                                
                                Pattern constPattern = Pattern.compile("const\\s+(\\w+)\\s*=");
                                Matcher constMatcher = constPattern.matcher(content);
                                while (constMatcher.find()) {
                                    definitions.add("const " + constMatcher.group(1));
                                }
                            }
                        });
                        
                        if (!definitions.isEmpty()) {
                            result.put(file.toString(), definitions);
                        }
                    }
                }
                
                return result;
            } catch (IOException e) {
                LOG.error("Error listing code definitions in directory: " + directory, e);
                throw new RuntimeException("Error listing code definitions in directory: " + directory, e);
            }
        });
    }

    /**
     * Search for files matching a pattern.
     *
     * @param directory The directory to search in
     * @param pattern The regex pattern to match
     * @param filePattern The file name pattern to match (glob)
     * @return A CompletableFuture containing the list of matching file paths
     */
    public CompletableFuture<List<SearchResult>> searchFiles(String directory, String pattern, @Nullable String filePattern) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path dir = Paths.get(directory);
                if (!Files.exists(dir)) {
                    throw new IOException("Directory not found: " + directory);
                }
                
                if (!Files.isDirectory(dir)) {
                    throw new IOException("Not a directory: " + directory);
                }
                
                Pattern regex = Pattern.compile(pattern);
                Pattern fileRegex = filePattern != null ? 
                        Pattern.compile(filePattern.replace("*", ".*").replace("?", ".")) : 
                        null;
                
                List<SearchResult> results = new ArrayList<>();
                
                try (Stream<Path> stream = Files.walk(dir)) {
                    List<Path> files = stream
                            .filter(path -> !Files.isDirectory(path))
                            .filter(path -> fileRegex == null || fileRegex.matcher(path.getFileName().toString()).matches())
                            .collect(Collectors.toList());
                    
                    for (Path file : files) {
                        List<String> lines = Files.readAllLines(file);
                        for (int i = 0; i < lines.size(); i++) {
                            String line = lines.get(i);
                            if (regex.matcher(line).find()) {
                                // Get context (3 lines before and after)
                                int startLine = Math.max(0, i - 3);
                                int endLine = Math.min(lines.size() - 1, i + 3);
                                
                                List<String> context = new ArrayList<>();
                                for (int j = startLine; j <= endLine; j++) {
                                    context.add((j + 1) + " | " + lines.get(j));
                                }
                                
                                results.add(new SearchResult(
                                        file.toString(),
                                        i + 1,
                                        line,
                                        context
                                ));
                            }
                        }
                    }
                }
                
                return results;
            } catch (IOException e) {
                LOG.error("Error searching files in directory: " + directory, e);
                throw new RuntimeException("Error searching files in directory: " + directory, e);
            }
        });
    }

    /**
     * Find a file by path.
     *
     * @param path The path of the file to find
     * @return The virtual file, or null if not found
     */
    @Nullable
    private VirtualFile findFile(String path) {
        // Try absolute path first
        VirtualFile file = LocalFileSystem.getInstance().findFileByPath(path);
        if (file != null) {
            return file;
        }
        
        // Try relative to project
        String projectPath = project.getBasePath();
        if (projectPath != null) {
            file = LocalFileSystem.getInstance().findFileByPath(projectPath + "/" + path);
            if (file != null) {
                return file;
            }
        }
        
        // Try by name
        String fileName = Paths.get(path).getFileName().toString();
        Collection<VirtualFile> files = FilenameIndex.getVirtualFilesByName(
                fileName, GlobalSearchScope.projectScope(project));
        
        for (VirtualFile vFile : files) {
            if (vFile.getPath().endsWith(path)) {
                return vFile;
            }
        }
        
        return null;
    }

    /**
     * Open a file in the editor.
     *
     * @param path The path of the file to open
     * @return A CompletableFuture that completes when the file is opened
     */
    public CompletableFuture<Void> openFile(String path) {
        return CompletableFuture.runAsync(() -> {
            ApplicationManager.getApplication().invokeLater(() -> {
                VirtualFile file = findFile(path);
                if (file != null) {
                    FileEditorManager.getInstance(project).openFile(file, true);
                } else {
                    LOG.warn("File not found: " + path);
                }
            });
        });
    }

    /**
     * Create a new directory.
     *
     * @param path The path of the directory to create
     * @return A CompletableFuture that completes when the directory is created
     */
    public CompletableFuture<Void> createDirectory(String path) {
        return CompletableFuture.runAsync(() -> {
            try {
                File dir = new File(path);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        throw new IOException("Failed to create directory: " + path);
                    }
                    
                    // Refresh the VFS
                    LocalFileSystem.getInstance().refreshAndFindFileByPath(dir.getAbsolutePath());
                }
            } catch (IOException e) {
                LOG.error("Error creating directory: " + path, e);
                throw new RuntimeException("Error creating directory: " + path, e);
            }
        });
    }

    /**
     * Delete a file or directory.
     *
     * @param path The path of the file or directory to delete
     * @return A CompletableFuture that completes when the file or directory is deleted
     */
    public CompletableFuture<Void> delete(String path) {
        return CompletableFuture.runAsync(() -> {
            try {
                VirtualFile file = findFile(path);
                if (file == null) {
                    throw new IOException("File not found: " + path);
                }
                
                WriteAction.runAndWait(() -> {
                    try {
                        file.delete(this);
                    } catch (IOException e) {
                        LOG.error("Error deleting file: " + path, e);
                        throw new RuntimeException("Error deleting file: " + path, e);
                    }
                });
            } catch (Exception e) {
                LOG.error("Error deleting file: " + path, e);
                throw new RuntimeException("Error deleting file: " + path, e);
            }
        });
    }

    /**
     * Represents a search result.
     */
    public static class SearchResult {
        private final String filePath;
        private final int lineNumber;
        private final String line;
        private final List<String> context;

        public SearchResult(String filePath, int lineNumber, String line, List<String> context) {
            this.filePath = filePath;
            this.lineNumber = lineNumber;
            this.line = line;
            this.context = context;
        }

        public String getFilePath() {
            return filePath;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        public String getLine() {
            return line;
        }

        public List<String> getContext() {
            return context;
        }

        @Override
        public String toString() {
            return filePath + ":" + lineNumber + "\n" + String.join("\n", context);
        }
    }
}