# Release 2.7.0+1.21.1
- Added `medsystem:armor_protection_area` data component to allow setting of armor protection areas for specific armor items
- Fixed totem of undying and other modded death protection items not correctly preventing death
  - Now recovers all limbs, removes pain and removes all negative effects such as bleeds
- Configuration changes
  - Moved all armor-related configs to `Armor` section - this will rewrite your armor configs!
  - Added `Modular armor multiplier` to allow custom armor value multipliers for MODULAR_BOOSTED armor system
  - Added a config option to configure protection areas of each limb. So, chest armor can protect arms, legs or whatever you need