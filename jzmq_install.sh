#!/bin/sh

# Download ZeroMQ core.
# Make sure that libtool, pkg-config, build-essential, autoconf, and automake are installed.
# Check whether uuid-dev package, uuid/e2fsprogs RPM or equivalent on your system is installed.
# tar -xvf zeromq-4.0.5.tar.gz
# cd zeromq-4.0.5
# ./configure
# make
# sudo make install
# sudo ldconfig

# git clone git://github.com/zeromq/jzmq.git
# cd jzmq/
# sudo apt-get install libtool
# sudo apt-get install autoconf
# ./autogen.sh
# ./configure
# make
# sudo make install
# sudo ldconfig
# mvn package
# mvn install:install-file -Dfile=target/jzmq-3.1.1-SNAPSHOT.jar -DgroupId=org.zemomq -Dversion=3.1.1-SNAPSHOT -Dpackaging=jar -DartifactId=jzmq
# sudo pip install pyzmq

# (java run e.g.)
# run with java -Djava.library.path=/usr/local/lib -cp target/ZeroMQTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar lpclient
# (in Eclipse extend Native library location in Java Build Path)
