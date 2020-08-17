package edu.usc.softarch.arcade.classgraphs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

import edu.usc.softarch.arcade.callgraph.MethodEdge;
import edu.usc.softarch.arcade.callgraph.MyCallGraph;
import edu.usc.softarch.arcade.callgraph.MyClass;
import edu.usc.softarch.arcade.callgraph.MyMethod;
import edu.usc.softarch.arcade.classgraphs.exception.CannotGetCurrProjStrException;
import edu.usc.softarch.arcade.clustering.FeatureVectorMap;
import edu.usc.softarch.arcade.config.Config;
import edu.usc.softarch.arcade.topics.DocTopicItem;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.topics.TopicItem;
import edu.usc.softarch.arcade.topics.TopicKey;
import edu.usc.softarch.arcade.topics.TopicKeySet;
import edu.usc.softarch.arcade.util.DebugUtil;

import soot.ArrayType;
import soot.Body;
import soot.MethodOrMethodContext;
import soot.MethodSource;
import soot.NullType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.ValueBox;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.Chain;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

/**
 * @author joshua
 */
public class ClassGraphTransformer extends SceneTransformer  {
	public static ClassGraph clg = new ClassGraph();
	public static MyCallGraph mg = new MyCallGraph();
	private static Map<String,MyClass> classesWithUsedMethods;
	private static Map<String,MyClass> classesWithAllMethods;
	
	public static TopicModelData freecsTMD = new TopicModelData();
	public static TopicModelData LlamaChatTMD = new TopicModelData();

	public String currDocTopicsFilename = "";
	public String currTopicKeysFilename = "";

	public String datasetName = "";
	
	public boolean wekaDataSetProcessing = false;

	private FeatureVectorMap fvMap = new FeatureVectorMap(new HashMap<>());
	
	private static Logger logger = Logger.getLogger(ClassGraphTransformer.class);

	private String rsfFilePath = "INSERT/PATH/HERE"; //TODO parameterize
	private String xmlFilePath = "INSERT/PATH/HERE"; //TODO parameterize

