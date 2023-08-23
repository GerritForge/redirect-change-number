#!/bin/bash

# Copyright (C) 2023 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


# The purpose of this script is to extract the current changes from the Eclipse
# Foundation Gerrit for the projects:
# - jgit/jgit
# - egit/egit

TMP_FOLDER=/tmp
ECLIPSE_FOUNDATION_GERRIT_URL=https://git.eclipse.org/r/

function export_changes_number_from_eclipse_foundation {
    SOURCE_PROJECT_GIT_URL=$ECLIPSE_FOUNDATION_GERRIT_URL$1
    DESTINATION_PROJECT=GerritCodeReview/$2
    DESTINATION_CSV_FILE=$TMP_FOLDER/$2.csv
    git ls-remote $SOURCE_PROJECT_GIT_URL | grep changes | awk  '{print $2}' | awk -F '/' '{print $4}' | uniq | sort -n | awk 'NF{print $0 ",'${DESTINATION_PROJECT}'"}'  > $DESTINATION_CSV_FILE && \
    echo "Number of changes exported from ${SOURCE_PROJECT_GIT_URL} $(wc -l $DESTINATION_CSV_FILE)"
}

export_changes_number_from_eclipse_foundation jgit/jgit jgit
export_changes_number_from_eclipse_foundation egit/egit egit
cat $TMP_FOLDER/jgit.csv $TMP_FOLDER/egit.csv > $TMP_FOLDER/change_project.csv
echo "Total of number of changes exported $(wc -l $TMP_FOLDER/change_project.csv)"
