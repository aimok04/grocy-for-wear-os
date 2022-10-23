![Grocy for Wear OS](/images/title.png)
---
![GitHub](https://img.shields.io/github/license/aimok04/grocy-for-wear-os?style=for-the-badge) ![GitHub release (latest by date)](https://img.shields.io/github/v/release/aimok04/grocy-for-wear-os?style=for-the-badge)

Grocy for Wear OS is an unofficial client for the grocy groceries & household management server for Google's Wear OS smartwatch operating system.

Currently the app only works as a viewer for shopping lists, but more features may be supported in the future (if I'll have time to do that ¯\\\_(ツ)_/¯)

## Supported features ##
- [x] Shopping list
	- [x] Marking items as done
	- [x] Reordering product groups

## Installation instructions ##
1. Download and install the .apk file for Wear OS in the releases tab. The easiest way to install .apk files on Wear OS devices is by using adb;
	1. Enable the developer settings (`Settings -> System -> About -> Versions -> Tap on the build number until the "you are now a developer" message appears`)
	2. Enable debugging over wifi (`Settings -> Developer options -> Wireless debugging -> Enable`)
	3. Enter pairing mode (`Settings -> Developer options -> Wireless debugging -> Pair new device`)
	4. Pair device on computer (`# adb pair {ip}:{port}`)
		1. Enter the pairing code shown on your watch.
	5. Get the adb ip and port (`Settings -> Developer options -> Wireless debugging -> IP address & port`)
	6. Connect your device via adb (`# adb connect {ip}:{port}`)
	7. Install the Wear OS .apk file on your watch (`# adb install {filename}`)
2. Download and install the companion .apk file on your android smartphone.
3. Open the companion app on your phone.
4. Enter your grocy credentials in the companion app and hit send.
5. Enjoy the app 🙃

## Screenshots ##
![Screenshots](/images/screenshots.png)
