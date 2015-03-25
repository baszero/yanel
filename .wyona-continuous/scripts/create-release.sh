#!/bin/sh

CURRENT_DIR=$( cd "$( dirname "$0" )" && pwd )
echo "Current directory: $CURRENT_DIR"

PARENT_DIR="$(dirname "$CURRENT_DIR")"
echo "Parent directory: $PARENT_DIR"

YANEL_SRC="$(dirname "$PARENT_DIR")"
echo "Yanel source directory: $YANEL_SRC"

YANEL_REVISION=c76b2afc69489824bc8f9c1f05ffdcffcfa07c04

cd $YANEL_SRC

./build.sh source-snapshot -Dyanel.revision=$YANEL_REVISION
#./build.sh bin-snapshot

scp build/source-snapshots/wyona-yanel-3.0.0-r${YANEL_REVISION}-src.zip wyona@www.yanel.org:src/yanel-node1/src/realms/yanel-website/data-repo/data/download/source-snapshots/.

#TODO: Update Yanel website http://www.yanel.org/en/download/unix.html
