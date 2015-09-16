# Android Linters

Android Linters is a project which contains custom linters for an Android
project.

## Why use it?

This project allows you to prevent some runtime errors.
For example, if you forgot to write a variable in your
[Parcelable](http://developer.android.com/reference/android/os/Parcelable.html)
class the
linter will tell you what variable you forgot to write.

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

#v1.1.1 Fix reading private method in ParcelDetector
InstanceStateDetector: Add temporal fix when checking for local variables

#v1.1.0 Add Readme, script for download the jar and licenses.

#v1.0.0 Add ParcelDetector, FactoryMethodDetector, InstanceStateDetector,
ManifestDetector

# Copyright and License
Copyright 2010-2015 Monits.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this work except in compliance with the License. You may obtain a copy of the
License at:

http://www.apache.org/licenses/LICENSE-2.0
