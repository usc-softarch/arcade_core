package edu.usc.softarch.arcade;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class FunctionFileNameTest {
	
	static Logger logger = Logger.getLogger(FunctionFileNameTest.class);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		String projFile = "cfg/bash_concerns.cfg";
		Config.setProjConfigFilename(projFile);
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
		logger.debug("Logging from " + this.getClass());
		Config.initConfigFromFile(Config.getProjConfigFilename());
		
		
		RsfReader.loadRsfDataForCurrProj();
		String funcFileLocation = "/home/joshua/workspace/cpp_func_separator/output";
		File funcFileDir = new File(funcFileLocation);
		
		FileFilter funcFileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.getName().endsWith(".func");
			}
		};
		
		logger.debug("Removing quotations from filtered routines...");
		List<String> quotelessFilteredRoutines = new ArrayList<String>();
		for (String routine : RsfReader.filteredRoutines) {
			logger.debug("\t original routine: " + routine);
			routine = routine.replaceAll("\"", "");
			quotelessFilteredRoutines.add(routine);
			logger.debug("\t resulting routine: " + routine);
		}
		
		/*logger.debug("Checking if RsfReader.filteredRoutines has changed...");
		for (String routine : RsfReader.filteredRoutines) {
			logger.debug("\t current routine: " + routine);
		}*/
		
		HashSet<String> quotelessFilteredRoutinesSet = Sets.newHashSet(quotelessFilteredRoutines);
		
		logger.debug("Processing func files...");
		for (File funcFile : funcFileDir.listFiles(funcFileFilter)) {
			String funcFileName = funcFile.getName();
			logger.debug(funcFileName);
			String[] tokens = funcFileName.split("#");
			
			int firstOccurrenceOfDotInFilename = funcFileName.indexOf(".func");
			String filenamePrefix = funcFileName.substring(0,firstOccurrenceOfDotInFilename);
			
			String funcNameAndExtension = tokens[2];
			logger.debug("function name and extension: " + funcNameAndExtension);
			int firstOccurrenceOfDot = funcNameAndExtension.indexOf(".");
			String functionNameOnly = funcNameAndExtension.substring(0,firstOccurrenceOfDot);
			if ( firstOccurrenceOfDot > 0)
				logger.debug("\tfunction name only: " +  functionNameOnly);
			else 
				logger.error("\tinvalid function name and extension: " + funcNameAndExtension);
			
			if (!quotelessFilteredRoutinesSet.contains(filenamePrefix)) {
				logger.error("\tfiltered routines does not filtered routine: " + filenamePrefix);
			}	
		}
	}

}
