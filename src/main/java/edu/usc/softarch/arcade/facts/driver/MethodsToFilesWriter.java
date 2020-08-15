package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.tartarus.snowball.SnowballStemmer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MethodsToFilesWriter {
	private static Logger logger = Logger.getLogger(MethodsToFilesWriter.class);

	public static void main(String[] args) {
		PropertyConfigurator.configure("cfg" + File.separator + "extractor_logging.cfg");
		
		String fileName = args[0];
		String langKeywordsFilename = args[1];
		String malletInstancesFilename = args[2];
		
		Map<String,String> methodToContentMap = new HashMap<>();
		
		Set<String> langKeywords = new HashSet<>();
		try (BufferedReader in = new BufferedReader(new FileReader(langKeywordsFilename))) {
			String word = null;
			while ((word = in.readLine()) != null) {
				word = word.trim();
				langKeywords.add(word);
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		extractMethodInfo(fileName, methodToContentMap);
		Charset charset = StandardCharsets.UTF_8;
		Path path = Paths.get(malletInstancesFilename);

		try (BufferedWriter out = Files.newBufferedWriter(path, charset)) {
			for (Entry<String, String> entry : methodToContentMap.entrySet()) {
				String methodNameNoSpaces = entry.getKey();
				String methodContentClean = entry.getValue();

				methodContentClean = methodContentClean.replaceAll(
						"[^A-Za-z0-9]", " "); // remove any non-alphanumeric
												// characters
				methodContentClean = methodContentClean.replaceAll("\\s+", " "); // replace
																					// multiple
																					// white
																					// space
																					// with
																					// single
																					// space
				String[] methodContentCleanArray = methodContentClean
						.split(" ");
				methodContentClean = "";
				for (String word : methodContentCleanArray) {
					if (!langKeywords.contains(word)) { // add word if it is not
														// a PL keyword
						methodContentClean += " " + word;
					}
				}
				String[] methodContentCamelCaseSplitArray = methodContentClean
						.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"); // split
																				// over
																				// camel
																				// case
				methodContentClean = StringUtils.join(
						methodContentCamelCaseSplitArray, " ");
				String[] methodContentWordSplitArray = methodContentClean
						.split(" ");

				String methodContentStemmed = "";

				for (String word : methodContentWordSplitArray) {
					Class stemClass = Class
							.forName("org.tartarus.snowball.ext.porterStemmer");
					SnowballStemmer stemmer = (SnowballStemmer) stemClass
							.newInstance();
					stemmer.setCurrent(word);
					stemmer.stem();
					String stemmedWord = stemmer.getCurrent();
					if (stemmedWord.length() == 1) { // do not add words of
														// length 1
						continue;
					}
					methodContentStemmed += " " + stemmedWord;
				}

				String line = methodNameNoSpaces + " X " + methodContentStemmed;
				logger.debug(line);
				out.write(line);
				out.newLine();

			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void extractMethodInfo(String fileName,
			Map<String, String> methodToContentMap) {
		try {

			File fXmlFile = new File(fileName);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			logger.debug("Root element :"
					+ doc.getDocumentElement().getNodeName());
			NodeList nList = doc.getElementsByTagName("class");
			logger.debug("-----------------------");

			int classCounter = 0;
			int methodCounter = 0;
			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element classElement = (Element) nNode;

					if (getTagValue("name", classElement) == null) {
						continue;
					}

					String containerName = getContainerNameOfClassElement(classElement);

					String className = getTagValue("name", classElement);

					logger.debug(classCounter + " - container name: "
							+ containerName);
					logger.debug(classCounter + " - class name: " + className);

					NodeList constructorList = classElement
							.getElementsByTagName("constructor");

					for (int fIndex = 0; fIndex < constructorList.getLength(); fIndex++) {
						Node constructorNode = constructorList
								.item(fIndex);
						if (constructorNode.getNodeType() == Node.ELEMENT_NODE) {
							Element constructorElement = (Element) constructorNode;
							Element nameElement = getChildElementByTagName(
									"name", constructorElement);

							Element paramListElement = getChildElementByTagName(
									"parameter_list", constructorElement);

							String methodNameNoSpaces = prepareMethodNameNoSpaces(
									methodCounter, nameElement,
									paramListElement);

							storeFunctionInfo(methodToContentMap,
									containerName, className,
									constructorElement, methodNameNoSpaces);
						}
						methodCounter++;

					}

					NodeList functionList = classElement
							.getElementsByTagName("function");

					for (int fIndex = 0; fIndex < functionList.getLength(); fIndex++) {
						Node functionNode = functionList.item(fIndex);
						if (functionNode.getNodeType() == Node.ELEMENT_NODE) {
							Element functionElement = (Element) functionNode;
							Element nameElement = getChildElementByTagName(
									"name", functionElement);

							Element paramListElement = getChildElementByTagName(
									"parameter_list", functionElement);

							String methodNameNoSpaces = prepareMethodNameNoSpaces(
									methodCounter, nameElement,
									paramListElement);

							storeFunctionInfo(methodToContentMap,
									containerName, className, functionElement,
									methodNameNoSpaces);
						}
						methodCounter++;
					}

					classCounter++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String prepareMethodNameNoSpaces(int methodCounter,
			Element nameElement, Element paramListElement) {
		String methodName = getElementValue(nameElement) + paramListElement.getTextContent().trim().replaceAll(" +", " ").replaceAll("[\n\t]+","");
		  String methodNameNoSpaces = methodName.replaceAll("\\s", "_");
		  
		  logger.debug("\t" + methodCounter + " - " + methodName);
		  logger.debug("\t" + methodCounter + " - " + methodNameNoSpaces);
		return methodNameNoSpaces;
	}

	private static void storeFunctionInfo(
			Map<String, String> methodToContentMap, String containerName,
			String className, Element functionElement, String methodNameNoSpaces) {
		String methodContent = functionElement.getTextContent();
		  logger.debug("\t\t" + methodContent);
		  
		  String fullMethodNameNoSpaces = containerName + "#" + className + "#" + methodNameNoSpaces;
		  
		  methodToContentMap.put(fullMethodNameNoSpaces, methodContent);
	}
	
	private static String getElementValue(Element element) {
		return element.getChildNodes().item(0).getNodeValue();
	}
	
	private static String getTagValue(String tag, Element element) {
		NodeList nlList = element.getElementsByTagName(tag).item(0)
				.getChildNodes();

		Node nValue = (Node) nlList.item(0);

		return nValue.getNodeValue();
	}
	
	private static String getPackageNameFromPackageElement(Element packageElement) {
		NodeList nodeList = packageElement.getElementsByTagName("name");
		String packageName = "";
		
		for (int i=0;i<nodeList.getLength();i++) {
			Node currNode = nodeList.item(i);
			packageName += currNode.getFirstChild().getNodeValue();
			if (i+1 < nodeList.getLength()) {
				packageName += ".";
			}
		}
		
		return packageName;
	}
	
	private static Element getChildElementByTagName(String tag, Element element) {
		NodeList nodeList = element.getChildNodes();
		
		for (int i=0;i<nodeList.getLength();i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element childElement = (Element)node;
				if (childElement.getNodeName().equals(tag)) {
					return childElement;
				}
			}
		}
		
		return null;
	}
	
	private static String getContainerNameOfClassElement(Element classElement) {
		String containerName = "";
		
		Element currElement = null;
		Node currNode = classElement.getPreviousSibling();
		if (currNode == null) {
			currNode = classElement.getParentNode();
			if (currNode.getNodeType() == Node.ELEMENT_NODE) {
				currElement = (Element) currNode;
				containerName = updateContainerName(containerName,
						currElement);
			}
		}
		
		while (currNode.getNodeType() != Node.ELEMENT_NODE) {
			Node prevNode = currNode;
			currNode = currNode.getPreviousSibling();
			if (currNode == null) {
				currNode = prevNode.getParentNode();
				if (currNode.getNodeType() == Node.ELEMENT_NODE) {
					currElement = (Element) currNode;
					containerName = updateContainerName(containerName,
							currElement);
				}
			}
		}
		currElement = (Element)currNode;
		
		while (!currElement.getNodeName().equals("package")) {
			Node prevNode = currNode;
			currNode = currNode.getPreviousSibling();
			if (currNode == null) {
				currNode = prevNode.getParentNode();
				if (currNode.getNodeType() == Node.ELEMENT_NODE) {
					currElement = (Element) currNode;
					containerName = updateContainerName(containerName,
							currElement);
				}
			}
			else {
				if (currNode.getNodeType() == Node.ELEMENT_NODE) {
					currElement = (Element) currNode;
				}
			}
		}
		
		Element packageElement = currElement;
		String packageName = getPackageNameFromPackageElement(packageElement);
		
		if (containerName != "")
			containerName = packageName + "." + containerName;
		else
			containerName = packageName;
	
		return containerName;
	}

	private static String updateContainerName(String containerName,
			Element currElement) {
		if (currElement.getNodeName().equals("class")) {
			if (containerName == "")
				containerName = getTagValue("name", currElement);
			else
				containerName = getTagValue("name", currElement) + "$" + containerName;
		}
		return containerName;
	}
}
