cmake_minimum_required(VERSION 3.5)

project(java_shared_memory)

set(CMAKE_CXX_STANDARD 17)

add_library(${PROJECT_NAME} STATIC include/jshm.h)

set_target_properties(java_shared_memory PROPERTIES LINKER_LANGUAGE CXX)

install(
    DIRECTORY "include/"
    DESTINATION "include"
)
