# Transbord

Transbord is a powerful voice transcription Android application that allows users to convert speech to text anywhere on their device through an innovative overlay button.

![Transbord Logo](app/src/main/res/drawable/splash_logo.png)

## Features

### Core Functionality
- **Voice Transcription**: Record and transcribe speech to text using Groq API's advanced speech recognition technology
- **Floating Overlay Button**: Access transcription functionality from any app with a draggable floating button
- **Text Insertion**: Directly insert transcribed text into any text field in other applications
- **Local Storage**: Automatically save all transcriptions for future reference and editing

### Advanced Features
- **AI-Powered Text Processing**: Enhance, correct, and format transcriptions using Groq's reasoning models
- **Voice Commands**: Control the app hands-free with customizable voice commands and hotword detection
- **Smart Templates**: Apply context-aware formatting to your transcriptions based on content patterns
- **Accessibility Integration**: Seamless text insertion across apps through accessibility services

### User Experience
- **Modern UI Design**: Sleek, intuitive interface with Material Design components
- **Comprehensive Onboarding**: Guided introduction for first-time users
- **Customization Options**: Personalize the app's behavior to suit your workflow
- **Processing Animations**: Visual feedback during transcription to indicate the app is working

## Screenshots
(Screenshots will be added here)

## Requirements

- Android 8.0 (API level 26) or higher
- Internet connection for transcription services
- Microphone permission for voice recording
- Overlay permission for floating button functionality
- Accessibility service permission for text insertion

## Installation

1. Download the latest APK from the [Releases](https://github.com/abdul977/transbord/releases) section
2. Enable installation from unknown sources in your device settings
3. Open the APK file and follow the installation instructions
4. Launch Transbord and complete the onboarding process

## Usage

### Basic Transcription
1. Open the Transbord app
2. Tap the microphone button to start recording
3. Speak clearly into your device
4. Tap the button again to stop recording and process the transcription
5. View, edit, or share your transcribed text

### Overlay Mode
1. Long-press the microphone button in the main app to activate overlay mode
2. Grant necessary permissions when prompted
3. Use the floating button to record and transcribe from any app
4. Transcribed text will be automatically inserted into the active text field

### Voice Commands
1. Enable voice commands in the settings
2. Set up your preferred hotword (default: "Hey Transbord")
3. Use commands like "start recording", "stop recording", "save", etc.
4. The app will respond to your voice commands even when running in the background

## Permissions Explained

- **Internet**: Required to send audio to the Groq API for transcription
- **Microphone**: Needed to record your voice for transcription
- **Storage**: Used to save audio recordings and transcriptions locally
- **Overlay**: Allows the floating button to appear over other apps
- **Accessibility**: Enables text insertion into other applications
- **Foreground Service**: Keeps the overlay and voice command services running

## Development

### Tech Stack
- Java for Android application development
- Room Database for local storage
- Retrofit for API communication
- Material Design components for UI
- Android Accessibility Services for text insertion

### API Integration
Transbord uses the Groq API for:
- Speech-to-text transcription with the Whisper model
- Text processing with the DeepSeek R1 Distill Llama model

## Contributing

Contributions are welcome! If you'd like to contribute to Transbord:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Developed by Muahib Solutions
- Powered by Groq API for transcription and reasoning capabilities
- Special thanks to all contributors and testers

## Contact

For questions, feedback, or support, please contact:
- Email: abdulmuminibrahim74@gmail.com
- GitHub: [abdul977](https://github.com/abdul977)
