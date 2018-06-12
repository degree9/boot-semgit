# boot-semgit
[![Clojars Project](https://img.shields.io/clojars/v/degree9/boot-semgit.svg)](https://clojars.org/degree9/boot-semgit)
[![Dependencies Status](https://jarkeeper.com/degree9/boot-semgit/status.svg)](https://jarkeeper.com/degree9/boot-semgit)
[![Downloads](https://jarkeeper.com/degree9/boot-semgit/downloads.svg)](https://jarkeeper.com/degree9/boot-semgit)
<!--- [![CircleCI](https://circleci.com/gh/degree9/boot-semgit.svg?style=svg)](https://circleci.com/gh/degree9/boot-semgit)
[![gitcheese.com](https://api.gitcheese.com/v1/projects/83cde58b-907d-4cd9-ba61-405b78f7b8f4/badges?type=1&size=xs)](https://www.gitcheese.com/app/#/projects/83cde58b-907d-4cd9-ba61-405b78f7b8f4/pledges/create) --->

Semantic Git access from [boot-clj][1].

---

<p align="center">
  <a href="https://degree9.io" align="center">
    <img width="135" src="http://degree9.io/images/degree9.png">
  </a>
  <br>
  <b>boot-semgit is developed and maintained by Degree9</b>
</p>

---

* Provides Git Porcelain tasks (wrappers around git binary)  
  `git-add, git-branch, git-commit, etc.`  
  See [which tasks are provided][2].

> The following outlines basic usage of the task, extensive testing has not been done.
> Please submit issues and pull requests!

## Usage

Add `boot-semgit` to your `build.boot` dependencies and `require` the namespace:

```clj
(set-env! :dependencies '[[degree9/boot-semgit "X.Y.Z" :scope "test"]])
(require '[degree9.boot-semgit :refer :all])
```

Semgit tasks look similar to regular git commands. Tasks follow a common name pattern of `git-<command>`:

```bash
boot git-commit --all --message "Commit from boot-clj."
```

Using the `git` binary for one off tasks still makes more sense and results in less typing:

```bash
git commit -a -m "Commit from git."
```

## Rational

#### Why use Semgit tasks?

Boot tasks provide access to your build pipeline, integrating directly with the pipeline allows git commands to be executed as part of your build workflow or from the REPL, again resulting in less typing and becoming a repeatable process which many developers across an entire project can use.

#### Improve your development process:

Fetch remote changes when you start your development:

```clojure
(boot/deftask develop
  "Build project for development."
  [...]
  (let [...]
    (comp
      (git-pull :branch "origin/master")
      (watch)
      ...
      (serve))))
```

#### Why not just call it `boot-git`?

This task provides porcelain git functions and is designed to build workflows (getting to that in a moment).
Meaning internal or plumbing tasks are not provided, therefore we wrap the systems `git` binary and only attempt to provide a subset of functionality. `boot-git` should probably be reserved for a set of tasks which implement `git` using a native library such as `JGit`. We will leave that up to you!

## Workflows

#### Where does the "semantic" part come in?

`boot-semgit` provides a set of tasks which integrate with `boot-semver` to provide consistent
branching and versioning of projects. Our goal is to provide accelerated development with common community driven process.

Open a feature branch from `master`:
```clojure
;; require the workflow namespace
(require '[degree9.boot-semgit.workflow :refer :all])
```
```bash
$ boot feature --name test

Creating feature branch: feature-test
Updating version...
Saving changes...
```

Close a feature branch (merging changes) into `master`:
```bash
$ boot feature --name test -c -b master

Closing feature branch: feature-test
Cleaning branch history...
Syncing version...
Saving changes...
Switching to target: master
Merging feature: feature-test  
```

#### Task Options

The `feature` task exposes a few options.

```clojure
n name       NAME   str  "Feature name which will be appended to 'feature-'."
c close             bool "Closes a feature branch using 'git-rebase' and 'git-merge'."
b branch     BRANCH str  "The base or target branch for this feature."
r remote     REMOTE str  "Remote repository to use as a base for this feature."
d delete            bool "Delete/Remove a feature without closing it."
```

[1]: https://github.com/boot-clj/boot
[2]: https://github.com/degree9/boot-semgit/wiki/Porcelain-Tasks
