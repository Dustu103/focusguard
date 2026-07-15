# Android build environment
FROM eclipse-temurin:17-jdk

# Install required tools
RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    git \
    && rm -rf /var/lib/apt/lists/*

# Android SDK setup
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools

RUN mkdir -p $ANDROID_SDK_ROOT/cmdline-tools

# Download Android command line tools
RUN wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdtools.zip \
    && unzip -q /tmp/cmdtools.zip -d /tmp/cmdtools \
    && mv /tmp/cmdtools/cmdline-tools $ANDROID_SDK_ROOT/cmdline-tools/latest \
    && rm /tmp/cmdtools.zip

# Accept licenses and install SDK components
RUN yes | sdkmanager --licenses > /dev/null 2>&1 || true
RUN sdkmanager \
    "platform-tools" \
    "platforms;android-34" \
    "build-tools;34.0.0"

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Make gradlew executable
RUN chmod +x gradlew

# Build the debug APK
RUN ./gradlew assembleDebug --no-daemon --stacktrace

# Output location
RUN echo "APK built at: /app/app/build/outputs/apk/debug/app-debug.apk"
