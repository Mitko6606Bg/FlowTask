# FlowTask — Agent Engineering & Design Guidelines

This project enforces strict standards for architecture, Jetpack Compose performance, Material 3 design tokens, and Emil Kowalski's animation/motion craft principles.

---

## 1. Core Skills Integration

Always consult and adhere to the installed skills located in `.agents/skills/`:

| Skill | Focus & Authority |
|---|---|
| **`compose-kotlin-agent-skills`** | Kotlin 2.x, strict MVI, atomic `_state.update { }`, `collectAsStateWithLifecycle()`, Compose stability (`@Immutable`/`@Stable`), `key` & `contentType` in lazy lists, Room, Coroutines. |
| **`emil-design-eng`** | Emil Kowalski's design engineering philosophy: frequency framework (high-frequency = snappy/instant; rare = delight), active touch feedback (`scale(0.96f..0.98f)`), origin-anchored transitions, physical springs. |
| **`review-animations`** | 10 non-negotiable motion standards, sub-300ms UI rule, responsive easing (never `ease-in` for entering elements), interruptible springs, Before/After table review format. |
| **`m3-expressive-compose`** | `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`, `MaterialExpressiveTheme`, `motionScheme` spring physics for spatial layout changes, `defaultEffectsSpec()` for fades, dynamic color. |
| **`material-3`** | Material Design 3 surface containers (`surfaceContainerLowest` to `surfaceContainerHighest`) replacing MD2 drop-shadow elevation, 8dp grid system, adaptive window size classes. |
| **`mobile-android-design`** | 48dp minimum touch targets, Navigation Compose, edge-to-edge system bars, accessible TalkBack semantics. |

---

## 2. Mandatory Coding Guardrails (Zero Tolerance for Banned Antipatterns)

1. **State Collection in UI**:
   - ❌ NEVER use `collectAsState()` in Android composables (causes background battery drain).
   - ✅ ALWAYS use `collectAsStateWithLifecycle()`.
2. **ViewModel State Mutation**:
   - ❌ NEVER use `_state.value = newValue` for concurrent state.
   - ✅ ALWAYS use `_state.update { it.copy(...) }` for atomic state transitions.
3. **Lazy Lists**:
   - ❌ NEVER write `items(...)` without a stable `key`.
   - ✅ ALWAYS provide both `key = { ... }` and `contentType = { ... }` to guarantee item recycling without recomposition storms.
4. **Touch & Click Feedback**:
   - ❌ Static cards/buttons without press feedback.
   - ✅ Use Emil's tactile press scaling (`Modifier.graphicsLayer { scaleX = pressScale; scaleY = pressScale }` where pressed = 0.96f-0.98f).
5. **Animation Curves**:
   - ❌ Never use `tween()` or linear interpolation for physical UI elements (cards, sheets, dialogs).
   - ✅ Use physical springs (`spring(stiffness = ..., dampingRatio = ...)` or `MaterialTheme.motionScheme.defaultSpatialSpec()`).
6. **Popovers & Menus**:
   - ❌ Never animate elements from `scale(0)` or triggerless center.
   - ✅ Start scale from `0.92f-0.96f` with `fadeIn`, anchored to the user's tap origin.

---

## 3. Build & Test Verification

After modifying any Kotlin or Compose code in FlowTask, always verify:
```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```
Ensure 0 compilation errors and 100% test pass rate.
