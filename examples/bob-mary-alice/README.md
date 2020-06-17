# Bob Mary Alice - Message exchanging Example
## Overview
This example intends to demonstrate the animation that jacamo-web produces in order to show message exchanging between agents. Green arrows represent `tell` performative which adds a belief in another agent. Red arrows represent `achieve` performative, i.e., asking to another agent to achieve some goal. The content of the message is shown as a label. Some messages are one-to-one messages, and others are broadcastings.

## Getting Started
### Prerequisites
1. [Java JDK 8+](https://www.oracle.com/technetwork/pt/java/javase/).
2. [Gradle](https://gradle.org/install/).

### Preparing the system
1. Grab jacamo-web project
```
ProjectsFolder$ git clone https://github.com/jacamo-lang/jacamo-web
```
2. Compiling jacamo-web to be used by financial agents example
```
ProjectsFolder/jacamo-web$ ./gradlew build
```

## Demo
### Running the demo
1. Run `bob-mary-alice` example
```
ProjectsFolder/jacamo-web/examples/bob-mary-alice$ ./gradlew run
```
2. Go to bob and execute the command `!start`
3. Go to `agents` window to see the messages presented on the agents' graph.
