import os
import sys
import subprocess
import glob

from constants import FACTS_ROOT, SUBJECT_SYSTEMS_ROOT, STOPWORDS_DIR_PATH, time_print

def gen_doc_topics(project_name: str, artifacts_path: str):
  time_print("Generating DocTopics.")
  subprocess.run(["java", "-Xmx8g", "-cp", "ARCADE_Core.jar", "edu.usc.softarch.arcade.topics.DocTopics", "mode=generate", f"artifacts={artifacts_path}", f"project={project_name}", "filelevel=true"])

def run_understand(system_name: str, version: str, language: str):
  """
  Run SciTools Understand on a given system version to extract its dependencies.

  Parameters:
    system_name: The name of the system to run Understand on.
    version: The version of the system to run Understand on.
    language: The language of the system.
  """
  system_path = f"{SUBJECT_SYSTEMS_ROOT}/{system_name}"
  version_path = f"{system_path}/{version}"
  und_path = f"{version_path}/{version}.und"
  deps_path = f"{system_path}/deps/{version}_deps.csv"

  time_print(f"Creating UND project at {und_path}.")
  if language != "c":
    subprocess.run(["und", "-quiet", "create", "-languages", language, und_path])
  else:
    subprocess.run(["und", "-quiet", "create", "-languages", "c++", und_path])
  time_print(f"Adding all files from {version_path}.")
  subprocess.run(["und", "-quiet", "add", version_path, und_path])
  time_print(f"Running UND analysis.")
  subprocess.run(["und", "-quiet", "analyze", und_path])
  time_print(f"Exporting dependencies to {deps_path}.")
  subprocess.run(["und", "-quiet", "export", "-dependencies", "-format", "long", "file", "csv", deps_path, und_path])

def csv_to_rsf(input_path: str, output_path: str, project_root_name: str):
  """
  Convert a dependencies CSV file to an RSF file using the UnderstandCsvToRsf utility.

  Parameters:
    input_path: The path to the input CSV file.
    output_path: The path to the output RSF file.
    project_root_name: The root name of the project, used for filtering.
  """
  time_print("Parsing Understand CSV dependencies to RSF.")
  subprocess.run(["java", "-cp", "ARCADE_Core.jar", "edu.usc.softarch.arcade.facts.dependencies.UnderstandCsvToRsf", input_path, output_path, project_root_name])

def run_mallet(system_root: str, language: str, artifacts_output_path: str):
  """
  Run Mallet on a given system and generate artifacts.

  Parameters:
    system_root: The root directory of the system to run Mallet on.
    language: The language of the system.
    artifacts_output_path: The path to the output directory for the generated artifacts.
  """
  time_print(f"Running Mallet on path {system_root}.")
  subprocess.run(["java", "-cp", "ARCADE_Core.jar", "edu.usc.softarch.arcade.topics.MalletRunner", system_root, language, artifacts_output_path, STOPWORDS_DIR_PATH])

def extract_facts(system_name: str, language: str):
  """
  Extract facts from a given system.

  Parameters:
    system_name: The name of the system to extract facts from.
    language: The language of the system.
    project_root_name: The root name of the project, used for filtering.
  """
  system_root = f"{SUBJECT_SYSTEMS_ROOT}/{system_name}"
  facts_dir = f"{FACTS_ROOT}/{system_name}"
  time_print(f"Creating Facts directory at {facts_dir}.")
  os.makedirs(facts_dir, exist_ok=True)
  time_print(f"Creating Understand dependencies directory at {system_root}/deps.")
  os.makedirs(f"{system_root}/deps", exist_ok=True)
  print()

  # Parse system Understand dependencies
  for entry in glob.glob(f"{system_root}/{system_name}-*"):
    prefix = f"{system_root}/"
    version = entry[len(prefix):]
    time_print(f"Extracting dependencies for {version}.")

    # Dependencies
    input_path = f"{system_root}/deps/{version}_deps.csv"
    output_path = f"{FACTS_ROOT}/{system_name}/{version}_deps.rsf"
    run_understand(system_name, version, language)
    project_root_name = version
    csv_to_rsf(input_path, output_path, project_root_name)
    time_print(f"Dependencies extracted for {version}.")
    print()

  # Mallet
  artifacts_output_path = f"{FACTS_ROOT}/{system_name}/artifacts"
  os.makedirs(artifacts_output_path, exist_ok=True)
  run_mallet(system_root, language, artifacts_output_path)
  gen_doc_topics(system_name, artifacts_output_path)
  time_print("All facts extracted.")

system_name = sys.argv[1]
language = sys.argv[2]

extract_facts(system_name, language)
