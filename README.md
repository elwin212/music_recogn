# My Music Identification App

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Description

This is an Android app similar to Shazam, designed to recognize and identify songs from audio samples. The app uses java with android sdk. Users can record a short audio clip and the app will attempt to identify the song by matching it with its database of songs. In this repo, I took the database away to avoid the copyright, you can create your own database (int this project I use sqlite).

## Features

- Song recognition from audio samples.
- UI design
- Searching history

## Screenshots

![Screenshot 1](screenshots/result.png)
![Screenshot 2](screenshots/history_screenshot.png)

## Installation

- Install Android Studio
- Clone the repo
- Build the project
- Run on emulator

## How to Use

- Make sure the app has the permission to record the audio
- Click the button to start listening (4 seconds at least, by default I set up to 12 seconds)
- It will search the database to find the matching song

## Technologies Used

- Android SDK
- Java
- SQLite

## How to Contribute

Contributions are welcome! If you want to contribute to this project, follow these steps:

1. Fork the repository.
2. Create a new branch: `git checkout -b new-branch`
3. Make your changes and commit them: `git commit -m "Add some feature"`
4. Push to the branch: `git push origin my-feature-branch`
5. Submit a pull request.

Please make sure your code follows the project's coding conventions and includes appropriate tests.

## Bugs and Issues

If you encounter any bugs or issues with the app, please let me know by opening an issue on the [GitHub repository](https://github.com/elwin212/music_recogn/issues).

## License

This project is licensed under the MIT License.

For the application architecture you can refer to this link https://drive.google.com/file/d/1ljrocl6fSWki4dqNILkc_KMA_WHtZ0ON/view.
