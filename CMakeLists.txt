cmake_minimum_required(VERSION 3.5)

project(java_shared_memory)

set(CMAKE_CXX_STANDARD 17)

include_directories("${PROJECT_SOURCE_DIR}/include")

# Shared Memory Test
add_executable(SmTest "${PROJECT_SOURCE_DIR}/test/main.cpp")