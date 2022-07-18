package edu.usc.softarch.arcade.visualization.topics;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import edu.usc.softarch.arcade.topics.DocTopics;
import edu.usc.softarch.arcade.visualization.ArchitectureTableModel;
import edu.usc.softarch.arcade.visualization.clustering.ArchitectureViewer;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import java.io.IOException;

public class DocTopicsViewer extends ArchitectureViewer {
	//region PUBLIC INTERFACE
	public static void main(String[] args) throws IOException {
		String projectName = args[0];
		String projectVersion = args[1];
		String docTopicsPath = args[2];

		DocTopics.deserialize(docTopicsPath);

		LafManager.install(new DarculaTheme());
		DocTopicsViewer viewerPanel =
			new DocTopicsViewer(projectName, projectVersion);
		JFrame window = new JFrame(
			"DocTopics Viewer: " + projectName + "-" + projectVersion);
		window.setContentPane(viewerPanel);
		window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		window.pack();
		// Puts the window in the center of the screen
		window.setLocationRelativeTo(null);
		window.setVisible(true);
	}
	//endregion

	//region CONSTRUCTORS
	public DocTopicsViewer(String projectName, String projectVersion) {
		super(projectName, projectVersion); }
	//endregion

	//region PROCESSING
	@Override
	protected ArchitectureTableModel buildArchitectureTable() {
		return new DocTopicsTableModel(this.projectName, this.projectVersion); }

	@Override
	protected Integer[] getFeatureIndex() {
		int docTopicsLength = DocTopics.getSingleton(
			super.projectName, super.projectVersion).getNumTopics();

		Integer[] topicNumberList = new Integer[docTopicsLength];
		for (int i = 1; i < docTopicsLength + 1; i++)
			topicNumberList[i - 1] = i;

		return topicNumberList;
	}

	@Override
	protected String getIndexName() {
		return "Document:"; }

	@Override
	protected String getFeatureName() {
		return "Topic Item:"; }

	@Override
	protected String getFeatureRankText() {
		return "Highest Topic Item in selected document:"; }
	//endregion
}
