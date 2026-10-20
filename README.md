# HidePlugins

Stops players from seeing what plugins and server software you run. Blocks `/pl`, `/ver`, `/help` and whatever else you add, removes them from tab complete, and by default replies with the server's own "unknown command" message so nobody can tell the command exists.

Works on Paper 1.18.2 up to 26.3, and on Folia.

## What it does

- Blocks the commands in your config for anyone without the bypass permission
- Catches every alias and namespaced version too, so blocking `plugins` also blocks `pl`, `bukkit:plugins` and `bukkit:pl`
- Removes them from the command list the client gets, so they don't show up in tab complete
- Filters tab complete server side too, for clients that ask the server directly
- Can hide every `namespace:command` entry from tab complete, since those give away plugin names
- `<unknown_command>` copies the real unknown command message for your server version, down to the packet

## Install

1. Drop the jar in `plugins/`
2. Restart
3. Edit `plugins/HidePlugins/config.yml` and run `/hp reload`

Needs Java 17 or newer, so whatever your server already runs on is fine.

## Config

```yaml
message: "<unknown_command>"

hidden-commands:
  - plugins
  - version
  - help
  - icanhasbukkit

custom-messages: {}

hide-namespaced-commands: true
```

`message` is what players see when they run a hidden command. It's [MiniMessage](https://docs.advntr.dev/minimessage/format.html), and you can use:

- `<unknown_command>` for the server's normal unknown command message
- `<command>` for the command they typed

Set it to `""` to not send anything at all.

`hidden-commands` is the list of commands to hide. You only need the main name, aliases and `bukkit:` versions are handled for you. Run `/hp check <command>` if you want to make sure something is covered.

`custom-messages` lets you give a command its own message. Anything in here is hidden too, so you don't have to put it in both places.

```yaml
custom-messages:
  plugins: "<white>Plugins (0):"
  version: "<gray>This server is running <green>a potato"
  help: "<yellow>Ask a staff member in discord"
```

`hide-namespaced-commands` removes every `something:command` from tab complete. The commands still work if typed, they just don't show up. Players with `hideplugins.bypass` still see everything.

## Commands

| Command | What it does |
| --- | --- |
| `/hp reload` | Reloads the config and resends tab complete to everyone online |
| `/hp check <command>` | Tells you if a command is hidden and which entry hides it |

`/hideplugins` works too.

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `hideplugins.admin` | op | Use `/hp` |
| `hideplugins.bypass` | op | See and use everything |
| `hideplugins.bypass.<command>` | op | Bypass one entry, like `hideplugins.bypass.plugins` |

## Coming from 2.x

Your old config still loads, you'll just get a warning in console. To switch over:

- `blockMsg` is now `message`
- `disabled-commands` is now `hidden-commands`, and you can drop the aliases and `bukkit:` entries
- `customBlockMsg` is now `custom-messages`, keyed by command instead of a list
- `hide_plugins.bypass` is now `hideplugins.bypass`
- `hide_plugins.reload` is now `hideplugins.admin`

## Building

```
./gradlew build
```

The jar ends up in `build/libs`.
