#!/bin/bash

TMP_FOLDER=/tmp
JGIT_FOLDER=$TMP_FOLDER/jgit
EGIT_FOLDER=$TMP_FOLDER/egit

# clone jgit & export changes numbers
git clone "https://git.eclipse.org/r/jgit/jgit" $JGIT_FOLDER && \
cd $JGIT_FOLDER && \
git ls-remote | grep changes | awk  '{print $2}' | awk -F '/' '{print $4}' | uniq | sort -n | awk 'NF{print $0 ",GerritForge/jgit"}' > $TMP_FOLDER/changes_jgit.csv && \
echo "Number of changes exported from jgit $(wc -l $TMP_FOLDER/changes_jgit.csv)"

# clone egit & export changes numbers
git clone "https://git.eclipse.org/r/egit/egit" $EGIT_FOLDER && \
cd $EGIT_FOLDER && \
git ls-remote | grep changes | awk  '{print $2}' | awk -F '/' '{print $4}' | uniq | sort -n | awk 'NF{print $0 ",GerritForge/egit"}' > $TMP_FOLDER/changes_egit.csv
echo "Number of changes exported from egit $(wc -l $TMP_FOLDER/changes_egit.csv)"

cat $TMP_FOLDER/changes_jgit.csv $TMP_FOLDER/changes_egit.csv > $TMP_FOLDER/change_project.csv

echo "Total of number of changes exported $(wc -l $TMP_FOLDER/change_project.csv)"
