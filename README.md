# StoragePlugin

A Minecraft plugin that adds a customizable storage system with a `/storage` command. Players can access their storage, which supports multiple pages, each with a configurable price. All items are securely stored in a specified database, and the plugin's behavior can be fully customized via the `config.yml` file.

## Features
- **Custom Storage Command:** `/storage` opens the player's storage system.
- **Configurable Pages:** 
  - Each storage can have multiple pages.
  - The number of pages and their respective costs can be set in the configuration.
- **Database Integration:** Items are stored in a specified database for safety and persistence.
- **Fully Configurable:** 
  - All settings, including prices, database connection, and more, can be adjusted in `config.yml`.
- **Custom Currency System:** 
  - Uses configurable in-game items as currency for purchasing storage pages.

## Configuration
Edit the `config.yml` file to set up the plugin:
- Configure the number of pages and their prices.
- Set up database connection details (e.g., MySQL).
- Configure the custom currency item (material type and display name).
- Adjust other plugin-specific settings.

Example configuration:
```yaml
max-pages: 5
page-costs:
  2: 10
  3: 20
  4: 30
  5: 40

database:
  host: "localhost"
  port: "3307"
  name: "minecraft_storage"
  user: "root"
  password: ""

currency-item:
  material: "SMALL_AMETHYST_BUD"
  name: "§5§lAndermant"
```

## Installation
1. Place the plugin's JAR file into your server's `plugins` folder.
2. Start or reload the server.
3. Edit the `config.yml` file to suit your needs.
4. Restart the server to apply the changes.

## Usage
- Use `/storage` to open your personal storage.
- Unlock additional pages by paying the configured price using the custom currency item.
- Players need to have the required number of currency items in their inventory to purchase additional storage pages.

## Requirements
- A supported database (e.g., MySQL) for item storage.

## Permissions
- `playerstorage.use` - Allows players to use the `/storage` command

## Support
If you encounter any issues or have feature requests, feel free to open an issue in the repository or contact the developer.

---

Enjoy managing your storage with **StoragePlugin**! 😊
