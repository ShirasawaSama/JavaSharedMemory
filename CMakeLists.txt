cmake_minimum_required(VERSION 3.5)

project(java_shared_memory)

set(CMAKE_CXX_STANDARD 17)

add_library(${PROJECT_NAME} STATIC include/jshm.h)

set_target_properties(${PROJECT_NAME}
  PROPERTIES
	ARCHIVE_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
	LIBRARY_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/lib"
	RUNTIME_OUTPUT_DIRECTORY "${CMAKE_BINARY_DIR}/bin"
)

target_include_directories(${PROJECT_NAME} PUBLIC include/)

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
