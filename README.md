# Olugbadebo Adesina
# Assignment 2
# CSDS 293: Software Craftmanship
# Distributed Task Scheduling System
This project implements a Distributed Task Scheduling System as described in the Programming Assignment 2 for CSDS 293 Software Craftsmanship.

## Project Structure
The project is organized in the following package:

## Classes
The main classes in this system are:
- Task (interface)
- SimpleTask
- Server
- TaskScheduler
- Duration

Additional classes include custom exceptions:
- TaskException
- ServerException
- SchedulerException

## Compilation

To compile the project, ensure you have Java Development Kit (JDK) installed on your system. Then, follow these steps:

1. Navigate to the root directory of the project in your terminal.
2. Run the following command to compile all Java files:


## Running the Program

This project doesn't have a main method as it's designed to be used as a library. However, you can run the JUnit tests to verify the functionality of the classes.

To run the tests:

1. Ensure you have JUnit 5 in your classpath.
2. Compile the test files:

3. Run the tests:


Replace `junit-platform-console-standalone-1.8.2.jar` with the actual path to your JUnit jar file.

## Usage

To use this library in your own project:

1. Import the necessary classes from the `edu.cwru.ooa21.taskscheduler` package.
2. Create `Task` objects using the `SimpleTask` class.
3. Create `Server` objects and add them to a `TaskScheduler`.
4. Use the `TaskScheduler` to schedule and execute tasks.

Example:

```java
TaskScheduler scheduler = new TaskScheduler();
Server server1 = new Server();
Server server2 = new Server();

scheduler.addServer(server1);
scheduler.addServer(server2);

Task task1 = new SimpleTask("task1", Duration.ofMillis(1000));
Task task2 = new SimpleTask("task2", Duration.ofMillis(2000));

scheduler.scheduleTask(task1);
scheduler.scheduleTask(task2);

Map<Server, List<Task>> results = scheduler.executeAll();


This README provides instructions on how to compile and run the code, explains the project structure, and gives a brief example of how to use the library. You may need to adjust some details based on your specific implementation and development environment.




From 3:
I see you've implemented PriorityTasks with the Enums and model, but I am not seeing where the priority is being accounted for in the execution. Also, the way you have it written, base tasks can't be accepted into queues again, just PriorityTasks.
* Sort queues based on priorities before execution
* Support base tasks. Parsing it to PriorityTasks and with a LOW priority

From 4:
* Task scheduler needs to check dependencies before scheduling
* Logging improvements ***
* Timeout mechanism is not implemented (cancellation of task once timeout is extended)

From 5:
* Serialize Task
* Implement RemoteServer class
* Add remote and local sever management to TaskScheduler
* Implement error handling for timeouts and remote server issues
	* circuit breaker pattern
	*  System-Wide Error Reporting
	* Network-Related Error Handling
* Real time performance monitor
	**
	* Alert system for sending notification (log files, email, cli output)
* Relevant Testing


Look bro, I squeezed time to accommodate this. I have a job and right now I'm trying to upscale and switch jobs. So I've been having interviews. I moved things around and got your work 70% done (based on the breakdown I sent you) without any guarantee of payment.
I don't know you, and I also don't directly know your sister. My hands are tied.

