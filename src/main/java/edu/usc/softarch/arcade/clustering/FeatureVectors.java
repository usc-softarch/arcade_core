package edu.usc.softarch.arcade.clustering;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class FeatureVectors implements Serializable {
	// #region ATTRIBUTES --------------------------------------------------------
	private static ObjectMapper mapper;
	private static final long serialVersionUID = -8870834810415855677L;

	private List<String> featureVectorNames = new ArrayList<>();
	private List<String> namesInFeatureSet = new ArrayList<>();
	private Map<String, BitSet> nameToFeatureSetMap = new HashMap<>();
	private int numSourceEntities;
	// #endregion ATTRIBUTES -----------------------------------------------------

	// #region CONSTRUCTORS ------------------------------------------------------
	/**
	 * Used by Jackson for deserialization.
	 */
	private FeatureVectors() { super(); }

	public FeatureVectors(Set<Map.Entry<String,String>> edges) {
		// Make a List with the sources of all edges
		List<String> startNodesList =
			edges.stream().map(Map.Entry::getKey).collect(Collectors.toList());

		this.numSourceEntities = new HashSet<>(startNodesList).size();

		// Make a List with the targets of all edges
		Set<String> endNodesListWithNoDupes =	edges.stream().map(Map.Entry::getValue)
			.collect(Collectors.toCollection(TreeSet::new));
		
		// Make a Set with all nodes
		Set<String> allNodesSet = new HashSet<>(startNodesList);
		allNodesSet.addAll(endNodesListWithNoDupes);

		for (String source : allNodesSet) {
			BitSet featureSet = new BitSet(endNodesListWithNoDupes.size());
			int bitIndex = 0;
			for (String target : endNodesListWithNoDupes) {
				if (edges.contains(new
						AbstractMap.SimpleEntry<>(source, target))) {
					featureSet.set(bitIndex, true);
				}
				bitIndex++;
			}

			this.nameToFeatureSetMap.put(source, featureSet);
		}

		this.setFeatureVectorNames(new ArrayList<>(allNodesSet));
		this.namesInFeatureSet = new ArrayList<>(endNodesListWithNoDupes);
	}
	// #endregion CONSTRUCTORS ---------------------------------------------------

	//region ACCESSORS
	public Map<String, BitSet> getNameToFeatureSetMap() {
		return nameToFeatureSetMap;	}

	public void setNameToFeatureSetMap(Map<String, BitSet> nameToFeatureSetMap){
		this.nameToFeatureSetMap = nameToFeatureSetMap; }

	public List<String> getNamesInFeatureSet() {
		return namesInFeatureSet;	}

	public void setNamesInFeatureSet(List<String> namesInFeatureSet) {
		this.namesInFeatureSet = namesInFeatureSet;
	}

	public int getNumSourceEntities() { return this.numSourceEntities; }

	public List<String> getFeatureVectorNames() {
		return featureVectorNames;
	}

	public void setFeatureVectorNames(List<String> featureVectorNames) {
		this.featureVectorNames = featureVectorNames;
	}
	//endregion

	// #region SERIALIZATION -----------------------------------------------------
	public void serializeFFVectors(String filePath) throws IOException {
		getSerializationMapper().writeValue(new File(filePath), this);
	}

	public static FeatureVectors deserializeFFVectors(String filePath)
			throws IOException {
		return getSerializationMapper().readValue(
			new File(filePath), FeatureVectors.class);
	}

	private static class BitSetSerializer extends JsonSerializer<BitSet> {
		@Override
		public void serialize(BitSet value, JsonGenerator gen,
				SerializerProvider serializers) throws IOException {
			gen.writeStartArray();
			for (long l : value.toLongArray())
				gen.writeNumber(l);
			gen.writeEndArray();
		}

		@Override
		public Class<BitSet> handledType() {
			return BitSet.class;
		}
	}

	private static class BitSetDeserializer extends JsonDeserializer<BitSet> {
		@Override
		public BitSet deserialize(JsonParser jsonParser,
				DeserializationContext deserializationContext) throws IOException {
			ArrayList<Long> l = new ArrayList<>();
			JsonToken token;
			while (!JsonToken.END_ARRAY.equals(token = jsonParser.nextValue()))
				if (token.isNumeric())
					l.add(jsonParser.getLongValue());

			return BitSet.valueOf(l.stream().mapToLong(i -> i).toArray());
		}
	}

	private static ObjectMapper getSerializationMapper() {
		if (FeatureVectors.mapper != null) return FeatureVectors.mapper;

		FeatureVectors.mapper = new ObjectMapper();
		SimpleModule bitSetModule = new SimpleModule("BitSetModule");
		bitSetModule.addSerializer(new BitSetSerializer());
		bitSetModule.addDeserializer(BitSet.class, new BitSetDeserializer());
		FeatureVectors.mapper.registerModule(bitSetModule);
		return FeatureVectors.mapper;
	}
	// #endregion SERIALIZATION --------------------------------------------------

	//region OBJECT METHODS
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FeatureVectors)) return false;
		FeatureVectors that = (FeatureVectors) o;
		return getNumSourceEntities() == that.getNumSourceEntities()
			&& Objects.equals(getFeatureVectorNames(), that.getFeatureVectorNames())
			&& Objects.equals(getNamesInFeatureSet(), that.getNamesInFeatureSet())
			&& Objects.equals(getNameToFeatureSetMap(), that.getNameToFeatureSetMap());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getFeatureVectorNames(), getNamesInFeatureSet(),
			getNameToFeatureSetMap(), getNumSourceEntities());
	}
	//endregion
}
