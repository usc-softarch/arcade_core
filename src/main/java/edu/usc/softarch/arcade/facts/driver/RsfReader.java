package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.usc.softarch.arcade.clustering.FeatureVectorMap;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Language;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.StopWatch;

public class RsfReader {

	private static Logger logger = Logger.getLogger(RsfReader.class);
	public static Set<List<String>> untypedEdgesSet;
	public static Set<String> startNodesSet;
	public static Iterable<List<String>> filteredRoutineFacts;
	public static List<String> filteredRoutines;
	public static Set<String> endNodesSet;
	public static Set<String> allNodesSet;
	public static List<List<String>> unfilteredFacts;

	public static void main(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option loadRsfData = new Option("l", "loads rsf data from deps_rsf_file property in project file");
		Option writeFilteredData = new Option("w", "write filtered rsf data from deps_rsf_file property in project file");

		Option projFile = OptionBuilder.withArgName("file").hasArg()
			.withDescription("project configuration file")
			.create("projfile");

		options.addOption(help);
		options.addOption(projFile);
		options.addOption(loadRsfData);
		options.addOption(writeFilteredData);

		// create the parser
		CommandLineParser parser = new GnuParser();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("projfile")) {
				Config.setProjConfigFilename(line.getOptionValue("projfile"));
			}
			if (line.hasOption("help")) {
				// automatically generate the help statement
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("RsfReader", options);
				System.exit(0);
			}
			
			PropertyConfigurator.configure(Config.getLoggingConfigFilename());

			Config.initConfigFromFile(Config.getProjConfigFilename());
			
