package edu.usc.softarch.gexf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import net.gexf._1.DefaultedgetypeType;
import net.gexf._1.EdgeContent;
import net.gexf._1.EdgesContent;
import net.gexf._1.GexfContent;
import net.gexf._1.GraphContent;
import net.gexf._1.MetaContent;
import net.gexf._1.ModeType;
import net.gexf._1.NodeContent;
import net.gexf._1.NodesContent;
import net.gexf._1.ObjectFactory;

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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.ojn.gexf4j.core.EdgeType;
import com.ojn.gexf4j.core.Gexf;
import com.ojn.gexf4j.core.Graph;
import com.ojn.gexf4j.core.Mode;
import com.ojn.gexf4j.core.Node;
import com.ojn.gexf4j.core.impl.GexfImpl;
import com.ojn.gexf4j.core.impl.StaxGraphWriter;

import edu.usc.softarch.arcade.ExpertRecoveryDriver;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.facts.driver.RsfReader;

public class ConvertRsfToGexf {
	static Logger logger = Logger.getLogger(ConvertRsfToGexf.class);
	
	public static void main(String[] args) {
		String containerRsfFilename = "/home/joshua/recovery/Expert Decompositions/linuxFullAuthcontain.rsf";
		String depsFilename = "/home/joshua/recovery/RSFs/linuxRel.rsf";
		String outputGexfFile = "linux_auth_recovery.gexf";
		boolean isStrippingExtensions = false;
		
		Options options = new Options();
		
		Option help = new Option( "help", "print this message" );
		
		Option authFile   = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "authoritative recovery file" )
                .create( "authfile" );
		
