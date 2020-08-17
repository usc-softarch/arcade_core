package edu.usc.softarch.arcade.classgraphs;

/**
 * @author joshua
 */
public class TopicModelData {
	// #region FIELDS ------------------------------------------------------------
	private String docTopicsFilename;
	private String topicKeysFilename;
	// #endregion FIELDS ---------------------------------------------------------

	// #region ACCESSORS ---------------------------------------------------------
	public String getDocTopicsFilename() { return this.docTopicsFilename; }
	public String getTopicKeysFilename() { return this.topicKeysFilename; }

	public void setDocTopicsFilename(String docTopicsFilename) {
		this.docTopicsFilename = docTopicsFilename; }
	public void setTopicKeysFilename(String topicKeysFilename) {
		this.topicKeysFilename = topicKeysFilename; }
	// #endregion ACCESSORS ------------------------------------------------------
}