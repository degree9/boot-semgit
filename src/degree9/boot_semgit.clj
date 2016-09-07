(ns degree9.boot-semgit
  (:require [clojure.string :as s]
            [boot.core :as boot]
            [boot.util :as util]
            [boot.git :as git]
            [degree9.boot-exec :as exec]
            [degree9.boot-semver :as semver]))
;; Semgit Global Settings ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def ^:dynamic *debug* false)

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
                (:force *opts*) (conj "--force")
                path            (into path))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

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
                 (:delete *opts*) (conj "--delete")
                 (:force *opts*)  (conj "--force")
                 name             (conj name)
                 rename           (conj rename)
                 start            (conj start))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

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
                 (:force *opts*) (conj "--force")
                 branch          (conj "-b")
                 name            (conj name)
                 start           (conj start))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-commit
  "Record changes to the repository."
  [a all         bool "Automatically stage files that have been modified and deleted"
   m message MSG str  "Commit message to use."]
  (let [message (:message *opts*)
        args    (cond-> ["commit"]
                  (:all *opts*) (conj "--all")
                  message       (conj "--message" message))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-fetch
  "Download objects and refs from another repository."
  [r remote REMOTE str "Remote repository to fetch."]
  (let [remote (:remote *opts*)
        args   (cond-> ["fetch"]
                 remote (conj remote))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-merge
  "Join two or more git branches together."
  [b branch  BRANCH  [str] "Branch(es) to merge into current branch."
   m message MESSAGE str   "Optional merge message."]
  (let [branch  (:branch *opts*)
        message (:message *opts*)
        args    (cond-> ["merge"]
                  message (conj "-m " message)
                  branch  (into branch))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-mv
  "Move or rename a git file or directory."
  [s source      SRC str  "Source file to move."
   d destination DES str  "Destination when moving."
   f force           bool "Force renaming or moving of a file even if the target exists."]
  (let [source   (:source *opts*)
        dest     (:destination *opts*)
        args   (cond-> ["mv"]
                 (:force *opts*)  (conj "--force")
                 source           (conj source)
                 dest             (conj dest))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-pull
  "Fetch from and integrate with another repository or a local branch."
  [b branch BRANCH str  "Branch to pull changes from (can be a remote branch ie. origin/master)."
   r rebase        bool "Uses git-rebase instead of git-merge."
   f force         bool "Force fetching even when local branch is not descendant."]
  (let [branch (:branch *opts*)
        rebase (:rebase *opts*)
        args   (cond-> ["pull"]
                 (:force *opts*) (conj "--force")
                 rebase          (conj "--rebase")
                 branch          (conj branch))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-push
  "Update remote refs along with associated objects."
  [r remote REMOTE str   "Remote repository of a push (defaults to 'origin')."
   b branch BRANCH str   "Remote branch to be pushed to."
   f force         bool  "Force fetching even when local branch is not descendant."]
  (let [remote (:remote *opts* "origin")
        branch (:branch *opts*)
        args   (cond-> ["push"]
                 (:force *opts*) (conj "--force")
                 remote          (conj remote)
                 branch          (conj branch))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-rebase
  "Reapply commits on top of another branch."
  [s start    START str "Starting point to reapply current branch commits."
   c checkout CHECK str "Optional branch to checkout before rebase."]
  (let [start (:start *opts* "master")
        check (:checkout *opts*)
        args    (cond-> ["rebase"]
                  start (conj start)
                  check (conj check))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-remote
  "Manage tracked git repositories."
  [n name   NAME str  "Name of remote repository."
   a add         bool "Adds a remote named 'name' for the repository at 'url'."
   u url    URL  str  "Url of remote repository."
   r remove      bool "Remove the remote named 'name'."]
  (let [name   (:name *opts*)
        add    (:add *opts*)
        url    (:url *opts*)
        remove (:remove *opts*)
        args    (cond-> ["remote"]
                  add    (conj "add")
                  remove (conj "remove")
                  name   (conj name)
                  url    (conj url))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-rm
  "Remove files from the working tree and from git index."
  [p path  PATH [str] "Files to remove from index. Fileglobs can be given to add all matching files."
   f force      bool  "Override the up-to-date check."]
  (let [path  (:path *opts*)
        args  (cond-> ["rm"]
                (:force *opts*) (conj "--force")
                path            (into path))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-subtree-add
  "Merge subtrees together and split repository into subtrees."
  [d prefix PREFIX str "Files to remove from index. Fileglobs can be given to add all matching files."
   r remote REMOTE str "Remote repository of subtree-push."
   b branch BRANCH str "Remote branch or reference of subtree-push."]
  (let [path  (:path *opts*)
        args  (cond-> ["subtree" "add"]
                prefix (conj "-P" prefix)
                remote (conj remote)
                branch (conj branch))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-subtree-pull
  "Merge subtrees together and split repository into subtrees."
  [d prefix PREFIX str "Files to remove from index. Fileglobs can be given to add all matching files."
   r remote REMOTE str "Remote repository of subtree-push."
   b branch BRANCH str "Remote branch or reference of subtree-push."]
  (let [path  (:path *opts*)
        args  (cond-> ["subtree" "pull"]
                prefix (conj "-P" prefix)
                remote (conj remote)
                branch (conj branch))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-subtree-push
  "Merge subtrees together and split repository into subtrees."
  [d prefix PREFIX str "Files to remove from index. Fileglobs can be given to add all matching files."
   r remote REMOTE str "Remote repository of subtree-push."
   b branch BRANCH str "Remote branch or reference of subtree-push."]
  (let [path  (:path *opts*)
        args  (cond-> ["subtree" "push"]
                prefix (conj "-P" prefix)
                remote (conj remote)
                branch (conj branch))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))

(boot/deftask git-tag
  "Create, list, delete or verify a tag."
  [n name    NAME str   "The name of the tag to create, delete, or describe."
   m message MSG  str  "Tag message to use."
   d delete       bool  "Delete existing tags with the given names."
   f force        bool  "Replace an existing tag with the given name. (instead of failing)"]
  (let [name    (:name *opts*)
        message (:message *opts*)
        args    (cond-> ["tag"]
                  (:delete *opts*) (conj "--delete")
                  (:force *opts*)  (conj "--force")
                  message          (conj message))]
    (exec/exec :process "git" :arguments args :directory "." :debug *debug*)))
