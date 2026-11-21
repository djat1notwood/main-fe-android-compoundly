---
trigger: always_on
---

# Git Workflow & Phase Auto-Commit Rules

1. **Auto-Commit Every Phase:** After completing all tasks and verification checks for a planned phase, automatically create a local `git commit` before moving on to the next phase.
2. **Conventional Commits Standard:** Write commit messages following **Conventional Commits v1.0.0**:
   - Format: `<type>(<scope>): <short imperative summary>`
   - Allowed `<type>`: `feat`, `fix`, `chore`, `refactor`, `test`, `build`, `docs`, `style`
   - Keep the subject line lowercase, imperative, English, concise (<= 72 chars), and without a trailing period.
3. **Pre-Commit Safety Checks:**
   - Run `git status` and confirm no secret or local-only files (`keystore.properties`, `*.jks`, `*.keystore`, `local.properties`) are staged.
   - Ensure targeted build/test checks for the phase pass prior to committing.
   - Commit locally only; do **not** run `git push` unless explicitly requested by the user.
