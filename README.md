# android-ble-switch
A switch app using Bluetooth LE device 

# Detail Info.

## Precondition

This app should be used with a specific Bluetooth LE device(*1)

*1: PochiruEco
http://www.products.braveridge.com/pochiru_eco/

With other device, it may work, but this app was tested targeting the device above especially.

## What the app can do

### parameters

With app UI, you can set the following

- Device Name:(e.g. PochiruEco0099999)
- Device Address:(e.g. 12:34:56:78:9A:BC)
- Service UUID: b3b36901-50d3-4044-808d-50835b13a6cd(*1)

*1:the UUID is used to scan and connect the "PochiruEco" BLE Device

### function

the app using a BLE device as a switch:

- when the switch is on
- the app invoke another android-app
- the android-app doing something(like shooting with a camera-lens)

the app performs with another app(camera app) below:

#### Step1: Setting the input parameters above

With app UI, input parameters to scan your BLE device as a switch.

#### Step2: When touch "Start service" button, android-OS register the service

the service is doing below:

- scan Bluetooth LE devicees with the parameters
- when a device found, stop scanning, invoke other app, connect to 
  stop adverting from the device, and start scanning again 

Currently, invoke a camera app(*3) by starting as an Intent

##### Intent info. about the camera app

- Intent(package): com.krasavkana.android.decoycamera
- Intent(activity): com.krasavkana.android.decoycamera.CameraActivity
- ble-command: Shoot!Shoot!

#### Step3: the camera app will a remote-shoot with android-camera-lens within 5 seconds

#### Step4: after remote-shooting, the camera app will finish itself

the service wait to find a switch device

#### Step5: When touch "Stop service" button, your android unregister the service

## about the camera app

Entire code of the camera app will be up here(github.com) 

and introduce how to use and so on on [MY BLOG PAGE](https://krasavkana.com)

Enjoy!
