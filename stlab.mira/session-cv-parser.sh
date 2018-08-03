#!/bin/bash

folder_name=$(basename $1)
mkdir -p $2/$folder_name

for session in $1/*; do
	session_name=$(basename $session)
	echo "Qualification level:" $session_name

	for disciplinary_area in $session/*; do
		disciplinary_area_name=$(basename $disciplinary_area)
		mkdir -p $2/$folder_name/$session_name/$disciplinary_area_name
		
		echo -e "\tDisciplinary area:" $disciplinary_area_name
		
		for cv in $disciplinary_area/*; do
			cv_name=$(basename $cv).ttl
			echo -e "\t\tCV:" $cv_name
			java -jar target/stlab.mira-0.0.1-SNAPSHOT.jar -o $2/$folder_name/$session_name/$disciplinary_area_name/$cv_name $cv
		done
	done
	
done