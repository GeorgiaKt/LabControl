# LabControl
Lab Control is an Android-based Client–Server system for remote control of lab PCs, built with Java and Sockets. 

## Description

### Features
- Command execution 
  - Echo
  - Restart
  - Shut down
  - Restore
  - Wake on LAN
- Multiple device selection and deselection
- Device status monitoring
- Network validation and monitoring
- Real-time display of device responses

### Command execution
Commands are exchanged between the client (android application) and the server using TCP Sockets. All commands are used to update the name of a device.
Purpose of each command:
- Echo: verifies whether a device is online and retrieves its operating system (e.g., Linux, Windows 10).
- Wake on LAN: sends a "magic packet" to power on devices that are either in sleep mode or fully shut down. For WOL to function, the target device must be:
  - Connected to the network via Ethernet
  - Plugged into a power source
  - Properly configured in BIOS/UEFI and OS settings to support Wake-on-LAN


*Commands such as restart, shut down, and restore are simulations only.*

## Requirements
- Android 8.0+

## Built With
- Android Studio Meerkat 2024.3.1
- Gradle 8.11.1
- AGPL 8.9.1
- Min SDK 26 (Android 8.0)
- Target SDK 35 (Android 15)
- JDK 20.0.1
