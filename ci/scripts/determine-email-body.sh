#!/bin/bash
set -e

pushd git-repo > /dev/null
	PREV_SHA=$(git rev-parse HEAD^1)
	echo $PREV_SHA
popd > /dev/null

#PREV_STATUSES=$(curl https://api.github.com/repos/spring-projects/spring-boot/commits/$PREV_SHA/statuses)
#PREV_STATES=echo $PREV_STATUSES | jq -r '.[]  | select(.context == "build") | .state'
#WAS_PREV_SUCCESSFUL=$(echo $PREV_STATES | grep 'success')
WAS_PREV_SUCCESSFUL=success

if [[ $STATE == "success" ]];then
	echo "Build SUCCESSFUL ${BUILD_PIPELINE_NAME} / ${BUILD_JOB_NAME} / ${BUILD_NAME}" > email-details/subject
	if [[ $WAS_PREV_SUCCESSFUL == "" ]];then
    	echo "Build ${ATC_EXTERNAL_URL}/teams/${BUILD_TEAM_NAME}/pipelines/${BUILD_PIPELINE_NAME}/jobs/${BUILD_JOB_NAME}/builds/${BUILD_NAME} is successful!" > email-details/body
	elif [[ $WAS_PREV_SUCCESSFUL == "success" ]];then
		touch email-details/body
	fi
elif [[ $STATE == "failure" ]];then
	echo "Build ${ATC_EXTERNAL_URL}/teams/${BUILD_TEAM_NAME}/pipelines/${BUILD_PIPELINE_NAME}/jobs/${BUILD_JOB_NAME}/builds/${BUILD_NAME} has failed!" > email-details/body
	if [[ $WAS_PREV_SUCCESSFUL == "" ]];then
		echo "Still FAILING ${BUILD_PIPELINE_NAME} / ${BUILD_JOB_NAME} / ${BUILD_NAME}" > email-details/subject
	elif [[ $WAS_PREV_SUCCESSFUL == "success" ]];then
		echo "Build FAILURE ${BUILD_PIPELINE_NAME} / ${BUILD_JOB_NAME} / ${BUILD_NAME}" > email-details/subject
	fi
fi
