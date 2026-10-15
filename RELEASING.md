# Releasing

## Branches and versions

| Branch       | Extension | Quarkus  |
|--------------|-----------|----------|
| `main`       | 4.x       | 4.x      |
| `3.40.x`     | 3.4.x     | 3.40 LTS |
| `3.33.x`     | 3.3.x     | 3.33 LTS |

* `main` is the development line. Each `X.Y.x` branch is an LTS line for one Quarkus stream. Branches of dead lines get an `-eol` suffix.
* The extension minor version is tied to the Quarkus stream, so LTS branches only get patch releases, also when backporting a feature.
* All changes go through PRs, backports are separate PRs against the LTS branch.

## Changelog

`CHANGELOG.md` on `main` is the changelog for all release lines. LTS branches only contain a pointer to it.

Feature, fix and backport PRs do not touch the changelog, since the version they ship in is not known yet. The entry is written when the release is prepared:

* **Release from `main`**: add the `## <version>` entry to `CHANGELOG.md` in the release PR itself.
* **Release from an LTS branch**: first merge a PR on `main` adding the `## <version>` entry, then open the release PR on the LTS branch.

For a backported change, mention where it comes from, e.g. `Backported from 4.0.0: ...`.

## Release PR

A release is triggered by merging a PR that changes `.github/project.yml` (`current-version` and `next-version`). On every PR the `Changelog entry` check runs. For PRs changing `.github/project.yml` it:

* fails when `CHANGELOG.md` has no `## <current-version>` heading, read from the PR itself for `main` and from `main` for LTS branches;
* lists the PRs merged into the branch since the previous tag labeled `enhancement`, `bug` or `breaking-change` in the job summary, to help writing the entry.

To touch `.github/project.yml` without releasing, put `[skip release]` in the PR description.
