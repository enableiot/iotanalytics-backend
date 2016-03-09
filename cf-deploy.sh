#!/bin/bash -x
# Copyright (c) 2015 Intel Corporation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

TARGET=(`cf t | grep "Space"`)
SPACE=${TARGET[1]}

function ask {
    if [ "x${SPACE}" = "x${1}" ]
    then
        read -p "Are you sure you'd like to deploy to ${1^^} space? " -r
        if [[ ! $REPLY =~ ^[Yy] ]]
        then
            exit 0
        fi
    fi
}

ask demo
ask prod

gradle clean build &&
cf push ${SPACE}-backend