		Option depFile  = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "dependencies file" )
                .create( "depfile" );
		
		Option gexfFile  = OptionBuilder.withArgName( "file" )
                .hasArg()
                .withDescription(  "output gexf file" )
                .create( "gexffile" );
		
		Option stripExt = new Option("stripext","strips .h and .c extensions from facts files");
		
		options.addOption(help);
		options.addOption(authFile);
		options.addOption(depFile);
		options.addOption(gexfFile);
		options.addOption(stripExt);
		
		 // create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        
	        if (line.hasOption("authfile")) {
	        	containerRsfFilename = line.getOptionValue("authfile");
	        }
	        if (line.hasOption("depfile")) {
	        	depsFilename = line.getOptionValue("depfile");
	        }
	        if (line.hasOption("gexffile")) {
	        	outputGexfFile = line.getOptionValue("gexffile");
	        }
	        if (line.hasOption("stripext")) {
	        	isStrippingExtensions = true;
	        }
	        if (line.hasOption("help")) {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( ConvertRsfToGexf.class.getName(), options );
	        	System.exit(0);
	        }
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
	    }
		
		PropertyConfigurator.configure(Config.getLoggingConfigFilename());

		Config.initConfigFromFile(Config.getProjConfigFilename());
		
		
		
		
		RsfReader.loadRsfDataFromFile(containerRsfFilename);
		Iterable<List<String>> containerFacts = RsfReader.filteredRoutineFacts;
		
		RsfReader.loadRsfDataFromFile(depsFilename);
		Iterable<List<String>> depsFacts = RsfReader.filteredRoutineFacts;
		
		//transformContaineesToMatchDepNodes(containerFacts, depsFacts);
		
		if (isStrippingExtensions) {
			stripExtensions(containerFacts, depsFacts);
		}
		
		for (List<String> fact : containerFacts) {
			logger.debug(fact);
		}
		
		Set<String> allNodesSet = new HashSet<String>();
		for (List<String> fact : containerFacts) {
			allNodesSet.add(fact.get(1));
			allNodesSet.add(fact.get(2));
		}
		
		List<String> containersList = Lists.transform(
				Lists.newArrayList(containerFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(1);
					}
				});
		
		Set<String> containerSet = new HashSet<String>(containersList);
		
		logger.debug("The containers...");
		logger.debug(Joiner.on("\n").join(containerSet));
		
		Iterables.filter(containerFacts,
				new Predicate<List<String>>() {
			public boolean apply(List<String> fact) {
				// Target is not a .h or .c file
				return !fact.get(2).endsWith(".h")
						|| !fact.get(2).endsWith(".c");
			}
		});
		
		logger.debug("The dependencies...");
		logger.debug(Joiner.on("\n").join(depsFacts));
		
		// attemptToUseGexfJavaLibrary(containerFacts, containerSet);
		try {
			JAXBContext jc = JAXBContext.newInstance(GexfContent.class);

			ObjectFactory objFactory = new ObjectFactory();
			GexfContent gexfContent = objFactory.createGexfContent();
			
			MetaContent metaContent = objFactory.createMetaContent();
			metaContent.setLastmodifieddate(getXMLGregorianCalendarNow());
			metaContent.getCreatorOrKeywordsOrDescription().add(objFactory.createCreator("extractors"));
			metaContent.getCreatorOrKeywordsOrDescription().add(objFactory.createDescription("a hierarchical recovery graph"));
			gexfContent.setMeta(metaContent);
			
			GraphContent graphContent = objFactory.createGraphContent();
			graphContent.setMode(ModeType.STATIC);
			graphContent.setDefaultedgetype(DefaultedgetypeType.DIRECTED);
			NodesContent nodesContent = objFactory.createNodesContent();
			
			graphContent.getAttributesOrNodesOrEdges().add(nodesContent);
			gexfContent.setGraph(graphContent);
			
			// create NodeContent map
			Map<String,NodeContent> nodeContentMap = new HashMap<String,NodeContent>();
			for (String node : allNodesSet) {
				NodeContent nodeContent = objFactory.createNodeContent();
				nodeContent.setId(node);
				nodeContent.setLabel(node);
				nodeContentMap.put(node,nodeContent);
			}
			
			// add child nodes to parents
			Set<String> accountedNodes = new HashSet<String>();
			for (String container : containerSet) {
				for (List<String> fact : containerFacts) {
					if (container.equals(fact.get(2)) && !fact.get(1).equals(fact.get(2))) {
						NodeContent parentNode = nodeContentMap.get(fact.get(1));
						NodeContent childNode = nodeContentMap.get(fact.get(2));
						NodesContent parentNodesContent = getNodesContent(parentNode);
						if (parentNodesContent == null) {
							parentNodesContent = objFactory.createNodesContent();
							parentNode.getAttvaluesOrSpellsOrNodes().add(parentNodesContent);
							
						}
						parentNodesContent.getNode().add(childNode);
						accountedNodes.add(childNode.getId());
						
					}
				}
			}
			
			
			Set<String> remainingContainerNodes = new HashSet<String>(containerSet);
			remainingContainerNodes.removeAll(accountedNodes);
			
			for (String node : remainingContainerNodes) {
				nodesContent.getNode().add(nodeContentMap.get(node));
			}
			
			Set<String> remainingNonContainerNodes = new HashSet<String>(allNodesSet);
			remainingNonContainerNodes.removeAll(accountedNodes);
			remainingNonContainerNodes.removeAll(remainingContainerNodes);
			
			Map<String,List<String>> containerMap = new HashMap<String,List<String>>();
			
			for (List<String> fact : containerFacts) {
				containerMap.put(fact.get(2), fact);
			}
			
			for (String node : remainingNonContainerNodes) {
				List<String> fact = containerMap.get(node);
				NodeContent parentNode = nodeContentMap.get(fact.get(1));
				NodeContent childNode = nodeContentMap.get(fact.get(2));
				NodesContent parentNodesContent = getNodesContent(parentNode);
				if (parentNodesContent == null) {
					parentNodesContent = objFactory.createNodesContent();
					parentNode.getAttvaluesOrSpellsOrNodes().add(parentNodesContent);
					
				}
				parentNodesContent.getNode().add(childNode);
			}
			
			int totalNodeCount = countNodes(nodesContent);
			System.out.println("Total nodes in gexf file: " + totalNodeCount);
			System.out.println("Total nodes from rsf: " + allNodesSet.size());
			
			EdgesContent edgesContent = objFactory.createEdgesContent();
			for (List<String> fact : depsFacts) {
				EdgeContent edgeContent = objFactory.createEdgeContent();
				edgeContent.setSource(fact.get(1));
				edgeContent.setTarget(fact.get(2));
				edgeContent.setLabel(fact.get(0));
				edgesContent.getEdge().add(edgeContent);
			}
			graphContent.getAttributesOrNodesOrEdges().add(edgesContent);

			Marshaller m = jc.createMarshaller();

			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			
			FileOutputStream fos = new FileOutputStream(outputGexfFile);
			m.marshal(gexfContent, fos);
		} catch (PropertyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch blocks
			e.printStackTrace();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void stripExtensions(Iterable<List<String>> containerFacts,
			Iterable<List<String>> depsFacts) {
		for (List<String> fact : containerFacts) {
			String source = fact.get(1);
			String target = fact.get(2);
			source = source.replace(".c", "");
			source = source.replace(".h", "");
			target = target.replace(".c", "");
			target = target.replace(".h", "");
			fact.remove(fact.size()-1);
			fact.remove(fact.size()-1);
			fact.add(source);
			fact.add(target);
		}
		
		for (List<String> fact : depsFacts) {
			String source = fact.get(1);
			String target = fact.get(2);
			source = source.replace(".c", "");
			source = source.replace(".h", "");
			target = target.replace(".c", "");
			target = target.replace(".h", "");
			fact.remove(fact.size()-1);
			fact.remove(fact.size()-1);
			fact.add(source);
			fact.add(target);
		}
	}

	private static void transformContaineesToMatchDepNodes(
			Iterable<List<String>> containerFacts,
			Iterable<List<String>> depsFacts) {
		List<String> containeesList = Lists.transform(
				Lists.newArrayList(containerFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(2);
					}
				});
		
		List<String> depSourceNodes = Lists.transform(
				Lists.newArrayList(depsFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(1);
					}
				});
		
		List<String> depTargetNodes = Lists.transform(
				Lists.newArrayList(depsFacts),
				new Function<List<String>, String>() {
					public String apply(List<String> fact) {
						return fact.get(2);
					}
				});
		List<String> allDepNodesList = new ArrayList<String>(depSourceNodes);
		allDepNodesList.addAll(depTargetNodes);
		
		Set<String> containeesSet = new HashSet<String>(containeesList);
		
		Set<String> allDepNodesSet = new HashSet<String>(allDepNodesList);
		
		for (String containee : containeesSet) {
			for (String depNode : allDepNodesSet) {
				String[] depNodeTokens = depNode.split("/");
				String cutDepNode = depNodeTokens[depNodeTokens.length-1];
				if (cutDepNode.trim().equals(containee.trim())) {
					for (List<String> fact : containerFacts) {
						
						if (fact.get(2).equals(cutDepNode)) {
							logger.debug("Changing fact: " + fact);
							fact.remove(2);
							fact.add(depNode);
							logger.debug("New fact: " + fact);
						}
					}
				}
			}
		}
	}
	
	private static int countNodes(NodesContent nodesContent) {
		int nodeCount = 0;
		if (nodesContent == null) {
			return 0;
		}
		if (nodesContent.getNode().size() != 0) {
			for (NodeContent nodeContent : nodesContent.getNode()) {
				nodeCount++;
				nodeCount += countNodes(getNodesContent(nodeContent));
			}
		}
		return nodeCount;
	}

	private static NodesContent getNodesContent(NodeContent parentNode) {
		for (Object obj : parentNode.getAttvaluesOrSpellsOrNodes()) {
			if (obj instanceof NodesContent) {
				return (NodesContent)obj;
			}
		}
		return null;
		
	}

	public static XMLGregorianCalendar getXMLGregorianCalendarNow() 
            throws DatatypeConfigurationException
    {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        XMLGregorianCalendar now = 
            datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        return now;
    }

	private static void attemptToUseGexfJavaLibrary(
			Iterable<List<String>> containerFacts, Set<String> containerSet) {
		Gexf gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();

		gexf.getMetadata().setLastModified(date.getTime())
				.setCreator("extractors").setDescription("A hierarchical brick dependency graph");

		Graph graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.DIRECTED).setMode(Mode.STATIC);
		
		for (String container : containerSet) {
			graph.createNode(container).setLabel(container);
		}
		
		for (List<String> fact : containerFacts) {
			for (String container : containerSet) {
				if (
						fact.get(2).equals(container) &&
						!fact.get(1).equals(fact.get(2))
					) {
					Node parentNode = getNode(fact.get(1),graph.getNodes());
					parentNode.createNode(fact.get(2));
				}
			}
			
		}
		
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File("linux_auth_recovery.gexf");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(f, false);
			graphWriter.writeToStream(gexf, fos);
			System.out.println(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Node getNode(String container, List<Node> nodes) {
		for (Node node : nodes) {
			if (node.getId().equals(container))
				return node;
		}
		return null;
	}
}
