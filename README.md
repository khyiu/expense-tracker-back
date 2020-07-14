# grocery-tracker-back
This is a personal project that I'll use to get familiar with tools and techniques I've wanted to explore for a while.  

Functionally-speaking, I've chosen to implement an application that (normally) doesn't hold much complexity, so that I 
could focus on exploring technical matters.  

Basically, **"Grocery tracker"** is an application in which I'll register the things I bought during grocery shopping 
and their price. Over time, the application would, then, be able to refine an average price for everything I would buy 
on a regular basis. Based on this average price, I could then know if I'm spending more or less than usual for a same 
article. 

#Journey log
##Stage 1: CI/CD server
### Circle CI
Initially I gave Circle CI a shot. As many other CI solutions, it was pretty straightforward to define a first working 
build pipeline: based on a yaml configuration and some commands I've found in some examples, I managed to have my project
build running. 

Right after, I wanted my build pipeline to push the build artifact to AWS CodeArtifact where I created a Maven repository.   
I browsed the Orbs registry but couldn't find any dedicated to AWS CodeArtifactory. I could have tried using the orb
that installs AWS CLI and try to run the proper command to do so but I figured, why not switch the CI/CD process 
altogether to AWS.

### AWS
#### CodeBuild

Project build process. Fairly easy and intuitive initial setup: simply created a project in the AWS CodeBuild console 
and specify to use the _buildspec.yml_ configuration file from the project source.
Didn't even bother to configure input/output folders in the dedicated S3 bucket -> use default values from CodeBuild.

#### CodePipeline


