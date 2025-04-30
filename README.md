[vanilla-discord-bridge]: https://github.com/OptimisticDeving/vanilla-discord-bridge

# decent-enough-vanish

An attempt at a decent vanish command for the Fabric ecosystem, with as
little leakage & complexity as possible.

## Caveats

1. Players in vanish below permission level 3 cannot see or communicate with
   each-other using vanilla
   commands (except for tellraw and perhaps others).
2. Players in vanish cannot communicate in chat or via broadcast commands (i.e.
   teammsg/say). That would probably be problematic on servers with chat signing
   enabled.
3. Fake join and leave messages may not be bridged where the bridge relies on
   mixins to PlayerList & ServerGamePacketListenerImplMixin bridge join/leave
   messages. [vanilla-discord-bridge] will work correctly, though.