			if (line.hasOption("l")) {
				loadRsfDataForCurrProj();
			}
			if (line.hasOption("w")) {
				writeFilteredFactsToFile();
			}
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}		
	}
	
	public static List<List<String>> extractFactsFromRSF(String rsfFilename) {
		// List of facts extracted from RSF File
		List<List<String>> facts = new ArrayList<>();

		logger.debug("Attempting to read " + rsfFilename);
		
		try (BufferedReader in = new BufferedReader(new FileReader(rsfFilename))) {
			String line;

			while ((line = in.readLine()) != null) {
				logger.debug(line);
				
				if (line.trim().isEmpty())
					continue;

				Scanner s = new Scanner(line);
				String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";

				String arcType = s.findInLine(expr);
				String startNode = s.findInLine(expr);
				String endNode = s.findInLine(expr);
				List<String> fact = Arrays.asList(arcType, startNode, endNode);
				logger.debug(fact);
				facts.add(fact);

				if (s.findInLine(expr) != null) {
					logger.error("Found non-triple in file: " + line);
					System.exit(1); //TODO Remove
				}
				s.close();
			}
		} catch (IOException e) {
			e.printStackTrace(); //TODO Treat properly
		}
		return facts;
	}
	
	private static void writeFilteredFactsToFile() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		String rsfFilename = Config.getDepsRsfFilename();

		List<List<String>> facts = extractFactsFromRSF(rsfFilename);

		List<String> factsText = FileUtil.collectionToString(facts);
		logger.debug("Printing stored facts...");
		logger.debug(String.join("\n", factsText));
		
		filteredRoutineFacts = filterRoutinesFromFacts(facts);
		
		// Turn Iterable into Collection
		List<List<String>> filteredRoutineFactsCollection = new ArrayList<>();
		filteredRoutineFacts.forEach(filteredRoutineFactsCollection::add);
		// Turn Collection into List<String>
		List<String> filteredRoutineFactsText =
			FileUtil.collectionToString(filteredRoutineFactsCollection);

		logger.debug("Printing filtered routine facts...");
		logger.debug(String.join("\n", filteredRoutineFactsText));
		
		Iterable<List<String>> filteredDepFacts = filterFacts(facts);

		List<List<String>> filteredDepFactsList =
			FileUtil.iterableToCollection(filteredDepFacts);
		List<String> filteredDepFactsText =
			FileUtil.collectionToString(filteredDepFactsList);
		logger.debug("Printing filtered dependency facts....");
		logger.debug("number of filtered dependency facts: "
				+ filteredDepFactsList.size());
		logger.debug(String.join("\n", filteredDepFactsText));
		
		try {
			writeFactsToFile(filteredRoutineFacts,Config.getFilteredRoutineFactsFilename());
			writeFactsToFile(filteredDepFacts,Config.getFilteredFactsFilename());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		stopWatch.stop();
		logger.debug("Elapsed time in milliseconds: " + stopWatch.getElapsedTime());
	}

	private static void writeFactsToFile(
			Iterable<List<String>> facts, String fileName) throws IOException {
		try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
			for (List<String> fact : facts)
				out.write(fact.get(0) + " " + fact.get(1) + " " + fact.get(2) + "\n");
		}
	}

	public static void loadRsfDataForCurrProj() {
		String rsfFilename = Config.getDepsRsfFilename();
		
		loadRsfDataFromFile(rsfFilename);
	}

	public static void loadRsfDataFromFile(String rsfFilename) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		unfilteredFacts = extractFactsFromRSF(rsfFilename);

		List<String> unfilteredFactsText =
			FileUtil.collectionToString(unfilteredFacts);
		logger.debug("Printing stored facts...");
		logger.debug(String.join("\n", unfilteredFactsText));
		
		filteredRoutineFacts = unfilteredFacts;

		List<List<String>> untypedEdges = convertFactsToUntypedEdges(unfilteredFacts);
		untypedEdgesSet = new HashSet<>(untypedEdges);

		List<String> untypedEdgesText = FileUtil.collectionToString(untypedEdges);
		logger.debug("Printing untyped edges....");
		logger.debug("number of untyped edges as list: " + untypedEdges.size());
		logger.debug("number of untyped edges as set: "	+ untypedEdgesSet.size());
		logger.debug(String.join("\n", untypedEdgesText));

		List<String> startNodesList = convertFactsToStartNodesList(unfilteredFacts);
		HashSet<String> rawStartNodesSet = new HashSet<>(startNodesList);

		List<String> rawStartNodesSetText =
			FileUtil.collectionToString(rawStartNodesSet);
		logger.debug("Printing raw start nodes...");
		logger.debug("number of raw start nodes: " + rawStartNodesSet.size());
		logger.debug(String.join("\n", rawStartNodesSetText));

		List<String> endNodesList = convertFactsToEndNodesList(unfilteredFacts);
		endNodesSet = new HashSet<>(endNodesList);
		
		List<String> endNodesSetText = FileUtil.collectionToString(endNodesSet);
		logger.debug("Printing end nodes...");
		logger.debug("number of end nodes: " + endNodesSet.size());
		logger.debug(String.join("\n", endNodesSetText));
		
		startNodesSet = new TreeSet<>(rawStartNodesSet);

		List<String> startNodesSetText = FileUtil.collectionToString(startNodesSet);
		logger.debug("Printing start nodes...");
		logger.debug("number of start nodes: " + startNodesSet.size());
		logger.debug(String.join("\n", startNodesSetText));
		
		allNodesSet = new HashSet<>(startNodesSet);
		allNodesSet.addAll(endNodesSet);
		
		stopWatch.stop();
		logger.debug("Elapsed time in milliseconds: " + stopWatch.getElapsedTime());
	}

	private static List<String> convertFactsToEndNodesList(
			List<List<String>> filteredFacts) {
		return filteredFacts.stream()
			.map((List<String> fact) -> fact.get(2))
			.collect(Collectors.toList());
	}

	private static List<String> convertFactsToStartNodesList(
			List<List<String>> filteredFacts) {
		return filteredFacts.stream()
			.map((List<String> fact) -> fact.get(1))
			.collect(Collectors.toList());
	}

	private static List<List<String>> convertFactsToUntypedEdges(
			List<List<String>> filteredFacts) {
		return filteredFacts.stream()
			.map((List<String> fact) -> Arrays.asList(fact.get(1), fact.get(2)))
			.collect(Collectors.toList());
	}

	private static Iterable<List<String>> filterRoutinesFromFacts(
			List<List<String>> facts) {
		return () -> facts.stream()
			.filter((List<String> fact) ->
				// Remove any startNode with a / in its name and any
				// actType that is level, lino, type or file
				!fact.get(1).contains("/")
				&& fact.get(0).matches("type")
				&& fact.get(2).matches("\"Routine\"")
			).iterator();
	}

	private static Iterable<List<String>> filterFacts(List<List<String>> facts) {
		return () -> facts.stream()
			.filter((List<String> fact) ->
				// Remove any startNode with a / in its name and any
				// actType that is level, lino, type or file
				!fact.get(1).contains("/")
				&& !fact.get(0).matches("level|lineno|type|file")
			).iterator();
	}

	public static void performPreClusteringTasks()
			throws ParserConfigurationException, TransformerException {
		Config.setSelectedLanguage(Language.c);
		Config.initConfigFromFile(Config.getProjConfigFilename());			
		writeXMLTypedEdgeDepGraph(filteredRoutineFacts);
		TypedEdgeGraph typedEdgeGraph = createFunctionGraph(filteredRoutineFacts);
		logger.debug("typed edge graph size: " + typedEdgeGraph.edges.size());
		FeatureVectorMap fvMap = new FeatureVectorMap(typedEdgeGraph);
		
		fvMap.serializeAsFastFeatureVectors();
	}

	private static TypedEdgeGraph createFunctionGraph(Iterable<List<String>> filteredFacts) {
		TypedEdgeGraph graph = new TypedEdgeGraph();
		for (List<String> fact : filteredFacts) {
			graph.addEdge(fact.get(0), fact.get(1), fact.get(2));
		}
		
		return graph;
	}

	private static void writeXMLTypedEdgeDepGraph(Iterable<List<String>> filteredFacts)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		  DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	 
		  //classgraph elements
		  Document doc = docBuilder.newDocument();
		  Element rootElement = doc.createElement("FunctionDepGraph");
		  doc.appendChild(rootElement);
	 
		  //classedge elements
		  for (List<String> fact : filteredFacts) {
			  Element ce = doc.createElement("edge");
			  rootElement.appendChild(ce);
			  
			  Element arcType = doc.createElement("arcType");
			  arcType.appendChild(doc.createTextNode(fact.get(0)));
			  ce.appendChild(arcType);
			  
			  Element src = doc.createElement("srcNode");
			  src.appendChild(doc.createTextNode(fact.get(1)));
			  ce.appendChild(src);
			  
			  Element tgt = doc.createElement("endNode");
			  tgt.appendChild(doc.createTextNode(fact.get(2)));
			  ce.appendChild(tgt);
		  }
	 
		  //write the content into xml file
		  TransformerFactory transformerFactory = TransformerFactory.newInstance();
		  Transformer transformer = transformerFactory.newTransformer();
		  transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		  DOMSource source = new DOMSource(doc);
		  File xmlFunctionDepGraphFile = new File(Config.getXMLFunctionDepGraphFilename());
		  xmlFunctionDepGraphFile.getParentFile().mkdirs();
		  StreamResult result =  new StreamResult(xmlFunctionDepGraphFile);
		  transformer.transform(source, result);
	 
		  logger.debug("In " + Thread.currentThread().getStackTrace()[1].getClassName() 
				+ ". " + Thread.currentThread().getStackTrace()[1].getMethodName() 
				+ ", Wrote " + Config.getXMLFunctionDepGraphFilename());
	}
}