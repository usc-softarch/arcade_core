import glob
import subprocess
import shutil
import sys
import shlex
import os

from constants import FACTS_ROOT, CLUSTERS_ROOT, time_print

def find_invalid(path1: str, path2: str, path3: str, path4: str):
  # Read the contents of all four files into lists of lines
  with open(path1) as f1, open(path2) as f2, open(path3) as f3, open(path4) as f4:
    lines1 = f1.readlines()
    lines2 = f2.readlines()
    lines3 = f3.readlines()
    lines4 = f4.readlines()

  # Extract the <2> strings from each line
  strings1 = [line.split()[2] for line in lines1]
  strings2 = [line.split()[2] for line in lines2]
  strings3 = [line.split()[2] for line in lines3]
  strings4 = [line.split()[2] for line in lines4]

  # Convert the lists of strings into sets
  set1 = set(strings1)
  set2 = set(strings2)
  set3 = set(strings3)
  set4 = set(strings4)

  # Find the <2> strings that are present in one set but not the other
  missing_in_set1 = set2.difference(set1).union(set3.difference(set1)).union(set4.difference(set1))
  missing_in_set2 = set1.difference(set2).union(set3.difference(set2)).union(set4.difference(set2))
  missing_in_set3 = set1.difference(set3).union(set2.difference(set3)).union(set4.difference(set3))
  missing_in_set4 = set1.difference(set4).union(set2.difference(set4)).union(set3.difference(set4))

  # Print the missing strings
  time_print(f"Missing in ARC, of size {len(set1)}: {missing_in_set1}")
  time_print(f"Missing in Limbo, of size {len(set2)}: {missing_in_set2}")
  time_print(f"Missing in ACDC, of size {len(set3)}: {missing_in_set3}")
  time_print(f"Missing in PKG, of size {len(set4)}: {missing_in_set4}")

def validate_results(system_name: str):
  time_print("Validating results.")
  path1 = f"{CLUSTERS_ROOT}/{system_name}/arc"
  path2 = f"{CLUSTERS_ROOT}/{system_name}/limbo"
  path3 = f"{CLUSTERS_ROOT}/{system_name}/acdc"
  path4 = f"{CLUSTERS_ROOT}/{system_name}/pkg"

  result = True

  if not (os.path.isdir(path1) and os.path.isdir(path2) and os.path.isdir(path3) and os.path.isdir(path4)):
    time_print("Failed to find any results for one or more techniques.")
    result = False
    
  subdirs1 = os.listdir(path1)
  subdirs2 = os.listdir(path2)
  subdirs3 = os.listdir(path3)
  subdirs4 = os.listdir(path4)
    
  if len(subdirs1) != len(subdirs2) or len(subdirs1) != len(subdirs3) or len(subdirs1) != len(subdirs4):
    time_print("One or more techniques have a different number of results.")
    result = False
    
  for subdir1, subdir2, subdir3, subdir4 in zip(subdirs1, subdirs2, subdirs3, subdirs4):
    file1 = os.path.join(path1, subdir1, f"{system_name}-{subdir1}_JS_50_clusters.rsf")
    file2 = os.path.join(path2, subdir2, f"{system_name}-{subdir2}_IL_50_clusters.rsf")
    file3 = os.path.join(path3, subdir3, f"{system_name}-{subdir3}_ACDC_clusters.rsf")
    file4 = os.path.join(path4, subdir4, f"{system_name}-{subdir4}_PKG_clusters.rsf")
        
    if not (os.path.exists(file1) and os.path.exists(file2) and os.path.exists(file3) and os.path.exists(file4)):
      time_print(f"Either {file1}, {file2}, {file3}, or {file4} does not exist.")
      result = False
        
    with open(file1) as f1, open(file2) as f2, open(file3) as f3, open(file4) as f4:
      num_lines1 = len(f1.readlines())
      num_lines2 = len(f2.readlines())
      num_lines3 = len(f3.readlines())
      num_lines4 = len(f4.readlines())
        
    if num_lines1 != num_lines2 or num_lines1 != num_lines3 or num_lines1 != num_lines4:
      time_print(f"{file1}, {file2}, {file3}, and {file4} have a different number of entities.")
      time_print(find_invalid(file1, file2, file3, file4))
      print()
      result = False
    
  return result

