package com.cline.services;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ClineFileService class.
 */
public class ClineFileServiceTest {
    
    private ClineFileService fileService;
    
    @Mock
    private Project project;
    
    @Mock
    private LocalFileSystem localFileSystem;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock project base path
        when(project.getBasePath()).thenReturn(tempDir.toString());
        
        // Create file service
        fileService = new ClineFileService(project);
    }
    
    @Test
    public void testReadFile() throws IOException, ExecutionException, InterruptedException {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, world!";
        Files.writeString(testFile, content);
        
        // Read the file
        String result = fileService.readFile(testFile.toString()).get();
        
        // Verify the content
        assertEquals(content, result);
    }
    
    @Test
    public void testWriteFile() throws ExecutionException, InterruptedException, IOException {
        // Define file path and content
        Path testFile = tempDir.resolve("write-test.txt");
        String content = "This is a test content.";
        
        // Write the file
        Boolean result = fileService.writeFile(testFile.toString(), content).get();
        
        // Verify the result
        assertTrue(result);
        
        // Verify the file was written correctly
        String writtenContent = Files.readString(testFile);
        assertEquals(content, writtenContent);
    }
    
    @Test
    public void testApplyDiff() throws IOException, ExecutionException, InterruptedException {
        // Create a test file
        Path testFile = tempDir.resolve("diff-test.txt");
        String content = "Line 1\nLine 2\nLine 3\nLine 4\nLine 5\n";
        Files.writeString(testFile, content);
        
        // Apply diff
        fileService.applyDiff(testFile.toString(), 2, 4, "New Line 2\nNew Line 3\nNew Line 4").get();
        
        // Read the modified file
        String modifiedContent = Files.readString(testFile);
        
        // Verify the content
        assertEquals("Line 1\nNew Line 2\nNew Line 3\nNew Line 4\nLine 5\n", modifiedContent);
    }
    
    @Test
    public void testApplyDiffWithSearchReplace() throws IOException, ExecutionException, InterruptedException {
        // Create a test file
        Path testFile = tempDir.resolve("search-replace-test.txt");
        String content = "Line 1\nLine 2\nLine 3\nLine 4\nLine 5\n";
        Files.writeString(testFile, content);
        
        // Create diff
        String diff = "<<<<<<< SEARCH\nLine 2\nLine 3\nLine 4\n=======\nNew Line 2\nNew Line 3\nNew Line 4\n>>>>>>> REPLACE";
        
        // Apply diff
        Boolean result = fileService.applyDiff(testFile.toString(), diff, 2, 4).get();
        
        // Verify the result
        assertTrue(result);
        
        // Read the modified file
        String modifiedContent = Files.readString(testFile);
        
        // Verify the content
        assertEquals("Line 1\nNew Line 2\nNew Line 3\nNew Line 4\nLine 5\n", modifiedContent);
    }
    
    @Test
    public void testListFiles() throws IOException, ExecutionException, InterruptedException {
        // Create test files
        Files.writeString(tempDir.resolve("file1.txt"), "Content 1");
        Files.writeString(tempDir.resolve("file2.txt"), "Content 2");
        Files.createDirectory(tempDir.resolve("subdir"));
        Files.writeString(tempDir.resolve("subdir/file3.txt"), "Content 3");
        
        // List files (non-recursive)
        List<String> files = fileService.listFiles(tempDir.toString(), false).get();
        
        // Verify the result
        assertEquals(2, files.size());
        assertTrue(files.stream().anyMatch(f -> f.endsWith("file1.txt")));
        assertTrue(files.stream().anyMatch(f -> f.endsWith("file2.txt")));
        
        // List files (recursive)
        List<String> recursiveFiles = fileService.listFiles(tempDir.toString(), true).get();
        
        // Verify the result
        assertEquals(3, recursiveFiles.size());
        assertTrue(recursiveFiles.stream().anyMatch(f -> f.endsWith("file1.txt")));
        assertTrue(recursiveFiles.stream().anyMatch(f -> f.endsWith("file2.txt")));
        assertTrue(recursiveFiles.stream().anyMatch(f -> f.endsWith("subdir/file3.txt")));
    }
    
    @Test
    public void testCreateDirectory() throws ExecutionException, InterruptedException {
        // Define directory path
        Path newDir = tempDir.resolve("new-dir");
        
        // Create directory
        fileService.createDirectory(newDir.toString()).get();
        
        // Verify the directory was created
        assertTrue(Files.exists(newDir));
        assertTrue(Files.isDirectory(newDir));
    }
    
    @Test
    public void testSearchFiles() throws IOException, ExecutionException, InterruptedException {
        // Create test files
        Files.writeString(tempDir.resolve("search1.txt"), "This is line 1\nThis contains search term\nThis is line 3");
        Files.writeString(tempDir.resolve("search2.txt"), "Another file\nNo match here\nAnother line");
        Files.writeString(tempDir.resolve("search3.java"), "public class Test {\n    // This contains search term\n}");
        
        // Search files
        List<ClineFileService.SearchResult> results = fileService.searchFiles(
                tempDir.toString(), "search term", null).get();
        
        // Verify the results
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getFilePath().endsWith("search1.txt")));
        assertTrue(results.stream().anyMatch(r -> r.getFilePath().endsWith("search3.java")));
        
        // Search with file pattern
        List<ClineFileService.SearchResult> javaResults = fileService.searchFiles(
                tempDir.toString(), "search term", "*.java").get();
        
        // Verify the results
        assertEquals(1, javaResults.size());
        assertTrue(javaResults.get(0).getFilePath().endsWith("search3.java"));
    }
}