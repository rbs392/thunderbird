#!/usr/bin/env bash

LINUX="linux"
MAC="mac"
  
detect_os() {
  uname=`uname`

  case ${uname} in
    "Linux") OS=${LINUX} ;;
    "Darwin") OS=${MAC} ;;
    *) echo "unable to determine os of type ${uname}" ;;
  esac
  
}


WORK_DIR="${HOME}/.thunderbird"
BIN_DIR=`pwd`/bin

setup_virtualEnv() {
  if [ ! -d ${WORK_DIR} ]; then virtualenv ${WORK_DIR}; fi
  source ${WORK_DIR}/bin/activate
}

install_essentials() {
  sudo apt-get install -y python2.7 build-essential python-pip-whl virtualenv
  pip install -r requirements.txt
}

detect_os

if [[ ${OS} == ${LINUX} ]] then
  if [ ! -f "${BIN_DIR}/done" ]; then 
    install_essentials
    echo "installed on `date`" > "${BIN_DIR}/done"
  fi
  setup_virtualEnv
fi