def validate_results_single(system_name: str, version: str):
  file1 = f"{CLUSTERS_ROOT}/{system_name}/arc/{version}/{system_name}-{version}_JS_50_clusters.rsf"
  file2 = f"{CLUSTERS_ROOT}/{system_name}/limbo/{version}/{system_name}-{version}_IL_50_clusters.rsf"
  file3 = f"{CLUSTERS_ROOT}/{system_name}/acdc/{version}/{system_name}-{version}_ACDC_clusters.rsf"
  file4 = f"{CLUSTERS_ROOT}/{system_name}/pkg/{version}/{system_name}-{version}_PKG_clusters.rsf"

  result = True

  if not (os.path.exists(file1) and os.path.exists(file2) and os.path.exists(file3) and os.path.exists(file4)):
    time_print(f"Either {file1}, {file2}, {file3}, or {file4} does not exist.")
    result = False

  with open(file1) as f1, open(file2) as f2, open(file3) as f3, open(file4) as f4:
    num_lines1 = len(f1.readlines())
    num_lines2 = len(f2.readlines())
    num_lines3 = len(f3.readlines())
    num_lines4 = len(f4.readlines())

  if num_lines1 != num_lines2 or num_lines1 != num_lines3 or num_lines1 != num_lines4:
    time_print(f"{file1}, {file2}, {file3}, and {file4} have a different number of entities.")
    time_print(find_invalid(file1, file2, file3, file4))
    print()
    result = False

  return result
			
def run_clusterer(project_name: str, project_version: str, language: str, measure: str, algorithm: str, memory: str):
  """
  Runs the Clusterer interface for the specified project version.

  Parameters:
    - project_name (str): The name of the project.
    - project_version (str): The version of the project.
    - language (str): The programming language of the project.
    - measure (str): The similarity measure to use with the algorithm.
    - algorithm (str): The clustering algorithm to use.
    - artifacts (str, optional): The path to the artifacts directory. Required for ARC.
    - reassign_version (bool, optional): Whether to reassign version of the DocTopics for ARC.
  """
  command = (
    f"java -Xmx{memory}g -cp ARCADE_Core.jar "
    f"edu.usc.softarch.arcade.clustering.Clusterer "
    f"algo={algorithm} "
    f"language={language} "
    f"deps={FACTS_ROOT}/{project_name}/{project_name}-{project_version}_deps.rsf "
    f"measure={measure} "
    f"projname={project_name} "
    f"projversion={project_version} "
    f"projpath={CLUSTERS_ROOT}/{project_name}/{algorithm}/{project_version} "
    "printdots=true "
  )
  if algorithm == "arc":
    command += f"artifacts={FACTS_ROOT}/{project_name}/artifacts "
    command += "reassignversion=true"
  time_print(f"Running {algorithm} on {project_name}-{project_version}.")
  subprocess.run(shlex.split(command))

def run_structural(project_name: str, project_version: str, language: str, measure: str, algorithm: str, memory: str):
  """
  Runs a structural clustering algorithm (Limbo or WCA) for the specified project.

  Parameters:
    - project_name (str): The name of the project.
    - project_version (str): The version of the project.
    - package_prefix (str): The package prefix to use for filtering entities.
    - language (str): The programming language of the project.
    - measure (str): The similarity measure to use with the algorithm.
    - algorithm (str): The clustering algorithm to use.
  """
  run_clusterer(project_name, project_version, language, measure, algorithm, memory)
  
