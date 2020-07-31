package edu.usc.softarch.arcade.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.apache.log4j.Logger;

public class FileUtilTest {
    // #region FIELDS ----------------------------------------------------------
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    static Logger logger = Logger.getLogger(FileUtilTest.class);
    // #endregion FIELDS -------------------------------------------------------

    // #region TESTS extractFilenamePrefix -------------------------------------
    @Test
    public void testExtractFilenamePrefix1() { //TODO Pass
        // Simple filename test
        String test = "fileName.suffix";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void testExtractFilenamePrefix2() { //TODO Fail
        // Filename without suffix
        String test = "fileName";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void testExtractFilenamePrefix3() { //TODO Pass
        // Filename inside directory, relative path, Windows format
        char fs = File.separatorChar;
        String test = "directory" + fs + "fileName.suffix";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void testExtractFilenamePrefix4() { //TODO Pass
        // Filename inside directory, absolute path, Unix format
        char fs = File.separatorChar;
        String test = fs + "dir1" + fs + "dir2" + fs + "dir3" 
            + fs + "fileName.suffix";
        String result = FileUtil.extractFilenamePrefix(test);
        assertEquals("fileName", result);
    }

    @Test
    public void testExtractFilenamePrefix5() { //TODO Pass
        // File has suffix
        char fs = File.separatorChar;
        String test = "." + fs + "src" + fs + "test" + fs + "resources"
            + fs + "FileUtilTest_resources" + fs + "empty.txt";
        File input = new File(test);
        String result = FileUtil.extractFilenamePrefix(input);
        assertEquals("empty", result);
    }

    @Test
    public void testExtractFilenamePrefix6() { //TODO Pass
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
    public void testExtractFilenameSuffix1() { //TODO Pass
        // Simple filename test
        String test = "fileName.suffix";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals(".suffix", result);
    }

    @Test
    public void testExtractFilenameSuffix2() { //TODO Fail
        // Filename without suffix
        String test = "fileName";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals("", result);
    }

    @Test
    public void testExtractFilenameSuffix3() { //TODO Pass
        // Filename inside directory, relative path, Windows format
        char fs = File.separatorChar;
        String test = "directory" + fs + "fileName.suffix";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals(".suffix", result);
    }

    @Test
    public void testExtractFilenameSuffix4() { //TODO Pass
        // Filename inside directory, absolute path, Unix format
        char fs = File.separatorChar;
        String test = fs + "dir1" + fs + "dir2" + fs + "dir3" 
            + fs + "fileName.suffix";
        String result = FileUtil.extractFilenameSuffix(test);
        assertEquals(".suffix", result);
    }

    @Test
    public void testExtractFilenameSuffix5() { //TODO Fail
        // File has suffix
        char fs = File.separatorChar;
        String test = "." + fs + "src" + fs + "test" + fs + "resources"
            + fs + "FileUtilTest_resources" + fs + "empty.txt";
        File input = new File(test);
        String result = FileUtil.extractFilenameSuffix(input);
        assertEquals(".txt", result);
    }

    @Test
    public void testExtractFilenameSuffix6() { //TODO Pass
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
    public void testReadFile1() { //TODO Pass
        // File is empty
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "empty.txt";
        try {
            String result = FileUtil.readFile(filePath, StandardCharsets.UTF_8);
            assertEquals("", result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testReadFile2() { //TODO Pass
        // File is a regular text, correct encoding
        char fs = File.separatorChar;
        String ls = System.lineSeparator();
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "firstLinePackage.txt";
        try {
            String result = FileUtil.readFile(filePath, StandardCharsets.UTF_8);
            String expectedResult = "package pkg.test.package;" + ls + 
                "more text" + ls + "more text2" + ls + "more text3";
            assertEquals(expectedResult, result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testReadFile3() { //TODO Pass, but make it fail somehow?
        // File is a regular text, incorrect encoding
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "firstLinePackage.txt";
        try {
            FileUtil.readFile(filePath, StandardCharsets.UTF_16);
            assertTrue(true);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testReadFile4() { //TODO Pass, but make it fail somehow?
        // File is a binary
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "ConvertRsfToGexf$1.class";
        try {
            FileUtil.readFile(filePath, StandardCharsets.UTF_16);
            assertTrue(true);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testReadFile5() { //TODO Pass, but make it fail somehow?
        // File does not exist
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "nonExist.txt";
        try {
            FileUtil.readFile(filePath, StandardCharsets.UTF_16);
            assertTrue(false);
        } catch(IOException e) {
            assertTrue(true);
        }
    }
    // #endregion TESTS readFile -----------------------------------------------

    // #region TESTS getPackageNameFromJavaFile --------------------------------
    @Test
    public void testGetPackageNameFromJavaFile1() { //TODO Pass
        // File does not contain any package names
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "textFile.txt";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals(null, result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile2() { //TODO Pass
        // File contains a package name on line 1
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "firstLinePackage.txt";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals("pkg.test.package", result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile3() { //TODO Fail
        // File contains a package name on line 4
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "fourthLinePackage.txt";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals("pkg.test.package", result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile4() { //TODO Pass
        // File contains multiple package names on different lines
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "manyPackages.txt";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals("pkg.test.package", result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile5() { //TODO Fail
        // File contains multiple package names on the same line
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "sameLinePackages.txt";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals("package.number.one", result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile6() { //TODO Pass
        // File is empty
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "empty.txt";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals(null, result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile7() { //TODO Pass
        // File is a binary
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "ConvertRsfToGexf$1.class";
        try {
            String result = FileUtil.getPackageNameFromJavaFile(filePath);
            assertEquals(null, result);
        } catch(IOException e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    @Test
    public void testGetPackageNameFromJavaFile8() { //TODO Pass
        // File does not exist
        char fs = File.separatorChar;
        String filePath = "." + fs + "src" + fs + "test" + fs + "resources" +
            fs + "FileUtilTest_resources" + fs + "nonExist.txt";
        try {
            FileUtil.getPackageNameFromJavaFile(filePath);
            assertTrue(false);
        } catch(IOException e) {
            assertTrue(true);
        }
    }
    // #endregion TESTS getPackageNameFromJavaFile -----------------------------

    // #region TESTS findPackageName -------------------------------------------
    @Test
    public void testFindPackageName1() { //TODO Pass
        // Input String is well-formed
        String toTest = "package pkg.test.package;";
        String result = FileUtil.findPackageName(toTest);
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void testFindPackageName2() { //TODO Fail
        // Input String contains multiple packages
        String toTest = "package pkg.test.package; package more.packages;";
        String result = FileUtil.findPackageName(toTest);
        assertEquals("pkg.test.package", result);
    }

    @Test
    public void testFindPackageName3() { //TODO Pass
        // Input String contains no packages
        String toTest = "no packages here";
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }

    @Test
    public void testFindPackageName4() { //TODO Pass
        // Input String is empty
        String toTest = "";
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }

    @Test
    public void testFindPackageName5() { //TODO Fail
        // Input String is null
        String toTest = null;
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }

    @Test
    public void testFindPackageName6() { //TODO Pass
        // Input String is malformed
        String toTest = "package";
        String result = FileUtil.findPackageName(toTest);
        assertEquals(null, result);
    }
    // #endregion TESTS findPackageName ----------------------------------------

    // #region TESTS tildeExpandPath -------------------------------------------
    @Test
    public void testTildeExpandPath1() { //TODO Pass
        // Input String is well-formed
        String fs = File.separator;
        String pathAppend = "path" + fs + "is" + fs + "valid";
        String toTest = "~" + fs + pathAppend;
        String expctdResult = System.getProperty("user.home") + fs + pathAppend;
        String result = FileUtil.tildeExpandPath(toTest);
        assertEquals(expctdResult, result);
    }

    @Test
    public void testTildeExpandPath2() { //TODO Pass
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
    public void testSortFileListByVersion1() { //TODO Fail
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
    public void testSortFileListByVersion2() { //TODO Pass
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
    public void testSortFileListByVersion3() { //TODO Pass
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
    public void testSortFileListByVersion4() { //TODO Fail
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
    public void testSortFileListByVersion5() { //TODO Pass
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
    public void testSortFileListByVersion6() { //TODO Fail
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
    public void extractVersionTest1() { //TODO Pass
        // Input is a three-point version
        String input = "2.3.10";
        String result = FileUtil.extractVersion(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionTest2() { //TODO Fail
        // Input is a one-point version
        String input = "2";
        String result = FileUtil.extractVersion(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionTest3() { //TODO Fail
        // Input is a lettered version
        String input = "2.3a";
        String result = FileUtil.extractVersion(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionTest4() { //TODO Pass
        // Input is a filename without a version
        String input = "systemfile";
        String result = FileUtil.extractVersion(input);
        assertEquals(null, result);
    }

    @Test
    public void extractVersionTest5() { //TODO Pass
        // Input is a filename with a version
        String input = "systemfile-1.3";
        String result = FileUtil.extractVersion(input);
        assertEquals("1.3", result);
    }

    @Test
    public void extractVersionTest6() { //TODO Pass
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
    public void extractVersionFromFilenameTest1() { //TODO Pass
        // Input is a three-point version
        String input = "2.3.10";
        String scheme = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionFromFilenameTest2() { //TODO Pass
        // Input is a one-point version
        String input = "2";
        String scheme = "[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionFromFilenameTest3() { //TODO Pass
        // Input is a lettered version
        String input = "2.3a";
        String scheme = "[0-9]+[a-zA-Z]*(\\.[0-9]+[a-zA-Z]*)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionFromFilenameTest4() { //TODO Pass
        // Input is a filename without a version
        String input = "systemfile";
        String scheme = "[0-9]+\\.[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals("", result);
    }

    @Test
    public void extractVersionFromFilenameTest5() { //TODO Pass
        // Input is a filename with a version
        String input = "systemfile-1.3";
        String scheme = "[0-9]+(\\.[0-9]+)*";
        String result = FileUtil.extractVersionFromFilename(scheme, input);
        assertEquals("1.3", result);
    }

    @Test
    public void extractVersionFromFilenameTest6() { //TODO Pass
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
    public void checkFileTest1() { //TODO Pass
        // Input is an existing file, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, false, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest2() { //TODO Pass
        // Input is an existing file, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, true, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest3() { //TODO Pass
        // Input is an existing file, create = false, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, false, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest4() { //TODO Pass
        // Input is an existing file, create = true, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "empty.txt";
        File result = FileUtil.checkFile(path, true, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkFileTest5() { //TODO Pass
        // Input is not an existing file, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        File result = FileUtil.checkFile(path, false, false);
        assertFalse(result.exists());
    }

    @Test
    public void checkFileTest6() { //TODO Pass
        // Input is not an existing file, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        File result = FileUtil.checkFile(path, true, false);
        assertTrue(result.exists());
        result.delete();
    }

    @Test
    public void checkFileTest7() { //TODO Pass
        // Input is not an existing file, create = false, exitOnNoExist = true
        exit.expectSystemExit();
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExist.txt";
        FileUtil.checkFile(path, false, true);
    }

    @Test
    public void checkFileTest8() { //TODO Pass
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
    public void checkDirTest1() { //TODO Pass
        // Input is an existing directory, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, false, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest2() { //TODO Pass
        // Input is an existing directory, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, true, false);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest3() { //TODO Pass
        // Input is an existing directory, create = false, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, false, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest4() { //TODO Pass
        // Input is an existing directory, create = true, exitOnNoExist = true
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "differentVersionScheme";
        File result = FileUtil.checkDir(path, true, true);
        assertTrue(result.exists());
    }

    @Test
    public void checkDirTest5() { //TODO Pass
        //Input not an existing directory, create = false, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        File result = FileUtil.checkDir(path, false, false);
        assertFalse(result.exists());
    }

    @Test
    public void checkDirTest6() { //TODO Pass
        // Input not an existing directory, create = true, exitOnNoExist = false
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        File result = FileUtil.checkDir(path, true, false);
        assertTrue(result.exists());
        result.delete();
    }

    @Test
    public void checkDirTest7() { //TODO Pass
        // Input not an existing directory, create = false, exitOnNoExist = true
        exit.expectSystemExit();
        String fs = File.separator;
        String path = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "nonExistDir";
        FileUtil.checkDir(path, false, true);
    }

    @Test
    public void checkDirTest8() { //TODO Fail
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
    public void extractVersionPrettyTest1() { //TODO Pass
        // Input is a three-point version
        String input = "2.3.10";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionPrettyTest2() { //TODO Fail
        // Input is a one-point version
        String input = "2";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionPrettyTest3() { //TODO Fail
        // Input is a lettered version
        String input = "2.3a";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(input, result);
    }

    @Test
    public void extractVersionPrettyTest4() { //TODO Pass
        // Input is a filename without a version
        String input = "systemfile";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals(null, result);
    }

    @Test
    public void extractVersionPrettyTest5() { //TODO Pass
        // Input is a filename with a version
        String input = "systemfile-1.3";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals("1.3", result);
    }

    @Test
    public void extractVersionPrettyTest6() { //TODO Pass
        // Input is a filepath with a version
        String fs = File.separator;
        String dirPath = "src" + fs + "test" + fs + "resources" + fs +
            "FileUtilTest_resources" + fs + "letteredVersionScheme";
        String input = dirPath + fs + "systemfile-2.5";
        String result = FileUtil.extractVersionPretty(input);
        assertEquals("2.5", result);
    }
    // #endregion TESTS extractVersionPretty -----------------------------------
}