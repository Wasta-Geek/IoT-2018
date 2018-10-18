cmake_minimum_required(VERSION 3.6)
project(IoT_2018)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
find_package( OpenCV REQUIRED )

add_subdirectory(sources)
set(SOURCE_FILES main.cpp)
add_executable(IoT_2018 ${SOURCE_FILES})
target_link_libraries( IoT_2018 ${OpenCV_LIBS} )
