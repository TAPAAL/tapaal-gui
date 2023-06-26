#!/bin/bash
#Create a branch and add it to eclipse

eclipseworkspace=/home/kyrke/.eclipse-workspace/workspace-tapaal/
tapaalsoruces=/home/kyrke/projects/tapaal/src/
branchtodir=/home/kyrke/projects/tapaal/src/
branchfromdir=/home/kyrke/projects/tapaal/src/
defaultbranchfrom=/home/kyrke/projects/tapaal/src/tapaal

function addToEclpse(){

#$1 = workspace
#$2 = sourcedir

eclipse -nosplash -data /path/to/your/workspace/directory -application org.eclipse.cdt.managedbuilder.core.headlessbuild -data "$1" -import "$2"

}

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

brancname=$(takeInput "Branch Name")
#Tjek den ikke er oprettet.

bzr pull -d $defaultbranchfrom || exit -1
bzr branch $defaultbranchfrom "$branchtodir/$brancname"
cp $defaultbranchfrom/.project "$branchtodir/$brancname"
cp $defaultbranchfrom/.classpath "$branchtodir/$brancname"

sed -i -e "3,3s/.*/\t<name>${brancname}<\/name>/g" "$branchtodir/$brancname/.project"

#Check that no eclipse is running
wmctrl -l | grep Eclipse | cut -d \  -f1 | xargs wmctrl -i -c

numberOfEclipse=`ps axu | grep Eclipse | wc -l`

if [[ $numberOfEclipse != 1 ]]; then
	echo not updating eclipse as its running
	exit -1
fi

addToEclpse $eclipseworkspace "$branchtodir/$brancname"

eclipse &

