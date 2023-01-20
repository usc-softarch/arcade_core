import subprocess
import sys
import pandas
import matplotlib.pyplot as plt
from scipy.stats import pearsonr

from constants import CLUSTERS_ROOT, FACTS_ROOT, METRICS_ROOT, time_print

def plot_a2a_next_version(system_name: str, algo: str):
  data1 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/a2a.csv")
  data2 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/weightedEdgea2a.csv")
  xaxis = data1[data1.columns[0]][1:]
  yaxis1 = []
  yaxis2 = []
  for i in range(1, len(data1.columns) - 1):
    yaxis1.append(data1[data1.columns[i]][i])
  for i in range(1, len(data2.columns) - 1):
    yaxis2.append(data2[data2.columns[i]][i])

  plt.figure(figsize=(20, 8))
  plt.plot(xaxis, yaxis1, label="a2a")
  plt.plot(xaxis, yaxis2, label="WEa2a")
  plt.title("a2a variant values between every subsequent version.")
  plt.xlabel("Version")
  plt.ylabel("Similarity")
  plt.ylim(0, 100)
  plt.yticks(range(0, 101, 10))
  plt.xticks(rotation=90)
  plt.legend()
  plt.savefig(f"{METRICS_ROOT}/{system_name}/{algo}/a2aNextV.png")

def plot_minor_versions(system_name: str, algo: str):
  data1 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/a2aMinor.csv")
  data2 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/weightedEdgea2aMinor.csv")

  x1 = data1[data1.columns[0]]
  y1 = data1[data1.columns[1]]
  y2 = data2[data2.columns[1]]

  plt.figure(figsize=(20, 8))
  plt.plot(x1, y1, label="a2a")
  plt.plot(x1, y2, label="WEa2a")
  plt.xlabel("Versions")
  plt.ylabel("Similarity")
  plt.ylim(0, 100)
  plt.yticks(range(0, 101, 10))
  plt.xticks(rotation=10)
  plt.legend()
  plt.savefig(f"{METRICS_ROOT}/{system_name}/{algo}/a2aMinor.png")

def plot_minor_difference(system_name: str, algo: str):
  data1 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/a2aMinor.csv")
  data2 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/edgea2aMinor.csv")
  data3 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/weightedEdgea2aMinor.csv")

  x1 = data1[data1.columns[0]]
  y1 = data1[data1.columns[1]]
  y2 = data2[data2.columns[1]]
  y3 = data3[data3.columns[1]]

  difference_e = y2 - y1
  max_value_e = max(abs(min(difference_e)), abs(max(difference_e)))
  ticks_e = range(-int(max_value_e), int(max_value_e)+1, 10)
  difference_w = y3 - y1
  max_value_w = max(abs(min(difference_w)), abs(max(difference_w)))
  ticks_w = range(-int(max_value_w), int(max_value_w)+1, 10)

  plt.figure(figsize=(20, 8))
  plt.plot(x1, difference_e)
  plt.xlabel("X-axis")
  plt.ylabel("Y-axis")
  plt.yticks(ticks_e)
  plt.xticks(rotation=10)
  plt.axhline(y=0, linestyle=":")
  plt.savefig(f"{METRICS_ROOT}/{system_name}/{algo}/a2aMinorEdgeDiff.png")

  plt.figure(figsize=(20, 8))
  plt.plot(x1, difference_w)
  plt.xlabel("X-axis")
  plt.ylabel("Y-axis")
  plt.yticks(ticks_w)
  plt.xticks(rotation=10)
  plt.axhline(y=0, linestyle=":")
  plt.savefig(f"{METRICS_ROOT}/{system_name}/{algo}/a2aMinorWEdgeDiff.png")

def calculate_correlation(system_name: str, algo: str):
  data1 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/a2a.csv", header=None)
  data2 = pandas.read_csv(f"{METRICS_ROOT}/{system_name}/{algo}/weightedEdgea2a.csv", header=None)
  data1 = data1.drop(data1.index[0]).drop(columns=data1.columns[0])
  data2 = data2.drop(data2.index[0]).drop(columns=data2.columns[0])

  series1 = []
  series2 = []
  for i in range(data1.shape[0]):
    for j in range(data1.shape[1]):
      if i < j:
        series1.append(float(data1.iloc[i, j]))
        series2.append(float(data2.iloc[i, j]))

  correlation = pearsonr(series1, series2)
  correlation_str = "PearsonRResult(statistic={}, pvalue={})".format(correlation[0], correlation[1])
  with open(f"{METRICS_ROOT}/{system_name}/{algo}/correlation.txt", "w") as f:
    f.write(correlation_str)

def generate_metrics(system_name: str, algo: str):
  """
  Calculate metrics for a given algorithm's results on a given system.

  Parameters:
    system_name: The name of the system to calculate metrics for.
    algo: The algorithm used for clustering.
  """
  system_dir_path = f"{CLUSTERS_ROOT}/{system_name}/{algo}"
  deps_dir_path = f"{FACTS_ROOT}/{system_name}"
  output_path = f"{METRICS_ROOT}/{system_name}/{algo}"
  command = f"java -cp ARCADE_Core.jar edu.usc.softarch.arcade.metrics.data.SystemMetrics systemdirpath={system_dir_path} depsdirpath={deps_dir_path} outputpath={output_path}"
  subprocess.run(command.split())
  plot_a2a_next_version(system_name, algo)
  plot_minor_versions(system_name, algo)
  plot_minor_difference(system_name, algo)
  calculate_correlation(system_name, algo)

def run_all(system_name: str):
  """
  Calculate metrics for the results of all algorithms on a given system.

  Parameters:
    system_name: The name of the system to calculate metrics for.
  """
  time_print(f"Calculating metrics for {system_name} with ACDC.")
  generate_metrics(system_name, "acdc")
  print()
  time_print(f"Calculating metrics for {system_name} with ARC.")
  generate_metrics(system_name, "arc")
  print()
  time_print(f"Calculating metrics for {system_name} with Limbo.")
  generate_metrics(system_name, "limbo")
  print()
  time_print(f"Calculating metrics for {system_name} with PKG.")
  generate_metrics(system_name, "pkg")
  print()
  time_print(f"Finished calculating metrics for {system_name}.")

run_all(sys.argv[1])
