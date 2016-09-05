(ns degree9.boot-semgit.workflow
  (:require [clojure.string :as s]
            [clojure.java.io :as io]
            [boot.core :as boot]
            [degree9.boot-semver :as semver]
            [degree9.boot-semgit :as semgit]))

(boot/deftask redirect-output
  "Silence task output."
  []
  (let [*out*' *out*
        *err*' *err*]
    (fn [next-handler]
      (fn [fileset]
        (binding [*out* (new java.io.StringWriter)
                  *err* (new java.io.StringWriter)]
           (next-handler fileset))))))

;; Semgit Workflow Tasks ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(boot/deftask feature
  "Manage project feature branches."
  [n name       NAME   str  "Feature name which will be appended to 'feature-'."
   c close             bool "Closes a feature branch using 'git-rebase' and 'git-merge'."
   b branch     BRANCH str  "The base or target branch for this feature."
   d delete            bool "Delete/Remove a feature without closing it."]
  (assert (:name *opts*) "Feature 'name' was not provided.")
  (assert (if (:close *opts*) (:branch *opts*) true) "Target 'branch' was not provided.")
  (let [mode     (:mode *opts* :rebase)
        bname    (:name *opts*)
        fname    (str "feature-" bname)
        target   (:branch *opts* "master")
        close?   (:close *opts*)
        remove?  (:delete *opts*)
        open?    (not (or close? remove?))
        closemsg (str "[close feature] " bname)
        openmsg  (str "[open feature] " bname " from " target)
        mergemsg (str "[merge feature] " bname " into " target)]
    (cond
      open?   (comp
                (redirect-output)
                (semgit/git-checkout :branch true :name fname :start target)
                ;(redirect-output)
                (semver/version :pre-release 'degree9.boot-semgit/get-feature)
                ;(redirect-output)
                (semgit/git-commit :all true :message openmsg))
      close?  (comp
                (semgit/git-rebase :start target :checkout fname)
                (semgit/git-checkout :name target :start "version.properties")
                (semgit/git-commit :all true :message closemsg)
                (semgit/git-checkout :name target)
                (semgit/git-merge :branch [fname] :message mergemsg))
      remove? (comp
                (semgit/git-checkout :name target :force true)
                (semgit/git-branch :name fname :delete true :force true)))))

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
