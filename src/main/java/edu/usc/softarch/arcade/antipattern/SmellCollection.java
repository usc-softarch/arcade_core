package edu.usc.softarch.arcade.antipattern;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.thoughtworks.xstream.XStream;

import edu.usc.softarch.arcade.util.FileUtil;

public class SmellCollection extends HashSet<Smell> {
  private static final long serialVersionUID = 1L;
  
  public SmellCollection() { super(); }
  public SmellCollection(String smellFile) throws IOException {
    XStream xstream = new XStream();
		String xml = null;
		xml = FileUtil.readFile(smellFile, StandardCharsets.UTF_8);
    Object result = xstream.fromXML(xml);
    if (!(result instanceof Set<?>))
      throw new IllegalArgumentException(
        "Error parsing XML file: not a valid input");
    // This whole lambda is to avoid unchecked cast. Feel free to improve it.
    this.addAll(((Set<?>) result).stream()
      .filter(Smell.class::isInstance)
      .map(Smell.class::cast)
      .collect(Collectors.toSet()));
  }

  /**
	 * Serializes the results of a smell analysis.
	 * 
	 * @param fileName Path to an output file to serialize into.
	 */
  public void serializeSmellCollection(String fileName) throws IOException {
    try (PrintWriter writer =
				new PrintWriter(fileName, StandardCharsets.UTF_8)) {
			XStream xstream = new XStream();
			String xml = xstream.toXML(this);
	    writer.println(xml);
		}
  }
}