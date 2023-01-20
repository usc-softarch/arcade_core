import subprocess
import sys
import glob
import os

from constants import FACTS_ROOT, CLUSTERS_ROOT, SMELLS_ROOT, time_print

def run_smell_detector(system_name: str, algo: str):
  """
  Run smell detection on a given system for a given algorithm's results.

  Parameters:
    system_name: The name of the system to run ArchSmellDetector on.
    algo: The algorithm used for clustering.
  """
  version_list = glob.glob(f"{CLUSTERS_ROOT}/{system_name}/{algo}/*")

  for version_dir in version_list:
    cluster_file = glob.glob(f"{version_dir}/*_clusters.rsf")[0]
    parts = os.path.basename(cluster_file).split('_')
    version = parts[0]
    measure = parts[1]

    if measure == "ARC":
      size = parts[2]
      doc_topics = f"{CLUSTERS_ROOT}/{system_name}/{version}_{measure}_{size}_clusteredDocTopics.json"
    else:
      doc_topics = ""

    command = f"java -cp ARCADE_Core.jar edu.usc.softarch.arcade.antipattern.detection.ArchSmellDetector {FACTS_ROOT}/{system_name}/{version}_deps.rsf {cluster_file} {SMELLS_ROOT}/{system_name}/{algo}/{version}_smells.json {doc_topics}"
    subprocess.run(command.split())

def run_all(system_name: str):
  """
  Run smell analysis on the results of all clustering algorithms for a given system.

  Parameters:
    system_name: The name of the system to run smell analysis on.
  """
  time_print(f"Running smell detection for {system_name} with ACDC.")
  run_smell_detector(system_name, "acdc")
  time_print(f"Running smell detection for {system_name} with ARC.")
  run_smell_detector(system_name, "arc")
  time_print(f"Running smell detection for {system_name} with Limbo.")
  run_smell_detector(system_name, "limbo")
  time_print(f"Running smell detection for {system_name} with PKG.")
  run_smell_detector(system_name, "pkg")
  print()
  time_print(f"Finished smell detection for {system_name}.")

run_all(sys.argv[1])