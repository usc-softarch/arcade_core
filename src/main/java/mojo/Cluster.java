package mojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Cluster {

  boolean isempty;

  /* No. A */
  private int no = 0;

  /* no. of cluster in A */
  // private int l = 0;
  /* no. of cluster in B */
  private int m = 0;

  /* max V(ij), the maximium tags */
  private int maxtag = 0;

  /* |Ai|, total objects in Ai */
  private int totaltags = 0;

  /* the total number of total groups */
  private int groupNo = 0;

  /* the group No that Ai belongs to */
  private int group = 0;

  /* tags */
  private int tags[];

  /* total misplaced omnipresent objects */
  private int misplacedOmnipresentObjects = 0;

  /* object list */
  private List<List<String>> objectList;

  /* group list */
  public List<Integer> groupList;

  public int getMisplacedOmnipresentObjects() {
    return misplacedOmnipresentObjects;
  }

  public int getNo() {
    return no;
  }

  public int getGroup() {
    return group;
  }

  public void setGroup(int group) {
    this.group = group;
  }

  public int getMaxtag() {
    return maxtag;
  }

  public int gettotalTags() {
    return totaltags;
  }

  public int getGroupNo() {
    return groupNo;
  };

  public void setGroupNo(int groupNo) {
    this.groupNo = groupNo;
  }

  public void minusGroupNo() {
    groupNo--;
  }

  public Cluster() {
  }

  public Cluster(int no, int l, int m) {
    isempty = false;
    // this.l = l;
    this.m = m;
    this.no = no;
    tags = new int[m];
    objectList = new ArrayList<>(m);
    groupList = new ArrayList<>();
    for (int j = 0; j < m; j++)
    {
      tags[j] = 0;
      objectList.add(j, new ArrayList<>());
    }
  }

  public int addobject_mojoplus(int t, String object) {
    if (t >= 0 && t < m && tags[t] == 0)
    {
      group = t;
      groupNo += 1;
      tags[t] = 1;
      totaltags += 1;
      groupList.add(t);
      objectList.get(t).add(object);
      maxtag = 1;
    }
    return t;
  }

  public int addobject_mojo(int t, String object) {
    if (t >= 0 && t < m) {
      tags[t] += 1;
      totaltags += 1;
      objectList.get(t).add(object);

      /* if tags is max & unique,then change group to it & clear grouplist */
      if (tags[t] > maxtag) {
        maxtag = tags[t];
        group = t;
        groupNo = 1;
        groupList.clear();
        groupList.add(t);
      }
      /* if tags is max but not nuique,then add it to the grouplist */
      else if (tags[t] == maxtag) {
        groupNo += 1;
        groupList.add(t);
      }
    }
    return group;
  }

  public int addobject(int t, String object, String mode) {
    if (mode.equals("MoJo")) return addobject_mojo(t, object);
    else return addobject_mojoplus(t, object);
  }

  public String toString() {
    String str = "";
    str = str + "A" + (no + 1) + " is in group G" + (group + 1) + "\n";

    for (int i = 0; i < m; i++)
      if (!objectList.get(i).isEmpty())
        str = str + "Group " + (i + 1) + ":" + " have " + objectList.get(i).size() + " objects, they are " + objectList.get(i).toString() + "\n";

    return str;
  }

  /* move objects to another cluster */
  public void move(int grouptag, Cluster sub) {
    for (int i = 0; i < objectList.get(grouptag).size(); i++)
      sub.objectList.get(grouptag).add(objectList.get(grouptag).get(i));
    objectList.get(grouptag).clear();
  }

  /* merge with another cluster */
  public void merge(Cluster sub) {
    maxtag += sub.maxtag;
    totaltags += sub.totaltags;
    sub.isempty = true;
    for (int j = 0; j < m; j++)
    {
      for (int i = 0; i < sub.objectList.get(j).size(); i++)
        objectList.get(j).add(sub.objectList.get(j).get(i));

    }

  }

  /* detect whether is a omnipresent object */
  private boolean isOmniPresent(String obj, List omniVector) {
    if (omniVector == null || omniVector.isEmpty()) return false;
    return omniVector.indexOf(obj) != -1;
  }

  /* edge metric cost */
  /*
   * calculation abs(edges(obj_j,A_i)-edges(obj_i,A_j)/
   * edges(obj_j,A_i)+edges(obj_i,A_j)
   */
  /* We assume we will move obj_j from cluster A_i to cluster A_j */
  public double edgeCost(Map<String, Double> tableR, Cluster[] grouptags, List<Integer> omniVector) {
    double cost = 0;
    double value1 = 0, value2 = 0;
    for (int j = 0; j < m; j++) {
      if (j != group) {
        if (grouptags[j] == null) {
          /*
           * if target cluster is null, i.e. we need to create one
           * then the additional edge cost is
           * abs(edge(A_i,obj_j)-0)/edge(A_i,obj_j)+0 = 1 we don't
           * need to calculate edge(A_i,obj_j) anymore
           */
          // cost += objectList[j].size();
          /* search omnipresent objects in objectList[j], T_j in A_i */
          for (int i = 0; i < objectList.get(j).size(); i++) {
            String obj = objectList.get(j).get(i);
            if (isOmniPresent(obj, omniVector)) misplacedOmnipresentObjects++;
            else cost++;
          }
        }
        else {
          for (int i = 0; i < objectList.get(j).size(); i++) {
            String obj = objectList.get(j).get(i);
            if (isOmniPresent(obj, omniVector)) misplacedOmnipresentObjects++;
            else {
              double edges_source = 0, edges_target = 0;
              /*
               * calculate edges(A_i,obj_j),calculate only total
               * connections between obj_j and all the tag T_i in
               * A_i
               */
              for (int k = 0; k < objectList.get(group).size(); k++) {
                String obj2 = objectList.get(group).get(k);
                /*
                 * we consider both the connection from obj to
                 * obj2 and obj2 to obj as same, so we plus them
                 * together as the total connection strength
                 * between obj and obj2
                 */
                value1 = tableR.get((obj + "%@$" + obj2)) == null ? 0 : ((Double) tableR.get(obj + "%@$" + obj2)).doubleValue();
                value2 = tableR.get((obj2 + "%@$" + obj)) == null ? 0 : ((Double) tableR.get(obj2 + "%@$" + obj)).doubleValue();
                edges_source += value1 + value2;
              }
              /*
               * calculate edges(A_j,obj_j),calculate only total
               * connections between obj_j and all the tag T_j in
               * A_j
               */
              for (int l = 0; l < grouptags[j].objectList.get(j).size(); l++) {
                String obj3 = grouptags[j].objectList.get(j).get(l);
                value1 = tableR.get((obj + "%@$" + obj3)) == null ? 0 : ((Double) tableR.get(obj + "%@$" + obj3)).doubleValue();
                value2 = tableR.get((obj3 + "%@$" + obj)) == null ? 0 : ((Double) tableR.get(obj3 + "%@$" + obj)).doubleValue();
                edges_target += value1 + value2;
              }
              if (edges_target != edges_source) cost += Math.abs(edges_source - edges_target) / (edges_source + edges_target);
            }
          }
        }
      }
    }
    return cost;
  }
}