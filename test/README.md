# Platform Test

the `test-builds.py` script builds all of the projects in the platform. Each project is built independently of any other project. This ensures that the project contains all of the necessary dependencies in it's `build.gradle` file. 

Here are the steps taken by the test: 

  1. Finds all `build.gradle` files in the platform
  1. For each `build.gradle` file,: 
    1. Executes `gradle clean` from the root directory to initially clean all projects
    1. For the given `build.gradle` file, executes `gradle build` to run a build for that specific project

# Running

To run the test, execute the following: 

`python test-builds.py`
