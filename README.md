# LavaFishing

A Minecraft Paper plugin that allows players to fish in lava.

## Permissions

- `lavafishing.fishing`

## Dependencies

- [mcMMO](https://github.com/mcMMO-dev/mcmmo) (optional) - Lava fishing gives mcMMO experience towards the fishing skill.
- [Jobs Reborn](https://github.com/Zrips/Jobs) (optional) - Lava fishing gives Jobs rewards (money, experience, and points) to any jobs that pay for the fishing action. Please ensure that config for the job you want this plugin to benefit is updated to include all the entries from the loot table defined in the config.yml of this plugin.

## Installation

1. Download the latest JAR from [Releases](https://github.com/SulkyWhale/LavaFishing/releases) and place it in your plugins folder.
2. Restart your server.
3. Modify the newly generated config.yml, found in the plugin's data directory, to your liking.
4. Restart your server again for the changes to take effect.

## Building

If you wish to build the plugin yourself, follow the instructions below.

1. Clone the repository:
    ```shell
    git clone https://github.com/SulkyWhale/LavaFishing.git
    ```
2. Change into the project directory:
    ```shell
    cd LavaFishing
    ```
3. Build the JAR with Maven:
    ```shell
    mvn clean package
    ```

## Issues and Suggestions

If you encounter any bugs, please open an [issue](https://github.com/SulkyWhale/LavaFishing/issues/new). Be sure to check existing issues first to avoid duplicates.

If you have a feature request, please start a [discussion](https://github.com/SulkyWhale/LavaFishing/discussions/new?category=ideas) to share your idea!

## Contributing

Contributions are welcome. If you have any bug fixes, improvements, or new features you would like to add to this project, feel free to open a [pull request](https://github.com/SulkyWhale/LavaFishing/pulls). For new features, it is recommended to start a [discussion](https://github.com/SulkyWhale/LavaFishing/discussions/new?category=ideas) first to discuss it, so that it can be evaluated before any time is invested into it. 

## License

LavaFishing is licensed under the GNU GPL-3.0-only. Please see the [license](/LICENSE.md) for more information.