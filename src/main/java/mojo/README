  MoJo Distance Measure - Release 2.0
  -------------------------------------

  To install, simply ensure that the mojo package is in your CLASSPATH.
  There should be a number of Java source and class files in the same
directory as this README file, as well as two RSF files (distra.rsf and
distrb.rsf).

  The files distra.rsf and distrb.rsf are for testing purposes. After
successful installation you should be able to give:

  java mojo.MoJo distra.rsf distrb.rsf 

  This should create the following output: 

383

  This is the one-way MoJo distance from decomposition distra.rsf to
distrb.rsf.  There are also a number of options that allow to calculate
two-way distance as well as MoJoPlus distance, and the MoJoFM and
EdgeMoJo metrics. You can see all these options by simpling typing: 

  java mojo.MoJo -h

  To calculate the distance between any two partitions, you will need to 
create two RSF files similar to the examples provided here. This means:
  1) The relation name must always be contain.
  2) There should be exactly one line per clustered object, i.e. only
flat decompositions are supported.
  
  If the two decompositions do not refer to the same set of clustered
objects, only the intersection of the two sets will be considered.

  Feel free to send email to bil@cse.yorku.ca with any questions. The
algorithm that calculates the MoJo distance was developed by Zhihua Wen.

