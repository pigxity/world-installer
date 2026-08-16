# World Install Button
Download on [Modrinth](https://modrinth.com/mod/worldinstaller)

A client-side Fabric mod that adds a button to the Select World screen to instantly extract and install worlds from your downloads folder. 

It scans your Downloads directory for valid zip files of Minecraft worlds (that contain a level.dat file) and allows you to select which one to install.

Also lets you export a world in one click from the Edit World screen, outputing a zip directly to your downloads folder.

## Config

The directory path is configurable. Edit `config/worldinstaller.json`. 

`~` here represents the user home and works on both Windows and Linux.

Default config:
```json
"installDirectory": "~/Downloads",
"exportDirectory": "~/Downloads"
``` 