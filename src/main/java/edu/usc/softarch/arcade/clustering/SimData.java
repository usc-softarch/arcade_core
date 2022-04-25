package edu.usc.softarch.arcade.clustering;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SimData implements Comparable<SimData> {
	public final Cluster c1;
	public final Cluster c2;
	public final Double cellValue;
	public final Integer clusterSize;
	public final String cellName;

	public SimData(Cluster c1, Cluster c2, Double cellValue, Integer clusterSize) {
		this.c1 = c1;
		this.c2 = c2;
		this.cellValue = cellValue;
		this.clusterSize = clusterSize;
		this.cellName = getCellName(c1.getName(), c2.getName());
	}

	private static String getCellName(String c1, String c2) {
		String[] clusterNames = { c1, c2 };
		// Order the names
		Arrays.stream(clusterNames).sorted()
			.collect(Collectors.toList()).toArray(clusterNames);
		return clusterNames[0] + clusterNames[1];
	}

	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SimData simData = (SimData) o;
		return Objects.equals(c1, simData.c1)
			&& Objects.equals(c2, simData.c2)
			&& Objects.equals(cellValue, simData.cellValue)
			&& Objects.equals(clusterSize, simData.clusterSize)
			&& Objects.equals(cellName, simData.cellName);
	}

	@Override
	public int hashCode() {
		return Objects.hash(c1, c2, cellValue, clusterSize, cellName); }

	@Override
	public int compareTo(SimData o) {
		if (!this.cellValue.equals(o.cellValue))
			return this.cellValue.compareTo(o.cellValue);
		if (!this.clusterSize.equals(o.clusterSize))
			return this.clusterSize.compareTo(o.clusterSize);
		return this.cellName.compareTo(o.cellName);
	}
	//endregion

	//region SERIALIZATION
	public void serialize(JsonGenerator generator) throws IOException {
		generator.writeNumberField("c1", c1.getName().hashCode());
		generator.writeNumberField("c2", c2.getName().hashCode());
		generator.writeNumberField("val", cellValue);
		generator.writeNumberField("size", clusterSize);
	}

	/**
	 * Deserialization of SimData takes a modified Architecture object where the
	 * keys are the hashcode of the Architecture's keys. This minimizes memory
	 * space required to record a SimilarityMatrix.
	 *
	 * @param parser A Jackson JsonParser, already initialized and set to the
	 *               correct position.
	 * @param hashArchitecture A modified Architecture object where the keys are
	 *                         the hashCode() of the Clusters' names, instead of
	 *                         the names themselves.
	 * @return A SimData object initialized to the appropriate Clusters.
	 * @throws IOException If the file of the JsonParser does not exist, or is
	 * 										 incorrectly formatted.
	 */
	public static SimData deserialize(
			JsonParser parser, Map<Integer, Cluster> hashArchitecture)
			throws IOException {
		parser.nextToken();
		Cluster c1 = hashArchitecture.get(parser.nextIntValue(0));
		parser.nextToken();
		Cluster c2 = hashArchitecture.get(parser.nextIntValue(0));
		parser.nextToken();
		parser.nextToken();
		Double cellValue = parser.getDoubleValue();
		parser.nextToken();
		Integer clusterSize = parser.nextIntValue(0);
		parser.nextToken();

		return new SimData(c1, c2, cellValue, clusterSize);
	}
	//endregion
}
