# Please run from parent dir of repos you would like to tag
import os
from subprocess import call

repos = os.listdir("C:\Work\diamond_dawn_workspace_git")

for path in repos:

    if path.endswith(".py"): # Its this script
        continue

    print str(path)
    os.chdir(str(path))

    # Tagging
    # call("git tag v1.2-beta")
    call("git push --tags")

    os.chdir("..") 