# LabControl
Lab Control is an Android-based Client–Server system for remote control of lab PCs, built with Java and Sockets. 

## Description

### Features
- [Command execution](#command-execution) 
  - Echo
  - Restart
  - Shut down
  - Restore
  - Wake on LAN
- [Multiple device selection and deselection](#multiple-device-selection-and-deselection)
- [Device status monitoring](#device-status-monitoring)
- [Network validation and monitoring](#network-validation-and-monitoring)
- [Real-time display of device responses](#real-time-display-of-device-responses)

### Command execution
Commands are exchanged between the client (android application) and the server using TCP Sockets. All commands are used to update the name of a device.
Purpose of each command:
- Echo: verifies whether a device is online and retrieves its operating system (e.g., Linux, Windows 10).
- Wake on LAN: sends a "magic packet" to power on devices that are either in sleep mode or fully shut down. For WOL to function, the target device must be:
  - Connected to the network via Ethernet
  - Plugged into a power source
  - Properly configured in BIOS/UEFI and OS settings to support Wake-on-LAN

*Commands such as restart, shut down, and restore are simulations only.*

### Multiple device selection and deselection
Device selection is managed through a Contextual Action Bar (CAB), which appears upon long-pressing a device. The CAB allows:
- Single or multiple selection of devices through taps
- Select / deselect All functionality
- Access to a command menu, enabling the execution of actions on the selected devices

### Device status monitoring
Device availability is checked using a bound service that periodically sends the Echo command to all configured devices. While the app is active, this service monitors whether each device is online and which operating system it is running.

### Network validation and monitoring
The application is designed to function exclusively within the lab's local network. To enforce this:
- The app continuously monitors the current network connection.
- If the device is not connected to the lab’s Wi-Fi, the user is notified via an alert dialog and prompted to connect to the correct network.
- To perform network checks, the app requests the necessary permissions and requires location services to be enabled, as Android restricts access to Wi-Fi SSID information without location access.
- If permissions are denied or location services are disabled, the app prompts the user via dialogs and directs them to the appropriate settings.

### Real-time display of device responses
Device responses are shown in a bottom sheet that updates live, supports vertical scrolling and adapts its size to fit the number of messages.

### Device loading from JSOM file
Devices are loaded from a `lab_devices_config.json` file located in `src/main/assets/`. Format of the file is:
```
{
  "devices": [
    {
      "name": "PC01",
      "networkName": "PRPC01",
      "ip": "device_ip",
      "mac": "device_mac"
    }
    // Additional devices can be added here, separated by commas
  ]
}
```

## Requirements
- Android 8.0 or higher
- Wake-on-LAN (WOL) enabled and configured on your PC
- Connected to the lab’s LAN network
- Location permission granted (as required by Android)
- Location services enabled

## Configuration
To adapt the application for your own network and devices, you need to:
- Update the `lab_devices_config.json` file located in `src/main/assets/`.
- Change the lab’s SSID to your own in `Constants.java`.
- Change the lab’s broadcast IP to your network’s broadcast address in `Constants.java`.

## Built With
- Android Studio Meerkat 2024.3.1
- Gradle 8.11.1
- AGPL 8.9.1
- Min SDK 26 (Android 8.0)
- Target SDK 35 (Android 15)
- JDK 20.0.1

## Screenshots
| ![](https://github.com/GeorgiaKt/LabControl/blob/main/screenshots/main_screen.jpg) | ![](https://github.com/GeorgiaKt/LabControl/blob/main/screenshots/multiple_devices_selected.jpg) | ![](https://github.com/GeorgiaKt/LabControl/blob/main/screenshots/multiple_devices_selected_and_command_menu.jpg) |
|---|----|---|
| ![](https://github.com/GeorgiaKt/LabControl/blob/main/screenshots/devices_responses_bottom_sheet_1.jpg) | ![](https://github.com/GeorgiaKt/LabControl/blob/main/screenshots/devices_responses_bottom_sheet_2.jpg) | ![](https://github.com/GeorgiaKt/LabControl/blob/main/screenshots/devices_responses_bottom_sheet_3_no_reponses.jpg) |

More screenshots can be found in [screenshots](https://github.com/GeorgiaKt/LabControl/tree/main/screenshots) folder.
