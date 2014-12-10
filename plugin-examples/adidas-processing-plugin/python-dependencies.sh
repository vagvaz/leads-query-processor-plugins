#!/bin/bash

############
### INIT ###
############
# get python packages installation tool
wget https://raw.github.com/pypa/pip/master/contrib/get-pip.py
sudo python2.7 get-pip.py
sudo rm get-pip.py*
#######################################
### INSTALL SCRIPTS FOR THE PROJECT ###
#######################################
# scikit-learn package install
echo "#### scikit-learn ####"
sudo apt-get -y install build-essential python-dev python-setuptools \
                     python-numpy python-scipy \
                     libatlas-dev libatlas3gf-base
sudo update-alternatives --set libblas.so.3 \
    /usr/lib/atlas-base/atlas/libblas.so.3
sudo update-alternatives --set liblapack.so.3 \
    /usr/lib/atlas-base/atlas/liblapack.so.3
yes w | pip2.7 install --install-option="--prefix=" -U scikit-learn
# requests package install
echo "#### requests ####"
yes w | pip2.7 install --install-option="--prefix=" -U requests
# lxml package install
echo "#### lxml ####"
sudo apt-get -y install libxslt1-dev libxslt1.1 libxml2-dev libxml2 libssl-dev
yes w | pip2.7 install --install-option="--prefix=" -U lxml
# BeautifulSoup package install
yes w | pip2.7 install --install-option="--prefix=" -U BeautifulSoup
