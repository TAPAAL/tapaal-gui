#!/usr/bin/python 

# Copyright (c) 2009 Canonical Ltd.
#
# This program is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2, or (at your option) any
# later version.
#
# lp-project-upload is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.

# Authors:
#  Martin Pitt <martin.pitt@ubuntu.com>, based on
#  http://blog.launchpad.net/api/recipe-for-uploading-files-via-the-api
#  Kenneth Yrke Joergensen <kenneth@yrke.dk>


''' Close all bugs marked fix committed, this program is based on
lp-project-upload '''


import datetime
import os
import subprocess
import sys
import tempfile

from launchpadlib.launchpad import Launchpad
from launchpadlib.errors import HTTPError

def main():
    if len(sys.argv) != 3:
        print >> sys.stderr, '''Error in input
Usage: closeBugs.py <projectname> <version>'''
        sys.exit(1)

    (project, version) = sys.argv[1:]


    try:
        launchpad = Launchpad.login_with('closeBugScript', 'production')
    except Exception, error:
        print >> sys.stderr, 'Could not connect to Launchpad:', str(error)
        sys.exit(2)


    try:
        # Look up the project using the Launchpad instance.
        proj = launchpad.projects[project]

        # Find the release in the project's releases collection.
        release = None
        for rel in proj.releases:
            if rel.version == version:
                release = rel
                break
        if not release:
            print >> sys.stderr, "Can't find release"
            sys.exit(1)


        # Mark any fix committed bugs released
        for task in release.milestone.searchTasks(status="Fix Committed"):
            print "Marking bug " + str(task.bug.id) + " as 'Fix Released'"
            task.status = "Fix Released"
            task.lp_save()

        
 

    except HTTPError, error:
        print 'An error happened in the upload:', error.content
        sys.exit(1)


if __name__ == '__main__':
    main()
