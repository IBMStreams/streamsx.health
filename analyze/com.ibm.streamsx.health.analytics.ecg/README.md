# ECG Java R-Peak Detector

This service performs R-Peak detection of an ECG waveform, using the [OSEA-4-Java](https://github.com/MEDEVIT/OSEA-4-Java) library.

# Running the Service

1.  `cd com.ibm.streamsx.health.analytics.ecg`
1.  `gradle build`
1.  `cd output`
1.  You will find a *.sab file in the output directory, submit this *.sab file to the Streams Instance.
