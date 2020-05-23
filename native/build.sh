#!/bin/sh
echo "Building JNI..."
gcc -c -march=native -mtune=native -o build/JNI.o -fPIC -m64 -Ofast -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" src/JNI.c -lm
gcc -c -save-temps -march=native -mtune=native -o build/bench_Matrix4fn.o -fPIC -m64 -Ofast -I"${JAVA_HOME}/include" -I"${JAVA_HOME}/include/linux" src/bench_Matrix4fn.c -lm
gcc -shared -static-libgcc -O3 -o build/libjoml.so -fPIC -m64 build/JNI.o build/bench_Matrix4fn.o -lm
cat bench_Matrix4fn.s
strip -x -s build/libjoml.so
