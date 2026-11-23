# rawpotato-opencode-mobile_custom

Наш слой правок поверх неофициального Android-клиента OpenCode.

- Апстрим: `opencode-mobile/opencode-mobile` (автор RawPotato), GPL-3.0. Мы ничего не форкаем
  на GitHub: это обычный приватный репозиторий, апстрим подтягивается remote `upstream`.
- Ветки:
  - `upstream-main` — точное зеркало `upstream/main`;
  - `custom` — наша ветка: апстрим + патч минимальной силы.
- Спеки и планы — не здесь, а в `ai_helper_v2`:
  `docs/specs/2026-11-23-mobile-*.md`, `docs/plans/2026-11-23-mobile-*.md`,
  подход — `docs/specs/2026-11-23-mobile-custom-fork.md`.
- Все наши правки включаются тумблерами в разделе «Свои настройки»; выключено — поведение апстрима.
- Сборка APK — GitHub Actions (workflow `android`, артефакт `app-debug`), локально на VPS не
  собирается: не хватает памяти.

## Обновление апстрима

```sh
git fetch upstream
git checkout upstream-main && git merge --ff-only upstream/main
git checkout custom && git rebase upstream-main
```
