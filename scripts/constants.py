from datetime import datetime

FACTS_ROOT = "facts"
CLUSTERS_ROOT = "clusters"
SUBJECT_SYSTEMS_ROOT = "subject_systems"
STOPWORDS_DIR_PATH = "stopwords"
METRICS_ROOT = "metrics"
SMELLS_ROOT = "smells"

def time_print(text: str):
  now = datetime.now()
  strnow = now.strftime("%H:%M:%S")
  print(f"{strnow}: {text}")
