package edu.usc.softarch.arcade.util.ldasupport;

import cc.mallet.types.FeatureSequence;
import cc.mallet.types.Instance;

public class InstanceComparator {
  private String data;
  private String name;

  public InstanceComparator(Instance instance) {
    this.data = ((FeatureSequence) instance.getData()).toString();

    String[] nameSplit = ((String) instance.getName()).split("\\\\\\\\");
    if (nameSplit.length == 1)
      this.name = (String) instance.getName();
    else
      this.name = nameSplit[1];
  }

  public String getName() {
    return this.name;
  }

  public String getData() {
    return this.data;
  }

  public boolean equals(Object o) {
    if(!(o instanceof InstanceComparator)) return false;

    InstanceComparator instComp = (InstanceComparator) o;
    return this.data.equals(instComp.data)
      && this.name.equals(instComp.name);
  }

  public int hashCode() {
    return this.data.hashCode()
      * this.name.hashCode();
  }
}