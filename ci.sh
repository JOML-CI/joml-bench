#!/bin/bash

if [ ! -d "jdk-15.0.2" ]; then
  # Download JDK15 Linux x64 build
  wget https://download.java.net/java/GA/jdk15.0.2/0d1cfde4252546c6931946de8db48ee2/7/GPL/openjdk-15.0.2_linux-x64_bin.tar.gz
  tar xf openjdk-15.0.2_linux-x64_bin.tar.gz
  rm openjdk-15.0.2_linux-x64_bin.tar.gz
fi

export JAVA_HOME=$(pwd)/jdk-15.0.2

if [ -d "panama-vector" ]; then
  # Pull changes
  (
    cd panama-vector
    git pull
  )
else
  # Shallow-clone vectorIntrinsics repo
  git clone --depth=1 --single-branch --branch=vectorIntrinsics https://github.com/openjdk/panama-vector.git
  # Configure rebase and autostash
  (
    cd panama-vector
    git config --local pull.rebase true
    git config --local rebase.autoStash true
  )
fi
# Build it
(
  cd panama-vector
  bash configure
  make images
)
# Configure JAVA_HOME and PATH
export JAVA_HOME=$(pwd)/panama-vector/build/linux-x86_64-server-release/images/jdk
export PATH=$(pwd)/panama-vector/build/linux-x86_64-server-release/images/jdk/bin:$PATH

# Download and extract binutils
if [ ! -d "binutils-2.32" ]; then
  wget https://ftp.gnu.org/gnu/binutils/binutils-2.32.tar.gz
  tar xf binutils-2.32.tar.gz
  rm binutils-2.32.tar.gz
fi
BINUTILS=$(pwd)/binutils-2.32
(
  cd panama-vector/src/utils/hsdis
  # Build hsdis
  if [ ! -f "build/linux-amd64/hsdis-amd64.so" ]; then
    make BINUTILS=$BINUTILS ARCH=amd64
  fi
  # Copy to JDK
  cp build/linux-amd64/hsdis-amd64.so ${JAVA_HOME}/lib/
)

# Build and run the benchmarks
./mvnw package && java --add-modules jdk.incubator.vector -jar target/bench.jar
