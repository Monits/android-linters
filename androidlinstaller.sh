#!/bin/bash
ANDROID_LINTER_METADATA_URL=http://nexus.monits.com/content/repositories/oss-releases/com/monits/android-linters/maven-metadata.xml
ANDROID_LINT_DIR=$HOME/.android/lint
ANDROID_LINTER_RELEASE_URL=http://nexus.monits.com/content/repositories/oss-releases/com/monits/android-linters
ANDROID_JAR_FILE_NAME=android-linters
ANDROID_JAR_OUTPUT_PATH=$ANDROID_LINT_DIR/$ANDROID_JAR_FILE_NAME.jar
ANDROID_JAR_OUTPUT_PATH_TEMP=`mktemp`
ERROR_STATUS_CODE=4
ERROR_MESSAGE="There was a problem. Please try again later"

function abort_on_error {
    rm $ANDROID_JAR_OUTPUT_PATH_TEMP
    echo $ERROR_MESSAGE
    exit $ERROR_STATUS_CODE
}

# Download the metadata into a variable
metadata=$(wget --output-document - $ANDROID_LINTER_METADATA_URL)

if [ $? -ne 0 ]; then
    abort_on_error
fi

# Extract the last version number
version=$(echo "$metadata" | grep "<version>" | tail -n 1 | sed -n 's|\s*<version>\(.*\)</version>|\1|p')

version="${version#${version%%[![:space:]]*}}"   # remove leading whitespace characters
version="${version%${version##*[![:space:]]}}"   # remove trailing whitespace characters

# Create the jar url
android_jar_url=$ANDROID_LINTER_RELEASE_URL/$version/$ANDROID_JAR_FILE_NAME-$version.jar

# Create lint directory if does not exists

if [ ! -d "$ANDROID_LINT_DIR" ]; then
	echo "Creating lint directory in $HOME/.android"
	mkdir -p $ANDROID_LINT_DIR
fi

echo "Installing Android Linters $version"
 
# The Jar is downloaded to a temporary directory to avoid stepping over the
# original. So if any problem ocurrs during download, and the file is
# corrupted, the original remains untouched.

wget -O $ANDROID_JAR_OUTPUT_PATH_TEMP $android_jar_url

if [ $? -eq 0 ]; then
	mv $ANDROID_JAR_OUTPUT_PATH_TEMP $ANDROID_JAR_OUTPUT_PATH	
else
    abort_on_error
fi
