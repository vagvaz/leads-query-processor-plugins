#!/bin/bash

############
### INIT ###
############
# get python packages installation tool
wget https://raw.github.com/pypa/pip/master/contrib/get-pip.py
sudo python2.7 get-pip.py

#######################################
### INSTALL SCRIPTS FOR THE PROJECT ###
#######################################
# scikit-learn package install
sudo apt-get install build-essential python-dev python-setuptools \
                     python-numpy python-scipy \
                     libatlas-dev libatlas3gf-base
sudo update-alternatives --set libblas.so.3 \
    /usr/lib/atlas-base/atlas/libblas.so.3
sudo update-alternatives --set liblapack.so.3 \
    /usr/lib/atlas-base/atlas/liblapack.so.3
pip2.7 install --user --install-option="--prefix=" -U scikit-learn
# requests package install
pip2.7 install --user --install-option="--prefix=" -U requests
# lxml package install
sudo apt-get install libxslt1-dev libxslt1.1 libxml2-dev libxml2 libssl-dev
pip2.7 install --user --install-option="--prefix=" -U lxml
# TODO test
