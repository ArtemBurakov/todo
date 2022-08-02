# todo
Todo is an Android app designed to make your life easier. It uses Firebase push notification service and SQLite database. You can create new notes, tasks, divide your tasks and notes into workplaces.

## Screenshots
<p float="left">
  <img src="https://user-images.githubusercontent.com/59533626/182351127-0e67a018-730a-4c21-8165-a1f5e022b348.png" height="600" width="300">
  <img src="https://user-images.githubusercontent.com/59533626/182351136-167d8c08-bd8b-4891-a261-436e7a83e77e.png" height="600" width="300">
  <img src="https://user-images.githubusercontent.com/59533626/182351154-fc7faf30-08a5-4262-bed9-e765e144beee.png" height="600" width="300">
  <img src="https://user-images.githubusercontent.com/59533626/182351145-c69eda95-ee2c-47d5-8af5-30d5412bcba8.png" height="600" width="300">
  <img src="https://user-images.githubusercontent.com/59533626/182351173-f3132080-4d70-46b8-a25f-18c259b53bb7.png" height="600" width="300">
  <img src="https://user-images.githubusercontent.com/59533626/182351160-cbf1149d-b429-4b0e-84a9-8d9b6431a629.png" height="600" width="300">
</p>

## Features

- :arrows_counterclockwise: Synchronization of user data with a remote server, so your data will always be backed up.
- :earth_africa: Versatility. Access your data from different devices around the world.
- :busts_in_silhouette: Ability to create user accounts.
- :calling: Background FCM push notifications, so your data is updated automatically in real time without the need to reload.

## Requirements

1. [php-yii2-todo](https://github.com/ArtemBurakov/php-yii2-todo).
2. Сonfigured development environment.
3. Created Firebase account and linked Firebase to the todo app.

## Getting started

### Installation

* Clone the project

```bash
  git clone https://github.com/ArtemBurakov/todo.git
```

* Open the todo app in Android Studio and wait for the app to build successfully

* Then place your `google-services.json` file in `todo/app` folder. If you don't have one, follow the configuration steps

* Now todo app is ready to use
> Mind that if you follow these installation steps, the app will run offline (the app data is stored in the local SQLite Android database on your device or emulator). If you want a fully working app with a website, sync with a remote database, login/registration functions, etc., use my [php-yii2-todo](https://github.com/ArtemBurakov/php-yii2-todo) project.


## Configuration

### 1. php-yii2-todo installation

To make this application fully functional, I created a [php-yii2-todo](https://github.com/ArtemBurakov/php-yii2-todo) project. How to setup php-yii2-todo can be found [here](https://github.com/ArtemBurakov/php-yii2-todo).

Once php-yii2-todo installed, in todo application change the http requests with your server url. Or if you are running php-yii2-todo server on your local network, you can use `localhost`, the IP address of your machine on your local network or IP address of emulator `10.0.2.2`.

> The IP address of your machine can be found using the `ifconfig` command.

### 2. Setting up the development environment

Open [this guide](https://developer.android.com/studio/intro) to install the build tools and all the necessary packages to start Android development and run a todo app on your device or emulator.

### 3. Firebase implementation

To start with Firebase visit [this page](https://cloud.google.com/firestore/docs/client/get-firebase) and follow provided steps there.

* After creating your Firebase app, you need to download the `google-services.json` file.
* Place the downloaded file into the `todo/app` folder.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
