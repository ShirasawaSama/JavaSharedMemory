cmake_minimum_required(VERSION 3.5)

project(java_shared_memory)

set(CMAKE_CXX_STANDARD 17)

add_library(${PROJECT_NAME} include/jshm.h)

target_include_directories(${PROJECT_NAME} PUBLIC include)

set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE CXX)

install(
    DIRECTORY "include/"
    DESTINATION "include"
)

install(
  TARGETS ${PROJECT_NAME}
  LIBRARY DESTINATION lib
  ARCHIVE DESTINATION lib
)
