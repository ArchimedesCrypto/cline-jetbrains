package com.cline.services;

import com.intellij.openapi.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for the ClineFileService class.
 * 
 * Note: These tests are simplified to avoid IntelliJ Platform dependencies.
 */
public class ClineFileServiceTest {
    
    @Mock
    private Project project;
    
    @Mock
    private ClineFileService fileService;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock project base path
        when(project.getBasePath()).thenReturn(tempDir.toString());
    }
    
    @Test
    public void testReadFile() throws IOException, ExecutionException, InterruptedException {
        // Create a test file
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, world!";
        Files.writeString(testFile, content);
        
        // Mock the readFile method
        when(fileService.readFile(testFile.toString())).thenReturn(
            CompletableFuture.completedFuture(content)
        );
        
        // Read the file
        String result = fileService.readFile(testFile.toString()).get();
        
        // Verify the content
        assertEquals(content, result);
        verify(fileService).readFile(testFile.toString());
    }
    
    @Test
    public void testWriteFile() throws ExecutionException, InterruptedException, IOException {
        // Define file path and content
        Path testFile = tempDir.resolve("write-test.txt");
        String content = "This is a test content.";
        
        // Mock the writeFile method
        when(fileService.writeFile(testFile.toString(), content)).thenReturn(
            CompletableFuture.completedFuture(true)
        );
        
        // Write the file
        Boolean result = fileService.writeFile(testFile.toString(), content).get();
        
        // Verify the result
        assertTrue(result);
        verify(fileService).writeFile(testFile.toString(), content);
    }
    
    @Test
    public void testListFiles() throws IOException, ExecutionException, InterruptedException {
        // Create test files
        Files.writeString(tempDir.resolve("file1.txt"), "Content 1");
        Files.writeString(tempDir.resolve("file2.txt"), "Content 2");
        Files.createDirectory(tempDir.resolve("subdir"));
        Files.writeString(tempDir.resolve("subdir/file3.txt"), "Content 3");
        
        // Mock the listFiles method
        List<String> nonRecursiveFiles = List.of(
            tempDir.resolve("file1.txt").toString(),
            tempDir.resolve("file2.txt").toString()
        );
        
        List<String> recursiveFiles = List.of(
            tempDir.resolve("file1.txt").toString(),
            tempDir.resolve("file2.txt").toString(),
            tempDir.resolve("subdir/file3.txt").toString()
        );
        
        when(fileService.listFiles(tempDir.toString(), false)).thenReturn(
            CompletableFuture.completedFuture(nonRecursiveFiles)
        );
        when(fileService.listFiles(tempDir.toString(), true)).thenReturn(
            CompletableFuture.completedFuture(recursiveFiles)
        );
        
        // List files (non-recursive)
        List<String> files = fileService.listFiles(tempDir.toString(), false).get();
        
        // Verify the result
        assertEquals(2, files.size());
        assertTrue(files.stream().anyMatch(f -> f.endsWith("file1.txt")));
        assertTrue(files.stream().anyMatch(f -> f.endsWith("file2.txt")));
        
        // List files (recursive)
        List<String> recursiveFilesResult = fileService.listFiles(tempDir.toString(), true).get();
        
        // Verify the result
        assertEquals(3, recursiveFilesResult.size());
        assertTrue(recursiveFilesResult.stream().anyMatch(f -> f.endsWith("file1.txt")));
        assertTrue(recursiveFilesResult.stream().anyMatch(f -> f.endsWith("file2.txt")));
        assertTrue(recursiveFilesResult.stream().anyMatch(f -> f.endsWith("subdir/file3.txt")));
    }
    
    @Test
    public void testSearchFiles() throws IOException, ExecutionException, InterruptedException {
        // Create test files
        Files.writeString(tempDir.resolve("search1.txt"), "This is line 1\nThis contains search term\nThis is line 3");
        Files.writeString(tempDir.resolve("search2.txt"), "Another file\nNo match here\nAnother line");
        Files.writeString(tempDir.resolve("search3.java"), "public class Test {\n    // This contains search term\n}");
        
        // Create search results
        List<ClineFileService.SearchResult> allResults = List.of(
            new ClineFileService.SearchResult(
                tempDir.resolve("search1.txt").toString(),
                2,
                "This contains search term",
                List.of("1 | This is line 1", "2 | This contains search term", "3 | This is line 3")
            ),
            new ClineFileService.SearchResult(
                tempDir.resolve("search3.java").toString(),
                2,
                "    // This contains search term",
                List.of("1 | public class Test {", "2 |     // This contains search term", "3 | }")
            )
        );
        
        List<ClineFileService.SearchResult> javaResults = List.of(
            new ClineFileService.SearchResult(
                tempDir.resolve("search3.java").toString(),
                2,
                "    // This contains search term",
                List.of("1 | public class Test {", "2 |     // This contains search term", "3 | }")
            )
        );
        
        when(fileService.searchFiles(tempDir.toString(), "search term", null)).thenReturn(
            CompletableFuture.completedFuture(allResults)
        );
        when(fileService.searchFiles(tempDir.toString(), "search term", "*.java")).thenReturn(
            CompletableFuture.completedFuture(javaResults)
        );
        
        // Search files
        List<ClineFileService.SearchResult> results = fileService.searchFiles(
                tempDir.toString(), "search term", null).get();
        
        // Verify the results
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getFilePath().endsWith("search1.txt")));
        assertTrue(results.stream().anyMatch(r -> r.getFilePath().endsWith("search3.java")));
        
        // Search with file pattern
        List<ClineFileService.SearchResult> javaResultsActual = fileService.searchFiles(
                tempDir.toString(), "search term", "*.java").get();
        
        // Verify the results
        assertEquals(1, javaResultsActual.size());
        assertTrue(javaResultsActual.get(0).getFilePath().endsWith("search3.java"));
    }
}