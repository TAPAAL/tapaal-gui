#!/bin/bash 

name=tapaal

function takeInput() {

	local input

	echo -n $1:\ >&2
	read input 

	while [[ -z $input ]]; do
		echo -n $1:\ >&2
		read input
	done;
	echo $input
}

function takeInputDefault() {

	local input

	echo -n $1 [$2]:\ >&2
	read input
	if [[ -z $input ]]; then
		input=$2
	fi;

	echo $input
}

#Get version number
version=$(takeInput "Version number [x.y.z]")
echo ${version}


#Get the path to the source 
proposeddir=`( cd ..; pwd  )`
sourcedir=`takeInputDefault "Path to soruce" $proposeddir`

while [ ! -d $sourcedir ]; do 
	echo Directory not found
	sourcedir=`takeInputDefault "Path to soruce" $proposeddir`
done

#Make temp place to
tempdir=`mktemp -d` || exit -1

#Make package dir
tmpsourcedir=${name}-${version}
tmpsourcedirfull=${tempdir}/$tmpsourcedir

#Export from bzr
bzr export $tmpsourcedirfull $sourcedir

#Removed files not to include in source dir
rm -rf "$tmpsourcedirfull/other"

tar czf $tempdir/${name}-${version}.tar.gz -C ${tempdir} $tmpsourcedir

#Upload the source file to launchpad

#Close bugs after upload

#Keep source tar.gz?

#Cleanup
if [ -e $tempdir ]; then
	rm -rf "$tempdir"
fi
