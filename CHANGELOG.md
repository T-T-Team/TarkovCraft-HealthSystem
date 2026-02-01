# Release 1.21.11-2.1.0-beta.1
- Improvements to hit detection
  - disabled hit approximation for players (controlled via entity tag `medsystem:no_limb_hit_approximation`)
  - improvements to melee hit calculation
  - improvements to projectile hit calculation
- Forced player body rotation synchronization between server and clients
  - vanilla had inconsistent rotation between server and clients causing inconsistencies in hit detection
- Added new experimental config options (require you to enable configuration Advanced mode when editing via GUI)
  - Strict player rotation synchronization: Enables player rotation synchronization. Defaults to `true`
  - Projectile hitbox inflation: Inflates entity default bounding box by specified amount for better hit detection. Defaults to `0.2`