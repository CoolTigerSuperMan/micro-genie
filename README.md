Micro Genie
=======

[![Build Status](https://travis-ci.org/shagwood/micro-genie.svg?branch=master)](https://travis-ci.org/shagwood/micro-genie)
[![Coverage Status](https://coveralls.io/repos/shagwood/micro-genie/badge.svg)](https://coveralls.io/r/shagwood/micro-genie)

Common application libraries used in micro-service architectures, including libraries for publishing and subscribing to and from event topics, saving and reading to and from file stores, producing and consuming to and from queues, and basic Entity database persistence.  


**Note on Micro Genie Commands: This will probably change to just include and rely on spotify trickle and remove the thread pool groups taken from hysterix**

Micro Genie also includes the ability to execute application commands asynchronously with dependency chaining (blocking until command results return) and passing outputs from one command as input to a chained / dependent command. 

The goal is to provide a library that enables fast development of micro-services by offering out of the box eventing, queuing, filestore persistence, and database persistence with optional asynchronous execution and command chaining. 


# Under Development

Micro Genie is currently under development in a non released stage

The APIs are not completed and will change. Sufficient test coverage does not exist and some methods are currently only stubbed out. 

