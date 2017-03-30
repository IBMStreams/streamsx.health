import glob
from subprocess import call

test_failures = {}
test_successes = {}

files = [file for file in glob.glob('../**/build.gradle', recursive=True)]
for f in files:
    if f.startswith('../test'):
        continue

    # clean all projects in the platform before executing build
    print("Cleaning all projects first...")
    call(['../gradlew', '-p', '../', 'clean'])

    print("Executing " + f + "...")
    rc = call(['../gradlew', '-b', f, 'build'])
    if rc == 0:
        test_successes[f] = rc
    else:
        test_failures[f] = rc

    print("Return code: " + str(rc))

print("FAILURES:")
for key in test_failures:
    print(key + ": " + "FAILED(rc=" + str(test_failures[key]) + ")!")

print("\n\n")
print("SUCCESSES:")
for key in test_successes:
    print(key + ": PASS")

