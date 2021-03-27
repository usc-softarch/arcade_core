
mvn -Dtest=edu.usc.softarch.arcade.util.ldasupport.PipeExtractorTest test
./ext-tools/mallet-2.0.7/bin/mallet.bat import-dir --input subject_systems/Struts2/src/struts-2.3.30 --remove-stopwords TRUE --keep-sequence TRUE --output target/test_results/BatchClusteringEngineTest/struts-2.3.30/base/topicmodel.data

./ext-tools/mallet-2.0.7/bin/mallet.bat train-topics --input target/test_results/BatchClusteringEngineTest/struts-2.3.30/base/topicmodel.data --inferencer-filename target/test_results/BatchClusteringEngineTest/struts-2.3.30/base/infer.mallet --num-top-words 50 --num-topics 100 --num-threads 3 --num-iterations 100 --doc-topics-threshold 0.1



./ext-tools/mallet-2.0.7/bin/mallet.bat import-dir --input subject_systems/Struts2/src/struts-2.5.2 --remove-stopwords TRUE --keep-sequence TRUE --output target/test_results/BatchClusteringEngineTest/struts-2.5.2/base/topicmodel.data

./ext-tools/mallet-2.0.7/bin/mallet.bat train-topics --input target/test_results/BatchClusteringEngineTest/struts-2.5.2/base/topicmodel.data --inferencer-filename target/test_results/BatchClusteringEngineTest/struts-2.5.2/base/infer.mallet --num-top-words 50 --num-topics 100 --num-threads 3 --num-iterations 100 --doc-topics-threshold 0.1



./ext-tools/mallet-2.0.7/bin/mallet.bat import-dir --input subject_systems/httpd/src/httpd-2.4.26 --remove-stopwords TRUE --keep-sequence TRUE --output target/test_results/BatchClusteringEngineTest/httpd-2.4.26/base/topicmodel.data

./ext-tools/mallet-2.0.7/bin/mallet.bat train-topics --input target/test_results/BatchClusteringEngineTest/httpd-2.4.26/base/topicmodel.data --inferencer-filename target/test_results/BatchClusteringEngineTest/httpd-2.4.26/base/infer.mallet --num-top-words 50 --num-topics 100 --num-threads 3 --num-iterations 100 --doc-topics-threshold 0.1



./ext-tools/mallet-2.0.7/bin/mallet.bat import-dir --input subject_systems/httpd/src/httpd-2.3.8 --remove-stopwords TRUE --keep-sequence TRUE --output target/test_results/BatchClusteringEngineTest/httpd-2.3.8/base/topicmodel.data

./ext-tools/mallet-2.0.7/bin/mallet.bat train-topics --input target/test_results/BatchClusteringEngineTest/httpd-2.3.8/base/topicmodel.data --inferencer-filename target/test_results/BatchClusteringEngineTest/httpd-2.3.8/base/infer.mallet --num-top-words 50 --num-topics 100 --num-threads 3 --num-iterations 100 --doc-topics-threshold 0.1
