#!/bin/sh
# python2 version
action=$1

if [ $action = "lib" ]
then
    # Don't provide a lib since the
    # libpython2.7 will be loaded
    # dynamically based upon $PYTHONHOME
    echo
elif [ $action = "libPath" ]
then
    # Don't provide a libPath to ensure
    # the sab is not tied to a fixed path
    echo
elif [ $action = "includePath" ]
then
    python-config --includes
fi
