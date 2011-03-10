#!/bin/bash

name=tapaal
version=1.4.1

dirname=$name-$version
tmpdir=/tmp/$dirname

mkdir $tmpdir
bzr export $tmpdir


tar czf ${name}_$version.orig.tar.gz -C /tmp/ $dirname

rm -rf $tmpdir

