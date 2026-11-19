# Ethereal Tower Defense

## Controls (Temporary)
- `1`: select Cannon placement
- `2`: select Mortar placement
- `8`: select Caesar Shield placement
- `3`–`7`: debug-spawn enemies
- Left click: confirm placement
- Right click or `Esc`: cancel placement
- `Space`: pause / resume simulation

## Design patterns
- Factory: `TowerFactory`, `EnemyFactory`
- Object Pool: `ProjectilePool`
- Strategy: `AttackStrategy`, `CannonStrategy`, `MortarStrategy`, `CaesarShieldStrategy`
- Observer: `GameEvents`, `GameObserver`, `GameEvent`
- Command: `BuyTowerCommand`, `PlaceTowerCommand`, `SpawnEnemyCommand`
- State: `GameState`, `PlayingState`, `PlacementState`, `PausedState`, `GameOverState`

## Splash indicators
`SplashIndicatorManager` owns the shared stationary, one-second fading circle effect. Mortar impacts and Caesar Shield attacks both use it; future splash towers can spawn indicators through the same manager.
