package edu.usc.softarch.arcade.facts.driver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.usc.softarch.arcade.clustering.FeatureVectorMap;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.config.Config.Language;
import edu.usc.softarch.arcade.functiongraph.TypedEdgeGraph;
import edu.usc.softarch.arcade.util.StopWatch;

public class RsfReader {

	static Logger logger = Logger.getLogger(RsfReader.class);
	public static HashSet<List<String>> untypedEdgesSet;
	public static TreeSet<String> startNodesSet;
	public static Iterable<List<String>> filteredRoutineFacts;
	public static List<String> filteredRoutines;
	public static HashSet<String> endNodesSet;
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
		List<List<String>> facts = Lists.newArrayList();

		boolean local_debug = false;
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(rsfFilename));
			String line;
			int lineCount = 0;
			int limit = 0;
			while ((line = in.readLine()) != null) {
				if (lineCount == limit && limit != 0) {
					break;
				}
				if (local_debug) {
					logger.debug(line);
				}
				
				if (line.trim().isEmpty()) {
					continue;
				}

				Scanner s = new Scanner(line);
				// String expr = "([\"\"'])(?:(?=(\\?))\2.)*?\1|([^\"].*[^\"])";
				// String expr = "^[^\"][^\\s]+[^\"]$";
				// String expr = "([^\"\\s][^\\s]*[^\"\\s])"; // any non
				// whitespace characters without quotes or whitespace at the
				// start or beginning
				// String expr = "([\"][^\"]*[\"])"; // any characters in quotes
				// including the quotes
				String expr = "([^\"\\s][^\\s]*[^\"\\s]*)|([\"][^\"]*[\"])";
				int tokenLimit = 3;

				String arcType = s.findInLine(expr);
				String startNode = s.findInLine(expr);
				String endNode = s.findInLine(expr);
				List<String> fact = Lists.newArrayList(arcType, startNode,
						endNode);
				if (local_debug) {
					logger.debug(fact);
				}
				facts.add(fact);

				if (s.findInLine(expr) != null) {
					logger.error("Found non-triple in file: " + line);
					System.exit(1);
				}

				/*
				 * MatchResult result = s.match(); for (int i=1;
				 * i<=result.groupCount(); i++) logger.debug(i + ": " +
				 * result.group(i)); s.close();
				 */

				lineCount++;
				/*
				 * if (triple.size() != 3) {
				 * logger.error("Found non-triple in file: " + triple);
				 * System.exit(1); } logger.debug(triple);
				 */
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return facts;
	}
	
	public static void writeFilteredFactsToFile() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		//String rsfFilename = "/home/joshua/Documents/Software Engineering Research/projects/recovery/RSFs/bash.rsf";
		//String rsfFilename = "/home/joshua/workspace/MyExtractors/data/test1.rsf";
		String rsfFilename = Config.getDepsRsfFilename();

		List<List<String>> facts = extractFactsFromRSF(rsfFilename);

		logger.debug("Printing stored facts...");
		logger.debug(Joiner.on("\n").join(facts));
		
		
		filteredRoutineFacts = filterRoutinesFromFacts(facts);
		logger.debug("Printing filtered routine facts...");
		logger.debug(Joiner.on("\n").join(filteredRoutineFacts));
		
		Iterable<List<String>> filteredDepFacts = filterFacts(facts);
		

		List<List<String>> filteredDepFactsList = Lists
				.newArrayList(filteredDepFacts);
		logger.debug("Printing filtered dependency facts....");
		logger.debug("number of filtered dependency facts: "
				+ filteredDepFactsList.size());
		logger.debug(Joiner.on("\n").join(filteredDepFacts));
		
		try {
			writeFactsToFile(filteredRoutineFacts,Config.getFilteredRoutineFactsFilename());
			writeFactsToFile(filteredDepFacts,Config.getFilteredFactsFilename());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stopWatch.stop();
		logger.debug("Elapsed time in milliseconds: " + stopWatch.getElapsedTime());
	}

	private static void writeFactsToFile(
			Iterable<List<String>> facts, String fileName) throws IOException {
		FileWriter fstream = new FileWriter(fileName);
		BufferedWriter out = new BufferedWriter(fstream);
		
		for (List<String> fact : facts) {
			out.write(fact.get(0) + " " + fact.get(1) + " " + fact.get(2) + "\n");
		}
		
		out.close();
		
	}
	
	public static void loadRsfDataAndFilter() {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		//String rsfFilename = "/home/joshua/Documents/Software Engineering Research/projects/recovery/RSFs/bash.rsf";
		//String rsfFilename = "/home/joshua/workspace/MyExtractors/data/test1.rsf";
		String rsfFilename = Config.getDepsRsfFilename();

		List<List<String>> facts = extractFactsFromRSF(rsfFilename);

		logger.debug("Printing stored facts...");
		logger.debug(Joiner.on("\n").join(facts));
		
		
		filteredRoutineFacts = filterRoutinesFromFacts(facts);
		logger.debug("Printing filtered routine facts...");
		logger.debug(Joiner.on("\n").join(filteredRoutineFacts));
		
		filteredRoutines = filterRoutinesFromRoutineFacts();
		List<String> sortedFilteredRoutines = new ArrayList<String>(filteredRoutines);
		Collections.sort(sortedFilteredRoutines);
		
		logger.debug("Printing filtered routines...");
		logger.debug("Number of filtered routines: " + sortedFilteredRoutines.size());
		logger.debug(Joiner.on("\n").join(sortedFilteredRoutines));

		Iterable<List<String>> filteredDepFacts = filterFacts(facts);
		

		List<List<String>> filteredDepFactsList = Lists
				.newArrayList(filteredDepFacts);
		logger.debug("Printing filtered dependency facts....");
		logger.debug("number of filtered dependency facts: "
				+ filteredDepFactsList.size());
		logger.debug(Joiner.on("\n").join(filteredDepFacts));

		List<List<String>> untypedEdges = convertFactsToUntypedEdges(filteredDepFacts);

		untypedEdgesSet = Sets.newHashSet(untypedEdges);

		logger.debug("Printing untyped edges....");
		logger.debug("number of untyped edges as list: "
				+ untypedEdges.size());
		logger.debug("number of untyped edges as set: "
				+ untypedEdgesSet.size());
		logger.debug(Joiner.on("\n").join(untypedEdges));

		List<String> startNodesList = convertFactsToStartNodesList(filteredDepFacts);
		
		HashSet<String> rawStartNodesSet = Sets.newHashSet(startNodesList);

		logger.debug("Printing raw start nodes...");
		logger.debug("number of raw start nodes: " + rawStartNodesSet.size());
		logger.debug(Joiner.on("\n").join(rawStartNodesSet));

		List<String> endNodesList = convertFactsToEndNodesList(filteredDepFacts);
		HashSet<String> endNodesSet = Sets.newHashSet(endNodesList);
		
		

		logger.debug("Printing end nodes...");
		logger.debug("number of end nodes: " + endNodesSet.size());
		logger.debug(Joiner.on("\n").join(endNodesSet));
		
		TreeSet<String> sortedFilteredRoutinesSet = Sets.newTreeSet(sortedFilteredRoutines);
		startNodesSet = new TreeSet<String>(rawStartNodesSet);
		startNodesSet.retainAll(sortedFilteredRoutinesSet);
		
		logger.debug("Printing start nodes...");
		logger.debug("number of start nodes: " + startNodesSet.size());
		logger.debug(Joiner.on("\n").join(startNodesSet));
		
		stopWatch.stop();
		logger.debug("Elapsed time in milliseconds: " + stopWatch.getElapsedTime());
	}

	public static void loadRsfDataForCurrProj() {
		//String rsfFilename = "/home/joshua/Documents/Software Engineering Research/projects/recovery/RSFs/bash.rsf";
		//String rsfFilename = "/home/joshua/workspace/MyExtractors/data/test1.rsf";
		String rsfFilename = Config.getDepsRsfFilename();
		
		loadRsfDataFromFile(rsfFilename);
	}

	public static void loadRsfDataFromFile(String rsfFilename) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		unfilteredFacts = extractFactsFromRSF(rsfFilename);
		
		boolean local_debug=false;

		if (local_debug) {
			logger.debug("Printing stored facts...");
			logger.debug(Joiner.on("\n").join(unfilteredFacts));
		}
		
		filteredRoutineFacts = unfilteredFacts;

		List<List<String>> untypedEdges = convertFactsToUntypedEdges(unfilteredFacts);

		untypedEdgesSet = Sets.newHashSet(untypedEdges);

		if (local_debug) {
			logger.debug("Printing untyped edges....");
			logger.debug("number of untyped edges as list: "
					+ untypedEdges.size());
			logger.debug("number of untyped edges as set: "
					+ untypedEdgesSet.size());
			logger.debug(Joiner.on("\n").join(untypedEdges));
		}
		List<String> startNodesList = convertFactsToStartNodesList(unfilteredFacts);
		
		HashSet<String> rawStartNodesSet = Sets.newHashSet(startNodesList);

		if (local_debug) {
			logger.debug("Printing raw start nodes...");
			logger.debug("number of raw start nodes: "
					+ rawStartNodesSet.size());
			logger.debug(Joiner.on("\n").join(rawStartNodesSet));
		}

		List<String> endNodesList = convertFactsToEndNodesList(unfilteredFacts);
		endNodesSet = Sets.newHashSet(endNodesList);
		

		if (local_debug) {
			logger.debug("Printing end nodes...");
			logger.debug("number of end nodes: " + endNodesSet.size());
			logger.debug(Joiner.on("\n").join(endNodesSet));
		}
		
		startNodesSet = new TreeSet<String>(rawStartNodesSet);

		if (local_debug) {
			logger.debug("Printing start nodes...");
			logger.debug("number of start nodes: " + startNodesSet.size());
			logger.debug(Joiner.on("\n").join(startNodesSet));
		}
		
		allNodesSet = new HashSet<String>(startNodesSet);
		allNodesSet.addAll(endNodesSet);
		
		stopWatch.stop();
		logger.debug("Elapsed time in milliseconds: " + stopWatch.getElapsedTime());
	}

	private static List<String> convertFactsToEndNodesList(
			Iterable<List<String>> filteredFacts) {
		return Lists.transform(
				Lists.newArrayList(filteredFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(2);
					}
				});
	}

	private static List<String> convertFactsToStartNodesList(
			Iterable<List<String>> filteredFacts) {
		return Lists.transform(
				Lists.newArrayList(filteredFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(1);
					}
				});
	}

	private static List<List<String>> convertFactsToUntypedEdges(
			Iterable<List<String>> filteredFacts) {
		return Lists.transform(
				Lists.newArrayList(filteredFacts),
				new Function<List<String>, List<String>>() {
					public List<String> apply(List<String> fact) {
						return Lists.newArrayList(fact.get(1), fact.get(2));
					}
				});
	}

	private static List<String> filterRoutinesFromRoutineFacts() {
		return Lists.transform(
				Lists.newArrayList(filteredRoutineFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(1);
					}
				});
	}

	private static Iterable<List<String>> filterRoutinesFromFacts(
			List<List<String>> facts) {
		return Iterables.filter(facts,
				new Predicate<List<String>>() {
			public boolean apply(List<String> fact) {
				// Remove any startNode with a / in its name and any
				// actType that is level, lino, type or file
				return !fact.get(1).contains("/")
						&& fact.get(0).matches("type")
						&& fact.get(2).matches("\"Routine\"");
			}
		});
	}

	private static Iterable<List<String>> filterFacts(List<List<String>> facts) {
		return Iterables.filter(facts,
				new Predicate<List<String>>() {
					public boolean apply(List<String> fact) {
						// Remove any startNode with a / in its name and any
						// actType that is level, lino, type or file
						return !fact.get(1).contains("/")
								&& !fact.get(0).matches(
										"level|lineno|type|file");
					}
				});
	}

	public static void setupLogging() {
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());
	}

	public static void performPreClusteringTasks()
			throws ParserConfigurationException, TransformerException {
		Config.setSelectedLanguage(Language.c);
		Config.initConfigFromFile(Config.getProjConfigFilename());			
		/*writeXMLFunctionDepGraph(filteredFacts);
		FunctionGraph functionGraph = createFunctionGraph(filteredFacts);*/
		writeXMLTypedEdgeDepGraph(filteredRoutineFacts);
		TypedEdgeGraph typedEdgeGraph = createFunctionGraph(filteredRoutineFacts);
		logger.debug("typed edge graph size: " + typedEdgeGraph.edges.size());
		//logger.debug("Printing typed edge graph...");
		//logger.debug(typedEdgeGraph);
		FeatureVectorMap fvMap = new FeatureVectorMap(typedEdgeGraph);
		//fvMap.writeXMLFeatureVectorMapUsingFunctionDepEdges();
		
		fvMap.serializeAsFastFeatureVectors();
	}

	private static TypedEdgeGraph createFunctionGraph(Iterable<List<String>> filteredFacts) {
		TypedEdgeGraph graph = new TypedEdgeGraph();
		for (List<String> fact : filteredFacts) {
			graph.addEdge(fact.get(0), fact.get(1), fact.get(2));
		}
		
		return graph;
		
	}

	public static void writeXMLTypedEdgeDepGraph(Iterable<List<String>> filteredFacts) throws ParserConfigurationException, TransformerException {
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
