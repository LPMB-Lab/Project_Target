TargetAccuracyTask
==============

### Description
An accuracy task that requires a user to hold their finger on a start position before a series of targets appear and disappear when the user lifts his/her finger to press them.

### Customizable Variables *Not Yet Implemented*
*You can find the 'settings.txt' file with the following variables that can be changed to whatever suits your study*

| Variable name | Description | Default Value |
| --- | --- | ------------- |
| length_targets | The number of targets to appear along the length | 6 |
| width_targets | The number of targets to appear along the width | 6 |
| circle_diameter | The size of the targets | 100 |

### Output
Example of output file with 7 trials attempted out of 36
```
Response Timings (ms): 1414, 1036, 949, 960, 1282, 1365, 1841, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
Reaction Timings (ms): 592, 473, 443, 456, 530, 714, 888, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
Points: 1.0, 0.5, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 
Entries: 32, 19, 18, 6, 11, 30, 31, 21, 13, 17, 35, 20, 9, 12, 14, 7, 16, 3, 15, 25, 28, 10, 34, 27, 2, 8, 24, 4, 33, 5, 0, 1, 23, 29, 26, 22, 
Fastest Response Time (ms): 949ms
Fastest Reaction Time (ms): 443ms 
```

Response Timings (ms)
- How long it takes from finger lift to pressing target

Reaction Timings (ms)
- How long it takes from target show to finger lift

Points
- The points accumulated for the trial, points are awarded as follows:
1.0 - Direct hit in the taget
0.5 - Non-direct hit, but within 125% of the radius
0.0 - Missed and not within 125% of radius

Entries
- Targets are enumerated from 1 to 36, this shows which target was presented. This is included if the researcher is looking to compare one users trials to another. For example data analysis can be done on multiple export files to see if entries 1-10 were pressed with more accuracy by ALL users.

Fastest Response Time
- Fastest Response time recorded

Fastest Reaction Time
- Fastest Reaction time recorded
