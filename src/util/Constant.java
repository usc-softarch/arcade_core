package util;

public class Constant {
	
	//the list of projects
	public final static int HADOOP = 1;
	public final static int LUCENE = 2;
	public final static int STRUTS2 = 3;
	public final static int IVY = 4;
	
	//current project that is being analyzed
	public final static int PROJECT = STRUTS2;
	
	/**
	 * Ivy file path
	 */
	public final static String[] IVY_PKG_PREFIX = { "src/java/org", "src/main/java/org"}; 
	//input files
	public final static String IVY_ISSUE_FILE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/jira_issue/ivy.json";
	public final static String IVY_SMELL_PKG = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/ivy_pkg.json";
	public final static String IVY_SMELL_ACDC = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/ivy_acdc.json";
	public final static String IVY_SMELL_ARC = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/ivy_arc.json";
	public final static String IVY_ALL_PKG = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_pkg_all.json";
	public final static String IVY_ALL_ACDC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_acdc_all.json";
	public final static String IVY_ALL_ARC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_arc_all.json";

	//output files
	public final static String IVY_DEP_CON_ACDC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/dep_con_smells/ivy_acdc.json";
	public final static String IVY_DEP_CON_ARC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/dep_con_smells/ivy_arc.json";
	public final static String IVY_DEP_CON_PKG = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/dep_con_smells/ivy_pkg.json";
	
	public final static String IVY_PKG_FILTER = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_pkg_all_filter_versions.json";
	public final static String IVY_ACDC_FILTER = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_acdc_all_filter_versions.json";
	public final static String IVY_ARC_FILTER = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_arc_all_filter_versions.json";
	
	public final static String IVY_ARC_MERGE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_arc_all_merged_commits.json";
	public final static String IVY_PKG_MERGE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_pkg_all_merged_commits.json";
	public final static String IVY_ACDC_MERGE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Ivy/all_smells/ivy_acdc_all_merged_commits.json";
	
	/**
	 * Lucene file path
	 */
	public final static String[] LUCENE_PKG_PREFIX = { "src/core/src/java/org", "src/java/org", "src/main/java/org"}; 
	//input files
	public final static String LUCENE_ISSUE_FILE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/jira_issue/lucene.json";
	public final static String LUCENE_SMELL_PKG = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/lucene_pkg.json";
	public final static String LUCENE_SMELL_ACDC = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/lucene_acdc.json";
	public final static String LUCENE_SMELL_ARC = "/Users/felicitia/Google_Drive/Arcade/ICSE_2016_data/yixue_arch_result/class_smell_json/lucene_arc.json";
	public final static String LUCENE_ALL_PKG = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_pkg_all.json";
	public final static String LUCENE_ALL_ACDC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_acdc_all.json";
	public final static String LUCENE_ALL_ARC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_arc_all.json";

	//output files
	public final static String LUCENE_DEP_CON_ACDC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/dep_con_smells/lucene_acdc.json";
	public final static String LUCENE_DEP_CON_ARC = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/dep_con_smells/lucene_arc.json";
	public final static String LUCENE_DEP_CON_PKG = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/dep_con_smells/lucene_pkg.json";
	
	public final static String LUCENE_PKG_FILTER = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_pkg_all_filter_versions.json";
	public final static String LUCENE_ACDC_FILTER = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_acdc_all_filter_versions.json";
	public final static String LUCENE_ARC_FILTER = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_arc_all_filter_versions.json";
	
	public final static String LUCENE_ARC_MERGE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_arc_all_merged_commits.json";
	public final static String LUCENE_PKG_MERGE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_pkg_all_merged_commits.json";
	public final static String LUCENE_ACDC_MERGE = "/Users/felicitia/Google_Drive/Arcade/ASE_2016/ASE_2016_data/Lucene/all_smells/lucene_acdc_all_merged_commits.json";
	
}
