#!/usr/bin/env bash 

if [ "$(uname)" == "Darwin" ]; then
    echo "Don't know how if this will work on mac, sorry (not really) :)"
    echo "Trying to install these packages: build-essential wget cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale"
    brew install build-essential;
    brew install -y wget cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev;
elif [ "$(expr substr $(uname -s) 1 5)" == "Linux" ]; then
    sudo apt-get install -y  build-essential;
    sudo apt-get install -y wget cmake git libgtk2.0-dev pkg-config libavcodec-dev libavformat-dev libswscale-dev;
fi    

if [ ! -e $(ls -d */ | grep 3.4.3) ];
then
    #opencvContribZipName = "opencv_contrib-3.4.3.zip";
    opencvZipName = "opencv-3.4.3.zip";
    echo "Downloading opencv 3.4.3 sources";
    wget https://github.com/opencv/opencv/archive/3.4.3.zip && mv 3.4.3.zip ${opencvZipName};
    #wget https://github.com/opencv/opencv_contrib/archive/3.4.3.zip && mv 3.4.3.zip ${opencvContribZipName};
    unzip -a ${opencvZipName};
    #unzip -a ${opencvContribZipName};
else
    echo "Opencv3.4.3 sources already here";
fi
cd opencv-3.4.3;
mkdir build;
cd build;
cmake -D CMAKE_BUILD_TYPE=Release -D CMAKE_INSTALL_PREFIX=/usr/local ..;
sudo make -j4 install;
echo "OpenCV install completed !";