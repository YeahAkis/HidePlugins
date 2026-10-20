# Changelog

## 3.0

- Rewritten from scratch, no more ACF, FoliaLib or Lombok, the jar is tiny now
- Works on Paper 1.18.2 up to 26.3 and on Folia
- Aliases and namespaced commands are picked up automatically, so `plugins` also covers `pl`, `bukkit:plugins` and `bukkit:pl`
- Fixed aliases like `/pl` and `/ver` getting through on newer Paper
- Fixed modified clients being able to tab complete hidden commands from the server
- Blocked commands now reply with the server's real unknown command message by default
- New option to hide every `namespace:command` from tab complete
- New `/hp check <command>` to see if a command is hidden
- Per command bypass permissions, like `hideplugins.bypass.plugins`
- `<command>` and `<unknown_command>` placeholders in messages
- `/hp reload` now updates tab complete for everyone online
- Config and permissions were renamed, old 2.x configs still load, check the README
