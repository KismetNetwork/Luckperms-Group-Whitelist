# Luckperms Group Whitelist for Velocity

A Velocity plugin that allows you to select which groups are allowed to join what servers. By default, it's no-op unless
you happen to use `example` as the name of one of the servers.

## Config

The configuration is name to a list of groups under the `servers` section.

### Example configuration:

```toml
[servers]
# The name of the server must match velocity.toml
# This allows only admins and moderators to join example.
example = ["admin", "moderator"]
```