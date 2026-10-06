> The original design for the command-line version of this project, written before any code. Kept as a record of the design process; the code has since moved on (see the README).

# Psuedocode

Is there a config file?
Yes: proceed
No:
    Ask for location of token file in minecraft launcher, store its path in a new file inside PWD.
    Are there multiple profiles in the accounts.json? If so then ask which one to use. Default to 0.

Is token active? "Checking for token in [filepath]..."
Yes: "Welcome, [api.username]." proceed
No: "Please sign in to your minecraft launcher and try again." Terminate

"Please enter the file path to the skin you'd like to equip: "
Check that file exists
Check that file is .png
Check that file is 64x64 resolution
Check pixel at 54,20
Does pixel have alpha=255?
Yes: isslim=false
No: isslim=true

if isslim:
    "It looks like this skin is designed to have slim arms. Is this correct? (Enter Y/IDK/N): "
    Y: "Selecting slim arms..." proceed
    IDK: "Selecting slim arms..." proceed
    N: "Selecting classic arms..." isslim=false
else:
    "It looks like this skin is designed to have classic arms. Is this correct? (Enter Y/IDK/N): "
    Y: "Selecting classic arms..." proceed
    IDK: "Selecting classic arms..." proceed
    N: "Selecting slim arms..." isslim=true

"Uploading skin to Mojang servers" API call
Check API response and say if it succeeded, timed out, or failed.

# Classes and functions

Main
    main

Config


SkinFile


ApiClient

