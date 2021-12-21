#!/bin/bash 


function takeInputTrueFalse() {

	local input 
	local output 
	

	if [ -z $2 ]; then 
		output="$1 [y/N]: "
	else
		output="$1 [Y/n]: "
	fi

	input="abemad"
	
	until [[ -z $input || $input == "y" || $input == "n" ]]; do
		echo -n $output >&2
		read input 
	done;
	
	if [[ -z $input ]]; then
		if [[ -z $2 ]]; then
			input="n"
		else 
			input="y"
		fi
	fi
	
	echo $input
	
	

}

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

echo This scripts is used to create releases on launchpad from a milestone.
echo 
echo The script creates a source tarball
echo Creates a relase in launchpad 
echo Uploades the source
echo Creates a new milestone for tracking new bugs
echo Closes all bugs marked fix committed related to the milestone 
echo 
echo All events are optinal, and you need to confirm all actions

#Get program name
name=$(takeInputDefault "Name" "tapaal")

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
tarball=$tempdir/${name}-${version}.tar.gz

tar czf $tarball -C ${tempdir} $tmpsourcedir

#Upload the source file to launchpad

upLoadToLaunchpad=$(takeInputTrueFalse "Upload to launchpad" "y")
if [[ $upLoadToLaunchpad == "y" ]]; then

	unset newVersion
	makeNewMilestone=$(takeInputTrueFalse "Create new milestone" "y")
	if [[ $makeNewMilestone == "y" ]]; then
		newVersion=${version%.*}.$((${version##*.}+1))		
	fi

	lp-project-upload $name $version $tarball $newVersion
fi

#Close bugs after upload

closeAllRelatedBugs=$(takeInputTrueFalse "Make bugs with fix committed as fix
released" "y")

if [[ $closeAllRelatedBugs == "y" ]]; then 
	python closeBugs.py $name $version
fi

#Keep source tar.gz?
keepSource=$(takeInputTrueFalse "Keep source file")
if [[ $keepSource == "y" ]]; then
	cp $tempdir/${name}-${version}.tar.gz .
fi

#Cleanup
if [ -e $tempdir ]; then
	rm -rf "$tempdir"
fi
