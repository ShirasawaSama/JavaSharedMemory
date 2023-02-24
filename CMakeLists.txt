cmake_minimum_required(VERSION 3.5)

project(java_shared_memory)

set(CMAKE_CXX_STANDARD 17)

add_library(${PROJECT_NAME} INTERFACE include/jshm.h)

target_include_directories(${PROJECT_NAME} INTERFACE include)

set_target_properties(${PROJECT_NAME} PROPERTIES LINKER_LANGUAGE CXX)

install(
  TARGETS ${PROJECT_NAME}
  RUNTIME DESTINATION bin
  LIBRARY DESTINATION lib
  ARCHIVE DESTINATION lib
)

install(
  DIRECTORY "include/"
  DESTINATION "include"
)

add_executable(jshm_test "${PROJECT_SOURCE_DIR}/test/main.cpp")
