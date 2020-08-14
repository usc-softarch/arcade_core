package edu.usc.softarch.arcade.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ginsberg.junit.exit.ExpectSystemExit;

import org.junit.jupiter.api.Test;

/**
 * Tests for the FileUtil utilities. All failures are known issues with
 * FileUtil and fixes are pending.
 */
public class FileUtilTest {
    // #region TESTS extractFilenamePrefix -------------------------------------
    @Test
    public void extractFilenamePrefixTest1() {
        // Simple filename test
        String test = "fileName.suffix";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void extractFilenamePrefixTest2() {
        // Filename without suffix
        String test = "fileName";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void extractFilenamePrefixTest3() {
        // Filename inside directory, relative path, Windows format
        char fs = File.separatorChar;
        String test = "directory" + fs + "fileName.suffix";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void extractFilenamePrefixTest4() {
        // Filename inside directory, absolute path, Unix format
        char fs = File.separatorChar;
        String test = fs + "dir1" + fs + "dir2" + fs + "dir3" 
            + fs + "fileName.suffix";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void extractFilenamePrefixTest5() {
        // File has suffix
        char fs = File.separatorChar;
        String test = "." + fs + "src" + fs + "test" + fs + "resources"
            + fs + "FileUtilTest_resources" + fs + "empty.txt";
        File input = new File(test);
        String result = FileUtil.extractFilenamePrefix(input);
        assertEquals("empty", result);
    }

    @Test
    public void extractFilenamePrefixTest6() {
        // File does not have suffix
        char fs = File.separatorChar;
        String test = "." + fs + "src" + fs + "test" + fs + "resources"
            + fs + "FileUtilTest_resources" + fs + "fileWithoutSuffix";
        File input = new File(test);
        String result = FileUtil.extractFilenamePrefix(input);
        assertEquals("fileWithoutSuffix", result);
    }
    // #endregion TESTS extractFilenamePrefix ----------------------------------

    // #region TESTS extractFilenameSuffix -------------------------------------
    @Test
    public void extractFilenameSuffixTest1() {
        // Simple filename test
        String test = "fileName.suffix";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals(".suffix", result);
    }

    @Test
    public void extractFilenameSuffixTest2() {
        // Filename without suffix
        String test = "fileName";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals("", result);
    }

    @Test
    public void extractFilenameSuffixTest3() {
        // Filename inside directory, relative path, Windows format
        char fs = File.separatorChar;
        String test = "directory" + fs + "fileName.suffix";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals(".suffix", result);
    }

    @Test
    public void extractFilenameSuffixTest4() {
        // Filename inside directory, absolute path, Unix format
        char fs = File.separatorChar;
        String test = fs + "dir1" + fs + "dir2" + fs + "dir3" 
            + fs + "fileName.suffix";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals(".suffix", result);
    }

    @Test
    public void extractFilenameSuffixTest5() {
        // File has suffix
        char fs = File.separatorChar;
        String test = "." + fs + "src" + fs + "test" + fs + "resources"
            + fs + "FileUtilTest_resources" + fs + "empty.txt";
        File input = new File(test);
        String result = FileUtil.extractFilenameSuffix(input);
        assertEquals(".txt", result);
    }

    @Test
    public void extractFilenameSuffixTest6() {
        // File does not have suffix
        char fs = File.separatorChar;
        String test = "." + fs + "src" + fs + "test" + fs + "resources"
            + fs + "FileUtilTest_resources" + fs + "fileWithoutSuffix";
        File input = new File(test);
        String result = FileUtil.extractFilenameSuffix(input);
        assertEquals("", result);
    }
    // #endregion TESTS extractFilenameSuffix ----------------------------------

    // #region TESTS readFile --------------------------------------------------
    @Test
    public void readFileTest1() {
        // File is empty
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "empty.txt";
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.readFile(filePath, StandardCharsets.UTF_8); });
        assertEquals("", result);
    }

    @Test
    public void readFileTest2() {
        // File is a regular text, correct encoding
        char fs = File.separatorChar;
        String ls = System.lineSeparator();
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "firstLinePackage.txt";
        String result = assertDoesNotThrow(() ->
            { return FileUtil.readFile(filePath, StandardCharsets.UTF_8); });
        String expectedResult = "package pkg.test.package;" + ls + 
            "more text" + ls + "more text2" + ls + "more text3";
        assertEquals(expectedResult, result);
    }

    @Test
    public void readFileTest3() {
        // File is a regular text, incorrect encoding
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "firstLinePackage.txt";
        assertDoesNotThrow(() -> 
            FileUtil.readFile(filePath, StandardCharsets.UTF_16));
    }

    @Test
    public void readFileTest4() {
        // File is a binary
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "ConvertRsfToGexf$1.class";
        assertDoesNotThrow(() -> 
            FileUtil.readFile(filePath, StandardCharsets.UTF_16));
    }

    @Test
    public void readFileTest5() {
        // File does not exist
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "nonExist.txt";
        assertThrows(IOException.class, () ->
            FileUtil.readFile(filePath, StandardCharsets.UTF_16));
    }
    // #endregion TESTS readFile -----------------------------------------------

    // #region TESTS getPackageNameFromJavaFile --------------------------------
    @Test
    public void getPackageNameFromJavaFileTest1() {
        // File does not contain any package names
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "textFile.txt";
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals(null, result);
    }

    @Test
    public void getPackageNameFromJavaFileTest2() {
        // File contains a package name on line 1
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "firstLinePackage.txt";
        String result = assertDoesNotThrow(() -> 
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void getPackageNameFromJavaFileTest3() {
        // File contains a package name on line 4
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "fourthLinePackage.txt";
        String result = assertDoesNotThrow(() ->
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void getPackageNameFromJavaFileTest4() {
        // File contains multiple package names on different lines
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "manyPackages.txt";
        String result = assertDoesNotThrow(() ->
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void getPackageNameFromJavaFileTest5() {
        // File contains multiple package names on the same line
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "sameLinePackages.txt";
        String result = assertDoesNotThrow(() ->
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals("package.number.one", result);
    }

    @Test
    public void getPackageNameFromJavaFileTest6() {
        // File is empty
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "empty.txt";
        String result = assertDoesNotThrow(() ->
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals(null, result);
    }

    @Test
    public void getPackageNameFromJavaFileTest7() {
        // File is a binary
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "ConvertRsfToGexf$1.class";
        String result = assertDoesNotThrow(() ->
            { return FileUtil.getPackageNameFromJavaFile(filePath); });
        assertEquals(null, result);
    }

    @Test
    public void getPackageNameFromJavaFileTest8() {
        // File does not exist
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "nonExist.txt";
        assertThrows(IOException.class, () -> 
            FileUtil.getPackageNameFromJavaFile(filePath));
    }
    // #endregion TESTS getPackageNameFromJavaFile -----------------------------

    // #region TESTS findPackageName -------------------------------------------
    @Test
    public void findPackageNameTest1() {
        // Input String is well-formed
        String toTest = "package pkg.test.package;";
        String result = FileUtil.findPackageName(toTest);
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void findPackageNameTest2() {
        // Input String contains multiple packages
        String toTest = "package pkg.test.package; package more.packages;";
        String result = FileUtil.findPackageName(toTest);
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void findPackageNameTest3() {
        // Input String contains no packages
        String toTest = "no packages here";
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }

    @Test
    public void findPackageNameTest4() {
        // Input String is empty
        String toTest = "";
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }

    @Test
    public void findPackageNameTest5() {
        // Input String is null
        String toTest = null;
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }

    @Test
    public void findPackageNameTest6() {
        // Input String is malformed
        String toTest = "package";
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }
    // #endregion TESTS findPackageName ----------------------------------------

    // #region TESTS tildeExpandPath -------------------------------------------
    @Test
    public void tildeExpandPathTest1() {
        // Input String is well-formed
        String fs = File.separator;
        String pathAppend = "path" + fs + "is" + fs + "valid";
        String toTest = "~" + fs + pathAppend;
        String expctdResult = System.getProperty("user.home") + fs + pathAppend;
        String result = FileUtil.tildeExpandPath(toTest);
        assertEquals(expctdResult, result);
    }

    @Test
    public void tildeExpandPathTest2() {
        // Input String is malformed
        String fs = File.separator;
        String pathAppend = "path" + fs + "is" + fs + "valid";
        String toTest = fs + "~" + fs + pathAppend;
        String result = FileUtil.tildeExpandPath(toTest);
        assertEquals(toTest, result);
    }
    // #endregion TESTS tildeExpandPath ----------------------------------------

    // #region TESTS sortFileListByVersion -------------------------------------
    @Test
    public void sortFileListByVersionTest1() {
        // Input is random files
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources";
        File inputDir = new File(dirPath);
        
        List<File> testList = Arrays.asList(inputDir.listFiles());
        List<File> resultList = FileUtil.sortFileListByVersion(testList);
        assertEquals(testList, resultList);
    }

    @Test
    public void sortFileListByVersionTest2() {
        // Input is files with same versioning scheme
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "sameVersionScheme";

        List<File> expectedList = new ArrayList<>();
        expectedList.add(new File(dirPath + fs + "systemfile-0.3.5"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.2.3"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.3.5"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.3.6"));
        expectedList.add(new File(dirPath + fs + "systemfile-2.5.4"));
        
        List<File> testList = new ArrayList<>();
        testList.add(new File(dirPath + fs + "systemfile-0.3.5"));
        testList.add(new File(dirPath + fs + "systemfile-2.5.4"));
        testList.add(new File(dirPath + fs + "systemfile-1.3.5"));
        testList.add(new File(dirPath + fs + "systemfile-1.2.3"));
        testList.add(new File(dirPath + fs + "systemfile-1.3.6"));

        List<File> resultList = FileUtil.sortFileListByVersion(testList);
        assertEquals(expectedList, resultList);
    }

    @Test
    public void sortFileListByVersionTest3() {
        // Input is files with different versioning scheme
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";

        List<File> expectedList = new ArrayList<>();
        expectedList.add(new File(dirPath + fs + "systemfile-0.3.5"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.2.3"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.3"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.3.5"));
        expectedList.add(new File(dirPath + fs + "systemfile-2.5.4.10"));
        
        List<File> testList = new ArrayList<>();
        testList.add(new File(dirPath + fs + "systemfile-2.5.4.10"));
        testList.add(new File(dirPath + fs + "systemfile-1.3"));
        testList.add(new File(dirPath + fs + "systemfile-1.2.3"));
        testList.add(new File(dirPath + fs + "systemfile-0.3.5"));
        testList.add(new File(dirPath + fs + "systemfile-1.3.5"));

        List<File> resultList = FileUtil.sortFileListByVersion(testList);
        assertEquals(expectedList, resultList);
    }

    @Test
    public void sortFileListByVersionTest4() {
        // Input is files with one-item versioning scheme
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "oneItemVersionScheme";

        List<File> expectedList = new ArrayList<>();
        expectedList.add(new File(dirPath + fs + "systemfile-1"));
        expectedList.add(new File(dirPath + fs + "systemfile-3"));
        expectedList.add(new File(dirPath + fs + "systemfile-4"));
        
        List<File> testList = new ArrayList<>();
        testList.add(new File(dirPath + fs + "systemfile-3"));
        testList.add(new File(dirPath + fs + "systemfile-1"));
        testList.add(new File(dirPath + fs + "systemfile-4"));

        List<File> resultList = FileUtil.sortFileListByVersion(testList);
        assertEquals(expectedList, resultList);
    }

    @Test
    public void sortFileListByVersionTest5() {
        // Input is files with two-item versioning scheme
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "twoItemVersionScheme";

        List<File> expectedList = new ArrayList<>();
        expectedList.add(new File(dirPath + fs + "systemfile-0.3"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.3"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.5"));
        expectedList.add(new File(dirPath + fs + "systemfile-2.3"));
        
        List<File> testList = new ArrayList<>();
        testList.add(new File(dirPath + fs + "systemfile-1.3"));
        testList.add(new File(dirPath + fs + "systemfile-1.5"));
        testList.add(new File(dirPath + fs + "systemfile-2.3"));
        testList.add(new File(dirPath + fs + "systemfile-0.3"));

        List<File> resultList = FileUtil.sortFileListByVersion(testList);
        assertEquals(expectedList, resultList);
    }

    @Test
    public void sortFileListByVersionTest6() {
        // Input is files with letters in versioning scheme
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "letteredVersionScheme";

        List<File> expectedList = new ArrayList<>();
        expectedList.add(new File(dirPath + fs + "systemfile-1.2a"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.2b"));
        expectedList.add(new File(dirPath + fs + "systemfile-1.3"));
        expectedList.add(new File(dirPath + fs + "systemfile-2.1a"));
        
        List<File> testList = new ArrayList<>();
        testList.add(new File(dirPath + fs + "systemfile-1.2b"));
        testList.add(new File(dirPath + fs + "systemfile-1.2a"));
        testList.add(new File(dirPath + fs + "systemfile-2.1a"));
        testList.add(new File(dirPath + fs + "systemfile-1.3"));

        List<File> resultList = FileUtil.sortFileListByVersion(testList);
        assertEquals(expectedList, resultList);
    }
    // #endregion TESTS sortFileListByVersion ----------------------------------

    // #region TESTS extractVersion --------------------------------------------
    @Test
    public void extractVersionTest1() {
        // Input is a three-point version
        String input = "2.3.10";
        String result = FileUtil.extractVersion(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionTest2() {
        // Input is a one-point version
        String input = "2";
        String result = FileUtil.extractVersion(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionTest3() {
        // Input is a lettered version
        String input = "2.3a";
        String result = FileUtil.extractVersion(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionTest4() {
        // Input is a filename without a version
        String input = "systemfile";
        String result = FileUtil.extractVersion(input);
        assertEquals(null, result);
    }

    @Test
    public void extractVersionTest5() {
        // Input is a filename with a version
        String input = "systemfile-1.3";
        String result = FileUtil.extractVersion(input);
        assertEquals("1.3", result);
    }

    @Test
    public void extractVersionTest6() {
        // Input is a filepath with a version
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "letteredVersionScheme";
        String input = dirPath + fs + "systemfile-2.5";
        String result = FileUtil.extractVersion(input);
        assertEquals("2.5", result);
    }
    // #endregion TESTS extractVersion -----------------------------------------

    // #region TESTS extractVersionFromFilename --------------------------------
    @Test
    public void extractVersionFromFilenameTest1() {
        // Input is a three-point version
        String input = "2.3.10";
        String scheme = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionFromFilenameTest2() {
        // Input is a one-point version
        String input = "2";
        String scheme = "[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionFromFilenameTest3() {
        // Input is a lettered version
        String input = "2.3a";
        String scheme = "[0-9]+[a-zA-Z]*(\\.[0-9]+[a-zA-Z]*)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionFromFilenameTest4() {
        // Input is a filename without a version
        String input = "systemfile";
        String scheme = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals("", result);
    }

    @Test
    public void extractVersionFromFilenameTest5() {
        // Input is a filename with a version
        String input = "systemfile-1.3";
        String scheme = "[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals("1.3", result);
    }

    @Test
    public void extractVersionFromFilenameTest6() {
        // Input is a filepath with a version
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "letteredVersionScheme";
        String input = dirPath + fs + "systemfile-2.5";
        String scheme = "[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals("2.5", result);
    }
    // #endregion TESTS extractVersionFromFilename -----------------------------

    // #region TESTS checkFile -------------------------------------------------
    @Test
    public void checkFileTest1() {
        // Input is an existing file, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, false, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest2() {
        // Input is an existing file, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, true, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest3() {
        // Input is an existing file, create = false, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, false, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest4() {
        // Input is an existing file, create = true, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, true, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest5() {
        // Input is not an existing file, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        File result = FileUtil.checkFile(path, false, false);
        assertFalse(result.exists());
    }

    @Test
    public void checkFileTest6() {
        // Input is not an existing file, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        File result = FileUtil.checkFile(path, true, false);
        assertTrue(result.exists());
        result.delete();
    }

    @Test
    @ExpectSystemExit
    public void checkFileTest7() {
        // Input is not an existing file, create = false, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        FileUtil.checkFile(path, false, true);
    }

    @Test
    public void checkFileTest8() {
        // Input is not an existing file, create = true, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        File result = FileUtil.checkFile(path, true, true);
        assertTrue(result.exists());
        result.delete();
    }
    // #endregion TESTS checkFile ----------------------------------------------

    // #region TESTS checkDir --------------------------------------------------
    @Test
    public void checkDirTest1() {
        // Input is an existing directory, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, false, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest2() {
        // Input is an existing directory, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, true, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest3() {
        // Input is an existing directory, create = false, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, false, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest4() {
        // Input is an existing directory, create = true, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, true, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest5() {
        //Input not an existing directory, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        File result = FileUtil.checkDir(path, false, false);
        assertFalse(result.exists());
    }

    @Test
    public void checkDirTest6() {
        // Input not an existing directory, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        File result = FileUtil.checkDir(path, true, false);
        assertTrue(result.exists());
        result.delete();
    }

    @Test
    @ExpectSystemExit
    public void checkDirTest7() {
        // Input not an existing directory, create = false, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        FileUtil.checkDir(path, false, true);
    }

    @Test
    public void checkDirTest8() {
        // Input not an existing directory, create = true, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        File result = FileUtil.checkDir(path, true, true);
        assertTrue(result.exists());
        result.delete();
    }
    // #endregion TESTS checkDir -----------------------------------------------

    // #region TESTS extractVersionPretty --------------------------------------
    @Test
    public void extractVersionPrettyTest1() {
        // Input is a three-point version
        String input = "2.3.10";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionPrettyTest2() {
        // Input is a one-point version
        String input = "2";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionPrettyTest3() {
        // Input is a lettered version
        String input = "2.3a";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionPrettyTest4() {
        // Input is a filename without a version
        String input = "systemfile";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(null, result);
    }

    @Test
    public void extractVersionPrettyTest5() {
        // Input is a filename with a version
        String input = "systemfile-1.3";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals("1.3", result);
    }

    @Test
    public void extractVersionPrettyTest6() {
        // Input is a filepath with a version
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "letteredVersionScheme";
        String input = dirPath + fs + "systemfile-2.5";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals("2.5", result);
    }
    // #endregion TESTS extractVersionPretty -----------------------------------

    // #region TESTS collectionToString ----------------------------------------
    @Test
    public void collectionToStringTest1() {
        // Input is a valid cluster file
        String fs = File.separator;
        String filePath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "orca-sim-1.0_cluster.rsf";
        List<String> inputList = assertDoesNotThrow(() ->
            { return Files.readAllLines(
                Paths.get(filePath), StandardCharsets.UTF_8); });
        assertDoesNotThrow(() ->
            { return FileUtil.collectionToString(inputList); });
    }
    // #endregion TESTS collectionToString -------------------------------------
}