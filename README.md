# Blind Assistant Android Application : Eyesy Hope Version 1
#An android application for blind people which implements object detection and face recognition as core features

![img.png](img.png).

# Technologies
- Natural Language Processing (NLP)
- Computer Vission

# Object detection
## YOLO (You Only Look Once) Algorithm was used.

## Why YOLO?

- YOLO (You Only Look Once) is a real-time object detection system that has gained popularity in computer vision applications. The main advantage of YOLO over other object detection systems is its speed. YOLO is able to detect objects in an image in real-time, meaning that it can process frames from a video stream at a rate of several frames per second.
- Another advantage of YOLO is its accuracy. YOLO is able to detect objects with a high degree of accuracy, even in cases where the objects are partially occluded or have low contrast with the background.
- YOLO also has a relatively simple architecture compared to other object detection systems, making it easier to implement and train. This simplicity allows YOLO to be used on resource-constrained devices like smartphones and embedded systems.
- Overall, YOLO's combination of speed, accuracy, and simplicity has made it a popular choice for real-time object detection in a variety of applications, including autonomous vehicles, surveillance systems, and robotics.

# Face Recognition
## OpenCV library was used in conjuction with CNN (Convolutionary Neural Networks) 
- CNNs are a type of deep learning algorithm that have shown impressive results in face recognition. CNNs can be trained on large datasets of faces to learn features automatically and can achieve high accuracy in face recognition tasks.

# Natural Language Processing 
## Natural Language Processing was used for allowing the blind person to navigate through the system using voice commands
- Google Text To Speech was used to convert voice input to text

# Aims Of the Project
- This research aims to design and implement a blind Assistant application using YOLO machine learning algorithm.

# Objectives

- To identify an appropriate dataset for the model
- To train the virtual blind assistant machine learning model and test the model
- To implement YOLO Machine learning algorithm to detect objects within 1m - 10m of range using TensorFlow
- To implement Convolutional Neural Networks (CNN) for face recognition using OpenCV.
- To implement Geo-Decoder an android class to locate the blind person at any time using GPS coordinates

# Tools used

- Database: SQLite
- Environment Variables: Android SDK , JDK
- Programming Languages: Kotlin, Java and Python
- Algorithms: YOLO and CNN
- Android Packaging Tool: Gradle
- Architecture: SSD Mobile Net
- Libraries: TensorFlow, OpenCV
- IDE: Android studio, Jupiter Notebook

# System Images 

## Object Detection

### My bicycle being detected with a 72% degree of accuracy
![object1.jpg](object1.jpg).

### My dog being detected with a 71% degree of accuracy
![object2.jpg](object2.jpg).

### Multiple objects , a mouse and a bottle being detected at the same time
![object3.jpg](object3.jpg).

### My laptop being detected with a 77% degree of accuracy
![object4.jpg](object4.jpg).

## Face Recognition

### My Friend Farai recognized with a 92% degree of accuracy
![face1.jpg](face1.jpg).

### My face being recognized with a 74% degree of accuracy , which was very okay for a cloudy day
![face2.jpg](face2.jpg).

### Trying to recognize a face that has not been trained / entered in the system , a blue bounding box sorrounding your face show that you are not recognized and also an audio output notifying the user that this person is not recognized is played
![face4.jpg](face4.jpg).



# How to run the project 

- Install Git: First, make sure you have Git installed on your system. You can download Git from the official Git website (https://git-scm.com). Follow the installation instructions based on your operating system.
- Clone the repository: Open a terminal or command prompt and navigate to the directory where you want to clone the repository. Then use the following command to clone the repository: Copy git clone <repository_url> Replace repository_url> with the URL of the GitHub repository you want to clone. For example:
- Open the project in Android Studio: Once the repository is cloned, open Android Studio and select "Open an existing Android Studio project." Navigate to the directory where the repository was cloned and select the project's root folder.
- Install dependencies: If the project has any external dependencies, you may need to install them. Check the project's documentation or README file for instructions on how to install the required dependencies.
- Set up device or emulator: Connect a physical Android device to your computer using a USB cable, or start an Android emulator from Android Studio. Make sure the device or emulator is recognized by your system.
- Build and run the project: In Android Studio, select the target device from the device dropdown menu (next to the green play button). Then click the play button or use the shortcut Shift + F10 to build and run the project on the selected device.



  


