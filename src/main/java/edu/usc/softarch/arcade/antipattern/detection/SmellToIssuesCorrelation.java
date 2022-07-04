package edu.usc.softarch.arcade.antipattern.detection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.antipattern.Smell;
import edu.usc.softarch.arcade.antipattern.SmellCollection;
import edu.usc.softarch.arcade.util.FileUtil;
import edu.usc.softarch.arcade.util.MapUtil;

public class SmellToIssuesCorrelation {
	private static Logger logger =
		LogManager.getLogger(SmellToIssuesCorrelation.class);

	public static void main(String[] args) throws IOException {
		// inputDirFilename is the directory containing the .ser files which contain detected smells
		String inputDirFilename = args[0];
		
		// location of the version2issuecountmap.obj file
		String issuesCountMapFilename = args[1];
		
		List<File> fileList = FileUtil.getFileListing(new File(FileUtil.tildeExpandPath(inputDirFilename)));
		Set<File> orderedSerFiles = new TreeSet<>();
		for (File file : fileList)
			if (file.getName().endsWith(".ser"))
				orderedSerFiles.add(file);
		
		// key: version, value: smells counts for the version
		Map<String,Integer> versionToSmellCount = new LinkedHashMap<>();
		for (File file : orderedSerFiles) {
			logger.debug(file.getName());
			SmellCollection smells = SmellCollection.deserialize(file.getAbsolutePath());
			logger.debug("\tcontains " + smells.size() + " smells");
			
			logger.debug("\tListing detected smells for file" + file.getName() + ": ");
			for (Smell smell : smells)
				logger.debug("\t" + smell.getSmellType() + " " + smell);
			
			// You may need to change the regular expression below to match the versioning scheme of your project
			Pattern p = Pattern.compile("[0-9]+\\.[0-9]+(\\.[0-9]+)*");
			Matcher m = p.matcher(file.getName());
			String currentVersion = "";
			if (m.find()) currentVersion = m.group(0);
			
			versionToSmellCount.put(currentVersion, smells.size());
		}
		
		versionToSmellCount = MapUtil.sortByKeyVersion(versionToSmellCount);
		//TODO This is right derpy, but it's fine temporarily because these prints
		// will all get removed anyway. It's because to use inside a lambda, the
		// variable has to be "effectively final".
		Map<String, Integer> versionToSmellCountPrintable1 =
			new HashMap<>(versionToSmellCount);
		System.out.println("Smell counts for versions:");
		System.out.println(versionToSmellCountPrintable1.keySet().stream()
			.map(key -> key + " = " + versionToSmellCountPrintable1.get(key))
			.collect(Collectors.joining("\n")));
		
		List<Integer> smellCounts = new ArrayList<>(versionToSmellCount.values());
		List<String> smellCountStrings =
			smellCounts.stream().map(String::valueOf).collect(Collectors.toList());
		System.out.println("Smell counts only:");
		System.out.println(String.join(",", smellCountStrings));
		double[] smellCountsArr = new double[smellCounts.size()];
		for (int i=0;i<smellCounts.size();i++)
			smellCountsArr[i] = (double)smellCounts.get(i);
		
		XStream xstream = new XStream();
		Map<String,Integer> issuesCountMap =
			(Map<String,Integer>) xstream.fromXML(
			new File(FileUtil.tildeExpandPath(issuesCountMapFilename)));
		System.out.println("Number of issues for each version:");
		System.out.println(issuesCountMap.keySet().stream()
			.map(key -> key + " = " + issuesCountMap.get(key))
			.collect(Collectors.joining("\n")));

		//TODO This is right derpy, but it's fine temporarily because these prints
		// will all get removed anyway. It's because to use inside a lambda, the
		// variable has to be "effectively final".
		Map<String, Integer> versionToSmellCountPrintable2 =
			new HashMap<>(versionToSmellCount);
		
		System.out.println("Keys of smell count map: ");
		System.out.println(versionToSmellCountPrintable2.keySet().stream()
			.map(key -> key + " " + versionToSmellCountPrintable2.get(key))
			.collect(Collectors.joining("\n")));
		
		List<String> versions = new ArrayList<>(versionToSmellCount.keySet());
		double[] issueCountsArr = new double[smellCounts.size()];
		for (int i=0;i<smellCounts.size();i++) {
			issueCountsArr[i] = 0;
			if (versions.get(i).endsWith(".0")) {
				String currentVersion = versions.get(i);
				currentVersion = currentVersion.substring(0, currentVersion.lastIndexOf(".0"));
				if (issuesCountMap.get(currentVersion) != null)
					issueCountsArr[i] += (double) issuesCountMap.get(currentVersion);
			}
			if (issuesCountMap.get(versions.get(i)) != null)
				issueCountsArr[i] += (double)issuesCountMap.get(versions.get(i));
		}

		System.out.println("version, smell count, issue count");
		for (int i=0;i<smellCounts.size();i++)
			System.out.println(versions.get(i) + ", " + smellCountsArr[i] + ", " + issueCountsArr[i]);
		
		PearsonsCorrelation pearsons = new PearsonsCorrelation();
		System.out.println("Pearson's correlation: " + pearsons.correlation(smellCountsArr, issueCountsArr));
		
		SpearmansCorrelation spearmans = new SpearmansCorrelation();
		System.out.println("Spearman's correlation: " + spearmans.correlation(smellCountsArr, issueCountsArr));
	}
}