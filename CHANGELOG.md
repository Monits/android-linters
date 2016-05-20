# History

##v1.2.0 - unreleased
 - Added NeedlessNullnessDetector to report on needless @Nullable / @NonNull
    annotations on primitive / void parameters and return types.

##v1.1.9
 - Fix compatibility with latest android tools.
 - Fix NPE in `InstanceStateDetector` when not using constants as keys.
 - Not using constants as keys in `InstanceStateDetector` is now reported.

##v1.1.8
 - Use Java 7 instead of 8.

##v1.1.7
* Fix scope of the detectors
* Fix in ParcelDetector when check for missing calling super

##v1.1.6
* Add checks to prevent get incorrect Fields

##v1.1.5 
* Fix an issue when we are restoring a state

##v1.1.4
* Fix checking for getArguments method when gets the variables

##v1.1.3
* Check when a state is restored in a local variable and then in a field

##v1.1.2
* Fix checking for local variables in InstanceStateDetector

##v1.1.1
* Fix reading private method in ParcelDetector
* InstanceStateDetector: Add temporal fix when checking for local variables

##v1.1.0
* Add Readme, script for download the jar and licenses.

##v1.0.0
* Initial release
