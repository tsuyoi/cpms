# [WIP] Cresco Pipeline Management System

*Note: This project is in the very early stages of development. This README will continue to be updated to reflect the current and future state, 
but the project is not currently feature-complete.*

Cresco Pipeline Management System (CPMS), a workflow management system, builds on the 
[Cresco Edge Computing Framework](http://cresco.io) to provide a simple scriptable workflow distribution framework to 
leverage edge computing to execute a pipeline consisting of jobs and tasks. 

#### Table of Contents:
 - [Requirements](#requirements)
 - [Installation](#installation)
 - [Starting Up](#starting-up)
 - [Building Pipelines](#building-pipelines)
 - [Running Pipelines](#running-pipelines)
 - [How We Leverage Cresco and Edge Computing](#how-we-leverage-cresco-and-edge-computing)

## Requirements
- To Run
  - [Cresco Agent v1.1](https://github.com/CrescoEdge/agent/releases/tag/1.1-SNAPSHOT)
- To Build
  - Java 1.8
  - Maven 3.6.3+

## Installation
Build or download the `cpms-processor.jar`, `cpms-collector.jar`, `cpms-controller.jar`, `cpms-api.jar`, and 
(optionally) `cpms-watcher.jar` files and load them on one or more Cresco agents running on infrastructure suitable to 
your pipeline requirements.

## Starting Up

### Prepare the processing node(s)
*Preparing the cresco and plugin jars...*

### Start the agents
*Running the cresco jar and checking for errors...*

### Review and inspect the processing fabric
*Using either the API or web interface to view node status...*

## Building Pipelines
With the system installed and running, you're ready to start creating pipelines to run. It is recommended to create
your pipelines using the web interface, but they can be generated manually as well.

### Using the web interface
*Show web interface pipeline creation...*

### Manual JSON File(s)
This is mainly used as a way to easily transfer pipelines from one installation to another, but you can manually
generate pipelines in the JSON format as well.

## Running Pipelines
CPMS uses pipelines to denote a complete processing workflow. These can be managed manually or automatically.

### Manual pipelines 
Manual pipelines can be created and instantiated using either the CPMS API or web interface.

### Automated pipelines
The CPMS Watcher plugin can be leveraged to trigger defined pipelines based on watched directory(ies) to support
automated processing of work as it is generated without needing to build a separate interface.


## How We Leverage Cresco and Edge Computing
CPMS leverages processing plugin(s) running on the Cresco framework to delegate jobs and tasks required to complete a 
pipeline. The processing plugin(s) transmit information and task output logs on a Cresco data plane.