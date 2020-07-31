package edu.usc.softarch.arcade.antipattern.detection.codesmell.jArch;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.usc.softarch.arcade.util.FileListing;
import edu.usc.softarch.arcade.util.FileUtil;

public class ProjectMetricsProcessing_ExportJSON {
	private static Logger logger = org.apache.logging.log4j.LogManager.getLogger(ProjectMetricsProcessing_ExportJSON.class);
	// Define XML TAGs
	public static String PKGNAME;
	
	public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
		String projectMetrics = "F:\\projects";

		final File projectMetricsDir = FileUtil.checkDir(projectMetrics, false, false);

		List<File> fileList = FileListing.getFileListing(projectMetricsDir);
		fileList = FileUtil.sortFileListByVersion(fileList);
		final Set<File> cloneFiles = new LinkedHashSet<>();
		for (final File file : fileList) {
			if (file.getName().endsWith(".xml")) {
				cloneFiles.add(file);
			}
		}
		
		final String versionSchemeExpr = "[0-9]+\\.[0-9]+(\\.[0-9]+)*+(-|\\.)*((RC|ALPHA|BETA|M|Rc|Alpha|Beta|rc|alpha|beta)[0-9])*";
		
		Map<String, String> versions = new LinkedHashMap<>();
		for (final File file : cloneFiles) {
			logger.debug(file.getName());
			final String version = FileUtil.extractVersionFromFilename(versionSchemeExpr, file.getName());
			assert !version.equals("") : "Could not extract version";
			versions.put(version, file.getAbsolutePath());
		}
		
		for (String key : versions.keySet()){
			String xmlFilePath = versions.get(key);
			//Handle by DOM Parser
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(true);
			factory.setIgnoringElementContentWhitespace(true);

			DocumentBuilder builder = factory.newDocumentBuilder();
			File file = new File(xmlFilePath);
			Document doc = builder.parse(file);
			System.out.println(doc.getElementsByTagName("Row").getLength());

			// Read the contents
			logger.info("Started processing XML input");
			// Building class method mapping
			Node vals = doc.getElementsByTagName("Row").item(0);
			Element eVals = (Element) vals;
			for (int k = 0; k < eVals.getElementsByTagName("Val").getLength(); k++){
				Node node = eVals.getElementsByTagName("Val").item(k);
				logger.debug("\nCurrent Element :" + ((Element) node).getTextContent());
			}
			System.out.println();
		}
	}
}
