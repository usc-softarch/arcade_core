package edu.usc.softarch.arcade.clustering.acdc;

public class Sortable implements Comparable<Sortable> {
  private final Integer key;
  private final Node o;

  public Sortable(Integer key, Node o) {
    this.key = key;
    this.o = o;
  }

  public Integer getKey() {
    return key ;
  }

  public Node getObject() {
    return o;
  }

  public int compareTo(Sortable o) {
    return key.compareTo(o.getKey());
  }  
}