def run_arc(project_name: str, project_version: str, language: str, measure: str, memory: str):
  """
  Runs the ARC clustering algorithm for the specified project.

  Parameters:
    - project_name (str): The name of the project.
    - project_version (str): The version of the project.
    - package_prefix (str): The package prefix to use for filtering entities.
    - language (str): The programming language of the project.
    - measure (str): The similarity measure to use with arc.
  """
  run_clusterer(project_name, project_version, language, measure, "arc", memory)

def run_acdc(project_name: str, project_version: str):
  """
  Runs the ACDC clustering algorithm, generating clusters.rsf result and .svg files for each cluster.

  Parameters:
    - project_name (str): The name of the project.
    - project_version (str): The version of the project.
  """
  command = (
    f"java -cp ARCADE_Core.jar "
    f"edu.usc.softarch.arcade.clustering.acdc.ACDC "
    f"{FACTS_ROOT}/{project_name}/{project_name}-{project_version}_deps.rsf "
    f"{CLUSTERS_ROOT}/{project_name}/acdc/{project_version}/{project_name}-{project_version}_ACDC_clusters.rsf"
  )
  time_print(f"Running ACDC on {project_name}-{project_version}.")
  subprocess.run(shlex.split(command))
  
def run_pkg(project_name: str, project_version: str, language: str):
  """
  Runs the PKG clustering algorithm, generating clusters.rsf result and .svg files for each cluster.

  Parameters:
    - project_name (str): The name of the project.
    - project_version (str): The version of the project.
    - language (str): The programming language of the project.
  """
  command = (
    f"java -cp ARCADE_Core.jar "
    f"edu.usc.softarch.arcade.clustering.Pkg "
    f"projectname={project_name} "
    f"projectversion={project_version} "
    f"projectpath={CLUSTERS_ROOT}/{project_name}/pkg/{project_version} "
    f"depspath={FACTS_ROOT}/{project_name}/{project_name}-{project_version}_deps.rsf "
    f"language={language}"
  )
  time_print(f"Running PKG on {project_name}-{project_version}.")
  subprocess.run(shlex.split(command))

  # Rename clusters output file to remove the size
  for entry in glob.glob(f"{CLUSTERS_ROOT}/{project_name}/pkg/{project_version}/{project_name}-{project_version}_PKG*_clusters.rsf"):
    shutil.move(entry, f"{CLUSTERS_ROOT}/{project_name}/pkg/{project_version}/{project_name}-{project_version}_PKG_clusters.rsf")

def run_all(project_name: str, project_version: str, language: str, memory: str):
  """
  Runs all clustering algorithms for a single version of a system.

  Parameters:
    - project_name (str): The name of the project.
    - project_version (str): The version of the project.
    - package_prefix (str): The package prefix to use for filtering entities.
    - language (str): The programming language of the project.
  """
  run_acdc(project_name, project_version)
  run_arc(project_name, project_version, language, "js", memory)
  run_structural(project_name, project_version, language, "il", "limbo", memory)
  run_pkg(project_name, project_version, language)
  if (not validate_results_single(project_name, project_version)):
    sys.exit(1)

def cluster_system(system_name: str, language: str, memory: str):
  """
  Clusters the specified system for all available versions.

  Parameters:
    - system_name (str): The name of the system to cluster.
    - language (str): The programming language of the system.
    - package_prefix (str): The package prefix to use for filtering entities.
  """
  time_print(f"Initiating clustering of {system_name}.")
  print()
  system_root=f"{FACTS_ROOT}/{system_name}"
  
  for entry in glob.glob(f"{system_root}/{system_name}-*"):
    prefix = f"{system_root}/{system_name}-"
    version = entry[len(prefix):]
    version = version.replace("_deps.rsf", "")

    time_print(f"Starting clustering for {system_name}-{version}.")
    run_all(system_name, version, language, memory)
    print()

  if (validate_results(system_name)):
    time_print("Results validated successfully.")
    time_print("Clustering completed.")
  else:
    time_print("Failed to validate results.")

system_name = sys.argv[1]
language = sys.argv[2]
if len(sys.argv) > 3:
    memory = sys.argv[3]
else:
    memory = "4"

cluster_system(system_name, language, memory)
