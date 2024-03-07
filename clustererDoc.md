# Clusterer

*Authored by Marcelo Schmitt Laser*

Clusterer is the entry point of ARC, WCA and Limbo. It parameterizes and 
standardizes all three of them, since they are effectively the same 
algorithm.

First, the Clusterer initializes a few data structures through the 
ClustererArguments inner class.

- An Architecture is initialized with all the input information: input data, 
  such as element dependencies and concern data. The ClustererArguments 
  identifies whether concern data was input at all, for similarity measures 
  ("clustering techniques") that do not take semantic information into 
  consideration.
- A StoppingCriterion is created with the decision algorithm of when to stop
  the clustering process.
- A SerializationCriterion is created to handle serialization decisions, i.e.
  if/when to serialize during the clustering process.
- The SimMeasure to be applied is identified. This is the only part of the 
  clustering techniques that actually differs, and therefore is the critical 
  argument for determining how the architecture is recovered.

The actual clustering process (computeArchitecture) initializes a 
SimilarityMatrix by feeding it the initialized Architecture object and the 
selected SimMeasure. The SimilarityMatrix will calculate the similarity 
value of every cluster pair and hold them in order.

Here the algorithm enters a loop, predicated on the selected 
and parameterized StoppingCriterion. The highest-similarity cluster pair is 
requested from the SimilarityMatrix and is merged, with the Cluster 
constructor handling all the necessary merge actions depending on the 
selected similarity measure. Finally, the SimilarityMatrix is updated to 
reflect the clusters being merged. At this point, the SerializationCriterion 
is queried, and the Architecture is serialized if appropriate. Once the 
StoppingCriterion accuses the end of the process, the loop terminates and 
the resulting Architecture is returned.

All single-step architecture recovery techniques based on a similarity value 
can be implemented with this algorithm:

- New similarity measures can be implemented as SimMeasures, modifying the 
  manner in which the SimilarityMatrix is calculated.
- New StoppingCriteria are easy to implement.
- Input handling is relegated to the Architecture object constructor, such 
  that different input formats can have handlers implemented for them by 
  extending the Architecture class. Processing and post-processing methods 
  are made available for overriding, with the processing initialization 
  being dedicated for structural data and the post-processing initialization 
  being dedicated for (optional) semantic data.
- The format of semantic data can be overriding by extending the Cluster 
  class, such that new concern extraction methods can be implemented with 
  minimal refactoring effort. If the semantic data is in the form of a 
  feature vector, no changes are necessary: it suffices to create a parser 
  to read them into the existing data structures.

Multistep architecture recovery techniques (that is, recovery techniques 
that involve the use of different similarity measures at different stages) 
are still not viable through this design. Hybrid architecture recovery 
techniques are viable by calculating the composing similarity measures and 
assigning them weights between 0..1 such that the sum of the weights is 1.