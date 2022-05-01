Marcelo Schmitt Laser

This file is meant to explain certain stylistic choices I made during the 
coding of ARCADE, to hopefully aid in the maintenance and understanding of 
the codebase. I will continually add items to it as I become conscious of 
more decisions. These are not meant as a statement of disapproval towards 
others' styles; they are merely a justification of my choices, intended to 
aid future maintainers and contributors.

I precede this by restating something that is present in the README.md: the 
reason why this version is called ARCADE_Core is that only its main 
functionalities have been kept. The original ARCADE workbench 
(https://bitbucket.org/joshuaga/arcade) contains significantly more 
functionalities, including several experimental designs and scripts. It was 
both beyond my time constraints and my abilities as a programmer to refactor 
and verify the correctness of the entirety of ARCADE. The 
purpose of ARCADE_Core is to be a more robust and extensible version of the 
main functionalities of ARCADE, not to supplant it.

- **Static attributes are evil**: While static methods are sometimes 
  reasonable, static attributes should be avoided at all 
  costs. I have yet to find any circumstances in which they appear 
  reasonable. The reasons are numerous: aside from the obvious encapsulation 
  and comprehensibility advantages, static class members have caused serious 
  issues with parallelization efforts in the past. I have done my best to 
  cull their presence wherever I could find them.
- **Attributes should only be accessed by accessors**: I have tried to use 
  region blocks in most of the code to clearly indicate which methods are 
  meant to be accessors. Any method that is not an accessor (or, in some 
  cases, a constructor), should not have direct access to attributes, even 
  within the same class. This grealy decreases the odds of a method 
  unwittingly bypassing a consistency check put in place 10 years ago.
- **Unless the field is immutable**: An exception to the above point: 
  immutable attributes have no need for accessors.
- **Accessors are your friends**: In line with the above point, I have given 
  preference to writing an excessive number of accessors to a data structure,
  rather than permitting direct operations by outsiders, even where such 
  operations are simple calls to existing methods (e.g. manipulating lists). 
  This is for the sake of understandability and for enforcing consistency 
  checks.
- **All member accesses should be preceded by <code>this</code> or 
  <code>super</code>**: A lot of ARCADE is dependent on complicated class 
  hierarchies. In order to make the origin of members clear, I have 
  attempted to use the <code>this</code> and <code>super</code> prefixes 
  almost universally. This has made identifying where a member originates 
  from much easier. The exception is accessors, as explained above.
- **Lines should, as much as possible, not exceed 80 characters**: A line of 
  code that exceeds 80 characters often indicates excessively long 
  statements or unnecessarily long member names. Plus, they make it 
  difficult to open multiple code files side by side on the same monitor, 
  and make it painful to review source code files in console-based editors. 
  That said, this rule is not strictly enforced when an individual line 
  exceeds 80 characters by very little and cannot easily be broken in two.
- **It is better to inherit from a Collection than to instantiate one**: Many 
  parts of ARCADE use Collections as clearly representative of a domain 
  concept. In such cases, I have created data structures to simply inherit 
  from those Collections, and enhanced them with domain-specific operations 
  that were otherwise being done by their users. In my opinion, a List 
  should only ever be a simple List if it does nothing but hold things. A 
  <code>Map<String, List<List\<Double\>>></code> does not appear to me to be 
  an appropriate data structure to use in production code, regardless of 
  whether it has any special attributes or not.
- **Stars are not welcome**: By which I mean the * wildcard. Any and all 
  <code>import *</code> and related statements will be removed without 
  exception. Beyond the base issues of readability, <code>import *</code> 
  statements significantly extended the amount of effort required to update 
  several of ARCADE's dependencies in the past. All imports must be fully 
  qualified.
- **Be wary of Util**: In my experience, "Util" is the word we use we don't 
  actually know where something should go. While I have not given this 
  enough thought to outright forbid the use of "utility classes", I have 
  done my best to remove them from the system. Their methods have almost 
  universally seemed better suited as a member of a newly-formed data 
  structure. The exception is IO utilities such as FileUtil, as I have yet 
  to find an appropriately elegant way to handle those.
- **Choose external libraries with care**: I have found that at least half 
  of the time, entire external libraries are imported just to use one or two 
  very simple utility methods. Of those, the vast majority could easily be 
  implemented with one or two lines of Java Streams (the sole exception 
  being the SystemUtils class from Apache Commons, which proved too 
  contrived to safely replace). Preference should be given to these in-house 
  implementations of exceptionally simple functionalities such as object 
  comparisons and manipulation. For reference, after the first round of 
  refactorings, which exclusively involved updating of deprecated code and 
  removal of under-utilized utility libraries, ARCADE_Core's packaged jar 
  became half the size it was prior. Care should be given to avoid bloating 
  it again. To this end, while maintainer, I may elect to reject any 
  contributions that include libraries not already present in ARCADE_Core's 
  dependencies if I deem that those libraries are being under-utilized and 
  can easily be replaced by in-house code.
- **Dead code is deleted code**: No matter how useful a piece of code has 
  been in the past, if it is no longer in use and its past use case is not 
  documented, it will most likely be deleted. Without usage examples, the 
  effort of manufacturing a use case outweighs the benefits of 
  retro-fitting dead code into the system. This is particularly true when 
  external files are involved where no such files exist anymore.
- **English has rules, and they should be followed**: Software code is meant 
  to be read by people, not computers; otherwise, we may as well all go back 
  to using punchcards. As such, spelling errors and grammatical mistakes are 
  unacceptable in ARCADE, whether in its source code or its documentation.
  Please report all such errors that I may have committed, and be aware that I 
  will review pull requests for them. Having English as a second language is 
  not an admissible excuse: both Neno and I are foreigners who learned 
  English well into our adulthood. If we could do it, so can you.
- **Empty constructors are useless constructors**: There are some exceptions,
  but generally speaking, empty constructors are a lazy programmer's way of 
  avoiding integrity checks. Rare is the object that makes sense without a 
  single field being attributed to, and an object that makes no sense is an 
  integrity threat.
- **Never iterate a HashMap** - HashMaps are not an ordered collection, and 
  should therefore never be iterated over. To do so causes significant 
  problems in determinism and, by extension, testing. In short, when one 
  iterates over a HashMap, one is delegating the condition of the order of 
  operations to the implementation of the HashMap data structure, rather 
  than establishing their own ordering condition. This essentially makes 
  the control flow arbitrary. This is further compounded when switching 
  between versions of either the data structure held by the map, or the Java 
  language, thereby changing the hashing functions and causing the entire 
  order of the program's execution to change. Obviously, this breaks several 
  tests, but more importantly may even alter the accuracy of some techniques.
  Therefore, one may never iterate over HashMaps. TreeMaps are the ideal 
  choice when map iteration is desired, as they allow one to explicitly 
  determine the iteration order. LinkedHashMaps are admissible, but 
  undesirable, as they will suffer from the same arbitrariness as HashMaps 
  when used to hold deserialized data, i.e. the order of execution is then 
  delegated to the order of the input data, rather than the language 
  implementation. 