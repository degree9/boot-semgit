(ns degree9.boot-semgit
  (:require [clojure.string :as s]
            [boot.core :as boot]
            [boot.util :as util]
            [boot.git :as git]
            [degree9.boot-exec :as exec]
            [degree9.boot-semver :as semver]))

;; Semgit Helper Fn's ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn get-feature [& _]
  (second (s/split (git/branch-current) #"-")))

;; Git Porcelain Tasks ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(boot/deftask git-add
  "Add file contents to git index."
  [p path  PATH [str] "Files to add to index. Fileglobs can be given to add all matching files."
   f force      bool  "Allow adding otherwise ignored files."]
  (let [path  (:path *opts*)
        args  (cond-> ["add"]
                (:force *opts*) (conj " --force")
                path            (into path))]
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

(boot/deftask git-branch
  "List, create, or delete branches."
  [n name   NAME  str  "Branch name to be created or deleted."
   m move         bool "Move/rename a branch."
   r rename RNAME str  "Destination branch name when moving/renaming."
   s start  START str  "Starting point for branch, default is 'HEAD'."
   d delete       bool "Delete a branch."
   f force        bool "Forces deleting or renaming of a branch."]
  (let [name   (:name *opts*)
        rename (:rename *opts*)
        start  (:start *opts*)
        args   (cond-> ["branch"]
                 (:delete *opts*) (conj " --delete")
                 (:force *opts*)  (conj " --force")
                 name             (conj name)
                 rename           (conj rename)
                 start            (conj start))]
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

(boot/deftask git-checkout
  "Switch git branches."
  [n name   NAME  str  "Branch name to checkout."
   b branch       bool "Causes a new branch to be created, as if 'git-branch' then 'git-checkout'."
   s start  START str  "Starting point for branch, default is 'HEAD'."
   f force        bool "Forces switching branches, throws away local changes."]
  (let [name   (:name *opts*)
        branch (:branch *opts*)
        start  (:start *opts*)
        args   (cond-> ["checkout"]
                 (:force *opts*) (conj " --force")
                 branch          (conj " -b")
                 name            (conj name)
                 start           (conj start))]
                 (prn args)
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

(boot/deftask git-commit
  "Record changes to the repository."
  [a all         bool "Automatically stage files that have been modified and deleted"
   m message MSG str  "Commit message to use."]
  (let [message (:message *opts*)
        args    (cond-> ["commit"]
                  (:all *opts*) (conj " --all")
                  message       (conj " --message" message))]
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

(boot/deftask git-mv
  "Move or rename a git file or directory."
  [s source      SRC str  "Source file to move."
   d destination DES str  "Destination when moving."
   f force           bool "Force renaming or moving of a file even if the target exists."]
  (let [source   (:source *opts*)
        dest     (:destination *opts*)
        args   (cond-> ["mv"]
                 (:force *opts*)  (conj " --force")
                 source           (conj source)
                 dest             (conj dest))]
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

(boot/deftask git-rm
  "Remove files from the working tree and from git index."
  [p path  PATH [str] "Files to remove from index. Fileglobs can be given to add all matching files."
   f force      bool  "Override the up-to-date check."]
  (let [path  (:path *opts*)
        args  (cond-> ["rm"]
                (:force *opts*) (conj " --force")
                path            (into path))]
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

(boot/deftask git-tag
  "Create, list, delete or verify a tag."
  [n name    NAME str   "The name of the tag to create, delete, or describe."
   m message MSG  str  "Tag message to use."
   d delete       bool  "Delete existing tags with the given names."
   f force        bool  "Replace an existing tag with the given name. (instead of failing)"]
  (let [name    (:name *opts*)
        message (:message *opts*)
        args    (cond-> ["tag"]
                  (:delete *opts*) (conj " --delete")
                  (:force *opts*)  (conj " --force")
                  message          (conj message))]
    (exec/exec :process "git" :arguments args :directory "." :debug true)))

;; Semgit Workflow Tasks ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(boot/deftask feature
  "Manage project feature branches."
  [n name       NAME   str  "Feature name which will be appended to 'feature-'."
   c close             bool "Closes a feature branch using 'mode'."
   b branch     BRANCH str  "The base branch and future target for this feature."
   m mode       MODE   kw   "The mode which 'close' should opperate, default is ':rebase'."
   r remove            bool "Removes a feature without closing it."]
  (assert (:name *opts*) "Feature branch 'name' was not provided.")
  (let [mode (:mode *opts* :rebase)
        bname (str "feature-" (:name *opts*))
        target (:branch *opts* "master")
        pre-release (fn [_] (:name *opts*))]
    (comp
      (git-checkout :branch true :name bname :start target)
      (semver/version :pre-release 'degree9.boot-semgit/get-feature)
      (git-commit :all true :message (str "Open branch: " bname " from " target))
      ;;close
      ;;remove
      )))

(boot/deftask patch
  "Manage project patch/hotfix branches."
  [n name       NAME   str  "Feature name which will be appended to 'patch-'."
   c close             bool "Closes a patch branch using 'mode'."
   b branch     BRANCH str  "The base branch and future target for this patch."
   m mode       MODE   kw   "The mode which 'close' should opperate, default is ':rebase'."
   r remove            bool "Removes a patch without closing it."]
  (let [mode (:mode *opts* :rebase)]
    (boot/with-pass-thru fs
      ;;branch
      ;;version
      ;;commit
      ;;close
      ;;remove
      )))

(def hotfix patch)
