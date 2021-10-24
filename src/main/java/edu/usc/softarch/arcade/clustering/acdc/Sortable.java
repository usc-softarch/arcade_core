package edu.usc.softarch.arcade.clustering.acdc;

public class Sortable implements Comparable<Sortable> {
  private Integer key;
  private Node o;

  public Sortable(Integer key, Node o) {
    this.key = key;
    this.o = o;
  }

  public void setKey(Integer key) {
    this.key = key;
  }

  public Integer getKey() {
    return key ;
  }

  public void setObject(Node o) {
    this.o = o;
  }

  public Node getObject() {
    return o;
  }

  public int compareTo(Sortable o) {
    return key.compareTo(o.getKey());
  }  
}