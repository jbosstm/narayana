This directory includes a few unit tests which can also help illustrate some of the STM concepts

BasicUnitTest shows how to create an STM which is managed via pessimistic concurrency control.

SampleUnitTest shows how to create an STM with is managed via optimistic concurrency control. All of the methods of the object are assumed to modify the state since we don't use annotations to specify otherwise.