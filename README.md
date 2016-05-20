[![Build Status](https://secure.travis-ci.org/Monits/android-linters.svg?branch=development)](http://travis-ci.org/Monits/android-linters)
[![Coverage Status](https://coveralls.io/repos/Monits/android-linters/badge.svg?branch=development)](https://coveralls.io/r/Monits/android-linters?branch=development)
[![Download](https://api.bintray.com/packages/monits/monits-android/android-linters/images/download.svg) ](https://bintray.com/monits/monits-android/android-linters/_latestVersion)
# Android Linters

Android Linters is a project which contains custom linters for an Android
project.

## Why use it?

This project allows you to prevent some runtime errors.
For example, if you forgot to write a variable in your
[Parcelable](http://developer.android.com/reference/android/os/Parcelable.html)
class the linter will tell you what variable you forgot to write.

## Linters

### ParcelDetector

Check for malformed `Parcelable` implementations:
* You forgot to write a variable
* You forgot to read a variable
* You forgot to call super in your constructor or in `writeToParcel` method
* You read a different type than you wrote
* You write in a different order than you read

For each case, the linter will show in which line is the error and what is the
reason. 

### FactoryMethodDetector

Checks that any instantion of a fragment is done in
a factory method to prevent forgetting the necessary arguments 

### InstanceStateDetector

Makes sure instance state is properly saved and restored.
* You forgot to save a variable in your `onSaveInstanceState` method
* You forgot to restore a variable
* You overwrite a saved variable
* You overwrite a restored variable
* Save and restore different types

### ManifestDetector

It is a xml detector that checks if you request for duplicate permissions in
you `AndroidManifest.xml`

## Usage

In the project there is a script that download the jar with all the linters and
it will copy to your directory `$HOME/.android/lint/`

## History

###v1.1.9
 - Fix compatibility with latest android tools.
 - Fix NPE in `InstanceStateDetector` when not using constants as keys.
 - Not using constants as keys in `InstanceStateDetector` is now reported.

###v1.1.8
 - Use Java 7 instead of 8.

###v1.1.7
 - Fix scope of the detectors
 - Fix in ParcelDetector when check for missing calling super

###v1.1.6
 - Add checks to prevent get incorrect Fields

###v1.1.5
 - Fix an issue when we are restoring a state

###v1.1.4
 - Fix checking for getArguments method when gets the variables

###v1.1.3
 - Check when a state is restored in a local variable and then in a field

###v1.1.2
 - Fix checking for local variables in InstanceStateDetector

###v1.1.1
 - Fix reading private method in ParcelDetector
 - InstanceStateDetector: Add temporal fix when checking for local variables

###v1.1.0
 - Add Readme, script for download the jar and licenses.

###v1.0.0
 - Add ParcelDetector, FactoryMethodDetector, InstanceStateDetector,
ManifestDetector

# Copyright and License
Copyright 2010-2015 Monits.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this work except in compliance with the License. You may obtain a copy of the
License at:

http://www.apache.org/licenses/LICENSE-2.0
