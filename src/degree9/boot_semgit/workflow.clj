(ns degree9.boot-semgit.workflow
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [boot.core :as boot]
            [boot.util :as util]
            [degree9.boot-semver :as semver]
            [degree9.boot-semgit :as semgit]))

;; Semgit Workflow Helper Fn's ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def orig-err (atom nil))
(def orig-out (atom nil))

(boot/deftask silence
  "Silence future task output."
  []
  (fn [next-handler]
    (fn [fileset]
      (let [out *out*
            err *err*]
        (reset! orig-out out)
        (reset! orig-err err)
        (binding [*out* (new java.io.StringWriter)
                  *err* (new java.io.StringWriter)]
          (next-handler fileset))))))

(boot/deftask unsilence
  "Unsilence future task output."
  []
  (fn [next-handler]
    (fn [fileset]
      (if (and @orig-out @orig-err)
        (binding [*out* @orig-out
                  *err* @orig-err]
          (next-handler fileset))
        (next-handler fileset)))))

(defmacro with-quiet [task]
  `(if semgit/*debug* ~task (comp (silence) ~task (unsilence))))

;; Semgit Workflow Tasks ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(boot/deftask feature
  "Manage project feature branches."
  [n name       NAME   str  "Feature name which will be appended to 'feature-'."
   c close             bool "Closes a feature branch using 'git-rebase' and 'git-merge'."
   b branch     BRANCH str  "The base or target branch for this feature."
   r remote     REMOTE str  "Remote repository to use as a base for this feature."
   d delete            bool "Delete/Remove a feature without closing it."]
  (assert (:name *opts*) "Feature 'name' was not provided.")
  (assert (if (:close *opts*) (:branch *opts*) true) "Target 'branch' was not provided.")
  (let [bname    (:name *opts*)
        fname    (str "feature-" bname)
        target   (:branch *opts* "master")
        remote   (:remote *opts* "origin")
        close?   (:close *opts*)
        remove?  (:delete *opts*)
        open?    (not (or close? remove?))
        closemsg (str "[close feature] " bname)
        openmsg  (str "[open feature] " bname " from " target)
        mergemsg (str "[merge feature] " bname " into " target)]
    (cond
      open?   (comp
                (boot/with-pass-thru fs
                  (util/info (str "Creating feature branch: " fname " \n")))
                (with-quiet
                  (semgit/git-checkout :branch true :name fname :start target))
                (boot/with-pass-thru fs
                  (util/info (str "Updating version... \n")))
                (with-quiet
                  (semver/version :pre-release 'degree9.boot-semgit/get-feature))
                (boot/with-pass-thru fs
                  (util/info (str "Saving changes... \n")))
                (with-quiet
                  (semgit/git-commit :all true :message openmsg)))
      close?  (comp
                (boot/with-pass-thru fs
                  (util/info (str "Closing feature branch: " fname " \n"))
                  (util/info (str "Fetching latest changes from: " remote " \n")))
                (with-quiet
                  (semgit/git-fetch :remote remote))
                (boot/with-pass-thru fs
                  (util/info (str "Cleaning branch history... \n")))
                (with-quiet
                  (semgit/git-rebase :start target :checkout fname))
                (boot/with-pass-thru fs
                  (util/info (str "Syncing version... \n")))
                (with-quiet
                  (semgit/git-checkout :name target :start "version.properties"))
                (boot/with-pass-thru fs
                  (util/info (str "Saving changes... \n")))
                (with-quiet
                  (semgit/git-commit :all true :message closemsg))
                (boot/with-pass-thru fs
                  (util/info (str "Switching to target: " target " \n")))
                (with-quiet
                  (semgit/git-checkout :name target))
                (boot/with-pass-thru fs
                  (util/info (str "Merging feature: " fname "  \n")))
                (with-quiet
                  (semgit/git-merge :branch [fname] :message mergemsg)))
      remove? (comp
                (boot/with-pass-thru fs
                  (util/info (str "Switching to target: " target "  \n")))
                (with-quiet
                  (semgit/git-checkout :name target :force true))
                (boot/with-pass-thru fs
                  (util/info (str "Removing feature: " fname "  \n")))
                (with-quiet
                  (semgit/git-branch :name fname :delete true :force true))))))

(boot/deftask patch
  "Manage project patch branches."
  [n name       NAME   str  "Patch issue id (github issue, etc.) which will be appended to 'patch-'."
   c close             bool "Closes a patch branch using 'git-rebase' and 'git-merge'."
   b branch     BRANCH str  "The base or target branch for this patch."
   d delete            bool "Delete/Remove a patch without closing it."]
  (let [bname    (:name *opts*)
        fname    (str "patch-" bname)
        target   (:branch *opts* "master")
        close?   (:close *opts*)
        remove?  (:delete *opts*)
        open?    (not (or close? remove?))
        closemsg (str "[close patch] " bname)
        openmsg  (str "[open patch] " bname " from " target)
        mergemsg (str "[merge patch] " bname " into " target)]
    (boot/with-pass-thru fs
      ;;branch
      ;;version
      ;;commit
      ;;close
      ;;remove
      )))

(def hotfix patch)