	@Override
	protected void internalTransform(String phaseName, Map options) {
		try {
			CallGraph cg = Scene.v().getCallGraph();
			addCallGraphEdgesToMyCallGraph(cg);
			
			constructClassGraph();
			createClassGraphDotFile();
			
			addUsedMethodsToClasses();
			logger.debug("Printing classes with used methods...");
			printClassesFromHashMap(classesWithUsedMethods);
			
			constructClassesWithAllMethods();
			logger.debug("Printing classes with all methods...");
			printClassesFromHashMap(classesWithAllMethods);
			
			Map<String,MyMethod> unusedMethods = determineUnusedMethods(classesWithUsedMethods,classesWithAllMethods);
			logger.debug("Printing unused methods...");
			printUnusedMethods(unusedMethods);
			
			outputGraphsAndClassesToFiles(xmlFilePath);
			outputUnusedMethodsToFile(unusedMethods);
			Config.initProjectData(this);

			fvMap = new FeatureVectorMap(clg);
			fvMap.writeXMLFeatureVectorMapUsingSootClassEdges();
			clg.generateRsf(rsfFilePath);

			List<SootClass> selectedClasses = new ArrayList<>();
			determineSelectedClasses(clg.getNodes(), selectedClasses);

			// Set up attributes for weka
			if (wekaDataSetProcessing) {
				processWekaDataSet(selectedClasses);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void printUnusedMethods(Map<String, MyMethod> unusedMethods) {
		for (MyMethod m : unusedMethods.values()) {
			logger.debug("\t" + m.toString());
		}
	}

	private Map<String, MyMethod> determineUnusedMethods(
			Map<String, MyClass> classesWithUsedMethods,
			Map<String, MyClass> classesWithAllMethods) {
		Map<String,MyMethod>unusedMethods = new HashMap<>();
		for (MyClass undeterminedClass : classesWithAllMethods.values()) {
			for (MyMethod undeterminedMethod : undeterminedClass.getMethods()) {
				if (classesWithUsedMethods.containsKey(undeterminedClass.toString())) { // undetermined class is part of the used classes
					MyClass usedClass = classesWithUsedMethods.get(undeterminedClass.toString());
					if (!usedClass.getMethods().contains(undeterminedMethod)) {
						unusedMethods.put(undeterminedMethod.toString(), undeterminedMethod); // method is unused
					}
				}
				else  { // undetermined class is not part of the used classes
					unusedMethods.put(undeterminedMethod.toString(), undeterminedMethod);
				}
			}
		}
		return unusedMethods;
	}

	private void constructClassesWithAllMethods() {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		classesWithAllMethods = new HashMap<>();
		for (SootClass sootClass : appClasses) {
			MyClass myClass = new MyClass(sootClass);
			for (SootMethod sootMethod : sootClass.getMethods()) {
				MyMethod myMethod = new MyMethod(sootMethod);
				myClass.addMethod(myMethod);
			}
			classesWithAllMethods.put(myClass.toString(), myClass);
		}
		
	}

	private void processWekaDataSet(List<SootClass> selectedClasses) {
		FastVector atts = new FastVector();
		atts.addElement(new Attribute("name", (FastVector) null));
		atts.addElement(new Attribute("fieldCount"));
		atts.addElement(new Attribute("methodCount"));
		atts.addElement(new Attribute("numCallerEdges"));
		atts.addElement(new Attribute("numCalleeEdges"));
		atts.addElement(new Attribute("pctCallers"));
		atts.addElement(new Attribute("pctCallees"));

		Instances data = new Instances("PDCRelation", atts, 0);
		
		try {
			prepareWekaDataSet(selectedClasses, atts, data);
			writeWekaDataSet(data);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
	}

	private void writeWekaDataSet(Instances data) {
		String datasetsDir = "/home/joshua/Documents/joshuaga-jpl-macbookpro/Documents/workspace/MyExtractors/datasets";

		String fullDir = datasetsDir + "/" + datasetName + "/"
				+ datasetName + ".arff";

		ArffSaver saver = new ArffSaver();
		saver.setInstances(data);
		try {
			saver.setFile(new File(fullDir));
			saver.setDestination(new File(fullDir)); // **not**
			// necessary
			// in 3.5.4 and
			// later
			saver.writeBatch();
			logger.debug("Wrote file: " + fullDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void prepareWekaDataSet(List<SootClass> selectedClasses,
			FastVector atts, Instances data) throws FileNotFoundException {
		List<DocTopicItem> dtItemList;

		DocTopics dts = new DocTopics(currDocTopicsFilename);
		dtItemList = dts.getDocTopicItemList();

		TopicKeySet tkList = new TopicKeySet(currTopicKeysFilename);

		for (int i = 0; i < tkList.size(); i++) {
			atts.addElement(new Attribute("topic" + i));
		}

		for (SootClass c : selectedClasses) {
			List<SootClass> callerClasses = clg.getCallerClasses(c);
			List<SootClass> calleeClasses = clg.getCalleeClasses(c);

			double[] vals = new double[data.numAttributes()];

			DocTopicItem dtItem = getMatchingDocTopicItem(dtItemList, c);

			if (dtItem == null) {
				System.err.println("Got null dtItem for " + c);
			}
			vals[0] = data.attribute(0).addStringValue(c.toString());
			logger.debug("=======================================");
			logger.debug("Field count: " + c.getFieldCount());
			vals[1] = c.getFieldCount();
			logger.debug("Method count: " + c.getMethodCount());
			vals[2] = c.getMethodCount();
			logger.debug("\n");
			logger.debug("Caller edges: " + callerClasses);
			logger.debug("Number of Caller edges: "
					+ callerClasses.size());
			vals[3] = callerClasses.size();
			logger.debug("Callee edges: " + calleeClasses);
			logger.debug("Number of Callee edges: "
					+ calleeClasses.size());
			vals[4] = calleeClasses.size();
			logger.debug("Percentage of Callers: "
					+ getPercentageOfCallers(callerClasses.size(),
							calleeClasses.size()));
			vals[5] = getPercentageOfCallers(callerClasses.size(),
					calleeClasses.size());
			logger.debug("Percentage of Callees: "
					+ getPercentageOfCallees(calleeClasses.size(),
							callerClasses.size()));
			vals[6] = getPercentageOfCallees(calleeClasses.size(),
					callerClasses.size());

			logger.debug("mallet");

			for (TopicKey tk : tkList.getSet()) { // iterator over all possible
												// topics
				boolean hasCurrTopicNum = false;
				for (TopicItem t : dtItem.getTopics()) { // iterate over topics
													// in this
													// document/class
					if (t.getTopicNum() == tk.getTopicNum()) { // if the document as
														// the current topic
						logger.debug(c + " has topic " + t.getTopicNum()
								+ " with proportion " + t.getProportion());
						vals[6 + 1 + tk.getTopicNum()] = t.getProportion();
						hasCurrTopicNum = true;
					}
				}
				if (!hasCurrTopicNum) {
					logger.debug(c + " does not have topic "
							+ tk.getTopicNum());
					vals[6 + 1 + tk.getTopicNum()] = 0;
				}
			}
			logger.debug("\n");
			data.add(new Instance(1.0, vals));
		}
	}

	private void determineSelectedClasses(List<SootClass> cgNodes,
			List<SootClass> selectedClasses) {
		selectedClasses.addAll(cgNodes);
		
		Chain<SootClass> classes = Scene.v().getClasses();
		Iterator<SootClass> iter = classes.iterator();
		logger.debug("Adding abstract classes...");
		while (iter.hasNext()) {
			SootClass c = iter.next();
			if (c.isAbstract() && Config.isClassInSelectedPackages(c)) {
				logger.debug(c);
				selectedClasses.add(c);
			}
		}
	}

	private void outputGraphsAndClassesToFiles(String xmlFilePath) throws ParserConfigurationException,
			TransformerException, IOException {
		writeXMLClassGraph(xmlFilePath);
		serializeMyCallGraph();
		serializeClassesWithUsedMethods();
		serializeClassesWithAllMethods();
	}

	private void writeXMLClassGraph(String xmlFilePath) throws ParserConfigurationException,
			TransformerException {
		clg.writeXMLClassGraph(xmlFilePath);
		logger.debug("ClassGraph's no. of edges: " + clg.size());
	}
	
	private void serializeMyCallGraph() throws IOException {
		String filename = Config.getMyCallGraphFilename();
		logger.debug("Trying to serialize my call graph");
		mg.serialize(filename);
	}
	
	private void serializeClassesWithUsedMethods() throws IOException {
		String filename = Config.getClassesWithUsedMethodsFilename();
		logger.debug("Trying to serialize classes with used methods...");
		serialize(classesWithUsedMethods,filename);
	}
	

	private void serializeClassesWithAllMethods() throws IOException {
		String filename = Config.getClassesWithAllMethodsFilename();
		logger.debug("Trying to serialize classes with all methods...");
		serialize(classesWithAllMethods,filename);
	}
	

	private void outputUnusedMethodsToFile(Map<String, MyMethod> unusedMethods) throws IOException {
		serialize(unusedMethods, Config.getUnusedMethodsFilename());
		
	}

	private void serialize(
			Map<?, ?> hashMap,
			String filename) throws IOException {
			// Write to disk with FileOutputStream
			FileOutputStream f_out = new FileOutputStream(filename);

			// Write object with ObjectOutputStream
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);

			// Write object out to disk
			obj_out.writeObject ( hashMap );
		
	}

	private void createClassGraphDotFile() throws CannotGetCurrProjStrException {
		String dotFilename = "";
		if (!(Config.getCurrProjStr().equals(""))) {
			dotFilename = Config.getClassGraphDotFilename();
		}
		else {
			throw new CannotGetCurrProjStrException("Cannot identify current project's str.");
		}
		try {
			clg.writeDotFile(dotFilename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private DocTopicItem getMatchingDocTopicItem(
			List<DocTopicItem> dtItemList, SootClass c) {
		logger.debug(c);
		logger.debug(c.getShortName());
		String[] classNames = c.getShortName().split("\\$");
		DocTopicItem dtItem = null;
		for (int i = 0; i < classNames.length; i++) {
			if (classNames.length == 1) {
				dtItem = returnMatchingDocTopicItem(dtItemList, classNames[0]);
			} else if (classNames.length == 2) {
				if (isIntNumber(classNames[1])) {
					String[] classPackageAndClassNames = classNames[0]
							.split("\\.");
					String shortClassName = classPackageAndClassNames[classPackageAndClassNames.length - 1];
					dtItem = returnMatchingDocTopicItem(dtItemList,
							shortClassName);
				} else {
					dtItem = returnMatchingDocTopicItem(dtItemList,
							classNames[1]);
				}
			} else if (classNames.length == 3) {
				dtItem = returnMatchingDocTopicItem(dtItemList, classNames[1]);
			} else {
				System.err.println("Cannot handle class " + c + " name");
				System.exit(1);
			}
		}
		return dtItem;
	}

	private boolean isIntNumber(String num) {
		try {
			Integer.parseInt(num);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	private DocTopicItem returnMatchingDocTopicItem(
			List<DocTopicItem> dtItemList, String className) {
		for (DocTopicItem dtItem : dtItemList) {
			String[] sourceSplit = dtItem.getSource().split("/");
			List<String> sourceSplitList = new ArrayList<>(Arrays
					.asList(sourceSplit));
			System.err.println("sourceSplit: " + sourceSplitList);
			String[] nameDotJava = sourceSplit[1].split("\\.");
			List<String> nameDotJavaList = new ArrayList<>(Arrays
					.asList(nameDotJava));
			System.err.println("nameDotJava: " + nameDotJavaList);
			System.err.println("");
			String name = nameDotJava[0];
			System.err.println("Comparing '" + className + "' to '" + name
					+ "'");
			if (className.equals(name)) {
				return dtItem;
			}
		}

		return null;
	}

	private double getPercentageOfCallees(int numCallees, int numCallers) {
		return ((double) numCallees / (double) (numCallers + numCallees)) * 100d;
	}

	private double getPercentageOfCallers(int numCallers, int numCallees) {
		return ((double) numCallers / (double) (numCallers + numCallees)) * 100d;
	}

	private static void constructClassGraph() {
		CallGraph cg = Scene.v().getCallGraph();
		addCallGraphEdgesToClassGraph(cg);
		addSuperClassEdgesToClassGraph();
		addInterfacesToClassGraph();
		addFieldRefEdgesToClassGraph();
		addThrowsEdgesToClassGraph();
		addUseEdgesToCallGraph();
	}

	private static void addUseEdgesToCallGraph() {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();

		for (SootClass srcClass : appClasses) {
			if (srcClass.isConcrete()) {
				Iterator<SootMethod> methodIt = srcClass.getMethods().iterator();
				
				while (methodIt.hasNext()) {
					SootMethod m = methodIt.next();
					logger.debug("=======================================");			
					logger.debug("method: " + m.toString());
					
					Body b = null;
					if (m.isConcrete())
						b = m.retrieveActiveBody();
					else {
						MethodSource ms = m.getSource();
						ms.getBody(m, "cg");
					}
					
					if (b == null) {
						logger.debug("Got null active body for.");
						continue;
					}
					
					UnitGraph graph = new ExceptionalUnitGraph(b);
					Iterator<Unit> graphIt = graph.iterator();
					while (graphIt.hasNext()) { // loop through all units of method's graph
						Unit u = graphIt.next(); // grab the next unit from the graph
						Stmt stmt = (Stmt)u;
						List<ValueBox> useBoxes = stmt.getUseBoxes();
						useEdgesHelper(useBoxes,0,srcClass);
					}
				}
			}
		}
	}
	
	private static void useEdgesHelper(List<ValueBox> useBoxes, int depth, SootClass srcClass) {
		for (ValueBox useBox : useBoxes) {
			Type useBoxType = useBox.getValue().getType();
			Type baseType = useBoxType;
			if (!(useBoxType instanceof RefLikeType) || useBoxType instanceof NullType)
				continue;
			if (useBoxType instanceof ArrayType) {
				ArrayType arrayType = (ArrayType)useBoxType;
				baseType = getNonArrayBaseType(arrayType);
			}
				
			if (baseType instanceof RefLikeType) {
				logger.debug(DebugUtil.addTabs(depth) + "useBox: " + useBox);
				SootClass tgtClass = Scene.v().getSootClass(baseType.toString());
				if (isClassValidForClassGraph(tgtClass)) {
					logger.debug(DebugUtil.addTabs(depth) + "useBox tgtClass: "
							+ tgtClass);
					logger.debug(DebugUtil.addTabs(depth) + "Adding srcClass: " + srcClass + " and " + " tgtClass: "
							+ tgtClass + " to class graph...");
					clg.addEdge(srcClass, tgtClass, "ref");
					
				}
			}
			depth++;
			useEdgesHelper(useBox.getValue().getUseBoxes(),depth,srcClass);
		}
	}
	
	private static Type getNonArrayBaseType(ArrayType arrayType) {
		Type baseType = arrayType.getElementType();
		if (baseType instanceof ArrayType) {
			ArrayType nextArrayType = (ArrayType)baseType;
			baseType = getNonArrayBaseType(nextArrayType);
		}
		return baseType;
		
	}

	private static void addThrowsEdgesToClassGraph() {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();

		for (SootClass c : appClasses) {
			if (c.isConcrete()) {
				Iterator<SootMethod> methodIt = c.getMethods().iterator();
				
				while (methodIt.hasNext()) {
					SootMethod m = methodIt.next();
					logger.debug("=======================================");			
					logger.debug("method: " + m.toString());
					
					Body b = null;
					if (m.isConcrete())
						b = m.retrieveActiveBody();
					else {
						MethodSource ms = m.getSource();
						ms.getBody(m, "cg");
					}
					
					if (b == null) {
						logger.debug("Got null active body for.");
						continue;
					}
					
					UnitGraph graph = new ExceptionalUnitGraph(b);
					Iterator<Unit> graphIt = graph.iterator();
					while (graphIt.hasNext()) { // loop through all units of method's graph
						Unit u = graphIt.next(); // grab the next unit from the graph

						if (u instanceof ThrowStmt) {
							ThrowStmt stmt = (ThrowStmt) u;
							logger.debug("ThrowStmt stmt: " + stmt);
							
							SootClass srcClass = m.getDeclaringClass();
							logger.debug("\t\tsource: " + srcClass);
							SootClass tgtClass = Scene.v().getSootClass(stmt.getOp().getType().toString());
							logger.debug("\t\ttarget: " + tgtClass);
							if (appClasses.contains(srcClass) && appClasses.contains(tgtClass)) {
								if (isEdgeValidForClassGraph(srcClass, tgtClass)) {
									clg.addEdge(srcClass, tgtClass, "throw");
								}
							}

						}
					}
				}
			}
		}
	}

	private static boolean isEdgeValidForClassGraph(SootClass srcClass,
			SootClass tgtClass) {
		return Config.isClassInSelectedPackages(srcClass)
				&& Config.isClassInSelectedPackages(tgtClass);
	}

	private static void addInterfacesToClassGraph() {
		Iterator<SootClass> iter = Scene.v().getApplicationClasses().iterator();
		logger.debug("Adding interface implementation edges to class graph...");
		while (iter.hasNext()) {
			SootClass src = iter.next();
		
			logger.debug("\n");
			logger.debug("SootClass src: " + src);
			
			if (isClassValidForClassGraph(src)) {

				Iterator<SootClass> interfaceIterator = src.getInterfaces().iterator();

				while (interfaceIterator.hasNext()) {
				
					SootClass tgtInterface = interfaceIterator
							.next();

					logger.debug("SootClass tgtInterface: " + tgtInterface);
					if (isClassValidForClassGraph(tgtInterface)) {
						clg.addEdge(src, tgtInterface, "implements");
					}
				}
			}
		}
	}

	private static boolean isClassValidForClassGraph(SootClass src) {
		return Config.isClassInSelectedPackages(src);
	}

	private static void addSuperClassEdgesToClassGraph() {
		Iterator<SootClass> iter = Scene.v().getApplicationClasses().iterator();
		logger.debug("Adding extended super class edges to class graph...");
		while (iter.hasNext()) {
			SootClass src = iter.next();
		
			logger.debug("\n");
			logger.debug("SootClass src: " + src);
			
			if (isClassValidForClassGraph(src)) {

				SootClass tgtSuperClass = src.getSuperclass();
				
			
				logger.debug("SootClass tgtSuperClass: " + tgtSuperClass);
				if (isClassValidForClassGraph(tgtSuperClass)) {
						clg.addEdge(src, tgtSuperClass, "extends");
				}
			}
		}
	}

	private static void printClassesFromHashMap(Map<String,MyClass> classes) {
		for (MyClass c : classes.values()) {
			logger.debug("Showing methods in " + c + "...");
			logger.debug(c.methodsToString(1));
		}
	}

	private static void addUsedMethodsToClasses() {
		Iterator iter = mg.getEdges().iterator();
		logger.debug("Adding my methods to each myclass..");
		classesWithUsedMethods = new HashMap<>();
		while (iter.hasNext()) {
			MethodEdge edge = (MethodEdge) iter.next();
			
			MyClass srcClass = null;
			MyClass tgtClass = null;
			
			if (classesWithUsedMethods.containsKey(edge.getSrc().getDeclaringClass().toString())) {
				srcClass = classesWithUsedMethods.get(edge.getSrc().getDeclaringClass().toString());
			}
			else {
				srcClass = new MyClass(edge.getSrc().getDeclaringClass());
				classesWithUsedMethods.put(edge.getSrc().getDeclaringClass().toString(),srcClass);
			}

			if (classesWithUsedMethods.containsKey(edge.getTgt().getDeclaringClass().toString())) {
				tgtClass = classesWithUsedMethods.get(edge.getTgt().getDeclaringClass().toString());
			}
			else {
				tgtClass = new MyClass(edge.getTgt().getDeclaringClass());
				classesWithUsedMethods.put(edge.getTgt().getDeclaringClass().toString(),tgtClass);
			}
			if (!edge.getSrc().getDeclaringClass().equals(edge.getTgt().getDeclaringClass())) {
				srcClass.addMethod(edge.getSrc());
				tgtClass.addMethod(edge.getTgt());
			}
		}
	}

	private static void addCallGraphEdgesToMyCallGraph(CallGraph cg) {
		Iterator<MethodOrMethodContext> iter = cg.sourceMethods();
		logger.debug("Adding call graph edges to my call graph...");
		while (iter.hasNext()) {
			SootMethod src = iter.next().method();
			MyMethod mySrc = new MyMethod(src);
			
			logger.debug("\n");
			logger.debug("Src SootMethod: " + src);
			logger.debug("Src MyMethod: " + mySrc);
			
			if (Config.isMethodInSelectedPackages(src)  && !Config.isMethodInDeselectedPackages(src)) {

				Iterator<MethodOrMethodContext> targets = new Targets(cg
						.edgesOutOf(src));
				
				while (targets.hasNext()) {
					SootMethod tgt = (SootMethod) targets.next();
					MyMethod myTgt = new MyMethod(tgt);
					
					logger.debug("\tTgt SootMethod" + tgt);
					logger.debug("\tTgt MyMethod" + myTgt);
					if (Config.isMethodInSelectedPackages(tgt)  && !Config.isMethodInDeselectedPackages(tgt)) {

						mg.addEdge(mySrc, myTgt);
					}
				}
			}
		}
	}

	private static void addFieldRefEdgesToClassGraph() {
		Chain<SootClass> appClasses = Scene.v().getApplicationClasses();
		
		logger.debug("Listing application classes...");
		for (SootClass c : appClasses) {
			logger.debug(c);
		}
		
		for (SootClass c : appClasses) {
			if (c.isConcrete()) {
				Iterator methodIt = c.getMethods().iterator();
				
				while (methodIt.hasNext()) {
					SootMethod m = (SootMethod)methodIt.next();
					logger.debug("=======================================");			
					logger.debug("method: " + m.toString());
					
					Body b = null;
					if (m.isConcrete())
						b = m.retrieveActiveBody();
					else {
						MethodSource ms = m.getSource();
						ms.getBody(m, "cg");
					}
					
					if (b == null) {
						logger.debug("Got null active body for.");
						continue;
					}
					
					UnitGraph graph = new ExceptionalUnitGraph(b);
					Iterator graphIt = graph.iterator();
					while (graphIt.hasNext()) { // loop through all units of method's graph
						Unit u = (Unit)graphIt.next(); // grab the next unit from the graph
						
						if (u instanceof Stmt) {
							Stmt stmt = (Stmt) u;
							if (stmt.containsFieldRef()) {
								FieldRef ref = stmt.getFieldRef();
								logger.debug("\t" + ref);
								SootClass srcClass = m.getDeclaringClass();
								logger.debug("\t\tsource: " + srcClass);
								SootClass tgtClass = ref.getFieldRef().declaringClass();
								logger.debug("\t\ttarget: " + tgtClass);
								Type fieldType = ref.getField().getType();
								logger.debug("\t\tfield's type: " + fieldType);
								if (appClasses.contains(srcClass) && appClasses.contains(tgtClass)) {
									if (isEdgeValidForClassGraph(srcClass, tgtClass)) {
										clg.addEdge(srcClass, tgtClass, "field_ref");
									}
								}
								if (ref.getField().getType() instanceof RefType) {
									SootClass fieldTypeClass = Scene.v().getSootClass(fieldType.toString());
									if (appClasses.contains(srcClass) && appClasses.contains(fieldTypeClass)) {
										if (isEdgeValidForClassGraph(srcClass,
												fieldTypeClass)) {
											logger.debug("\t\t\tAdding edge " + "("+ srcClass + ","+ fieldTypeClass +")");
											clg.addEdge(srcClass, fieldTypeClass, "field_ref");
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private static void addCallGraphEdgesToClassGraph(CallGraph cg) {
		Iterator<MethodOrMethodContext> iter = cg.sourceMethods();
		logger.debug("Adding call graph edges to class graph...");
		while (iter.hasNext()) {
			SootMethod src = iter.next().method();
			
			logger.debug("\n");
			logger.debug("Src SootMethod: " + src);
			
			if (Config.isMethodInSelectedPackages(src) && !Config.isMethodInDeselectedPackages(src)) {

				Iterator<MethodOrMethodContext> targets = new Targets(cg
						.edgesOutOf(src));
				
				while (targets.hasNext()) {
					SootMethod tgt = (SootMethod) targets.next();
					
					logger.debug("\tTgt SootMethod" + tgt);
					if (Config.isMethodInSelectedPackages(tgt)  && !Config.isMethodInDeselectedPackages(tgt)) {

						clg.addEdge(src.getDeclaringClass(), tgt
								.getDeclaringClass(), "call");
					}
				}
			}
		}
	}
}