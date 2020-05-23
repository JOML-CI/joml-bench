#!/bin/bash

if [ -d "panama-vector" ]; then
  # Pull changes
  git pull
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
# Build and run the benchmarks
./mvnw package && java --add-modules jdk.incubator.vector -jar target/bench.jar
