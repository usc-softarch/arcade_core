import os
import re

def rename_directories(path, regex_pattern_from, regex_pattern_to):
    # Ensure the provided path is a directory
    if not os.path.isdir(path):
        print(f"Error: '{path}' is not a valid directory.")
        return

    # Compile the regular expressions
    regex_from = re.compile(regex_pattern_from)

    # Iterate over subdirectories
    for subdir in os.listdir(path):
        subdir_path = os.path.join(path, subdir)

        # Check if it's a directory and matches the first regex
        if os.path.isdir(subdir_path) and regex_from.match(subdir):
            # Generate the new name using the second regex
            new_name = regex_from.sub(regex_pattern_to, subdir)

            # Construct the new path
            new_subdir_path = os.path.join(path, new_name)

            # Rename the directory
            os.rename(subdir_path, new_subdir_path)

            print(f"Renamed: {subdir_path} to {new_subdir_path}")

# Example usage
path_to_dir = '/path/to/your/directory'
regex_pattern_from = r'apache_maven-\d+\.\d+\.\d+-DEV'
regex_pattern_to = r'\1.\2.\3'

rename_directories(path_to_dir, regex_pattern_from, regex_pattern_to)