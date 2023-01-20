import subprocess
import sys

from typing import List
from constants import SUBJECT_SYSTEMS_ROOT

def run_dir_cleaner(system_name: str, safe_mode: str, ignore_patterns: List[str]):
  """
  Run the DirCleaner utility on a given system. DirCleaner will delete any subdirectories that contain the word "test" in their name. Safe mode will create a list of directories that would be deleted, but will not perform the deletion.

  Parameters:
    system_name: The name of the system to run DirCleaner on.
    safe_mode: A string indicating whether to run DirCleaner in safe mode or not.
               If set to "off", DirCleaner will run in normal mode.
               If set to "on", DirCleaner will run in safe mode.
    ignore_patterns: A list of string patterns to ignore during clean-up.
  """
  if safe_mode == "off":
    command = ["java", "-cp", "ARCADE_Core.jar", "edu.usc.softarch.arcade.util.DirCleaner", f"{SUBJECT_SYSTEMS_ROOT}/{system_name}", f"{SUBJECT_SYSTEMS_ROOT}/{system_name}/dir_cleaner_output.txt", "false"]
    command.extend(ignore_patterns)
    subprocess.run(command)
  else:
    command = ["java", "-cp", "ARCADE_Core.jar", "edu.usc.softarch.arcade.util.DirCleaner", f"{SUBJECT_SYSTEMS_ROOT}/{system_name}", f"{SUBJECT_SYSTEMS_ROOT}/{system_name}/dir_cleaner_output.txt", "true"]
    command.extend(ignore_patterns)
    subprocess.run(command)

if len(sys.argv) > 2:
    ignore_patterns = sys.argv[2:]
else:
    ignore_patterns = []

run_dir_cleaner(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else "on", ignore_patterns)
