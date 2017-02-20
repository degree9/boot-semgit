(set-env!
 :dependencies  '[[org.clojure/clojure     "1.8.0"]
                  [boot/core               "2.7.1"]
                  [adzerk/bootlaces        "0.1.13" :scope "test"]
                  [clj-time                "0.13.0"]
                  [boot/new                "0.5.1"]
                  [degree9/boot-exec       "0.4.0"]
                  [degree9/boot-semver     "1.4.1"]]
 :resource-paths   #{"src"})

(require
 '[boot.util :as util]
 '[adzerk.bootlaces :refer :all]
 '[degree9.boot-semver :refer :all]
 '[degree9.boot-semgit :refer :all]
 '[degree9.boot-semgit.workflow :refer :all])

(task-options!
 pom {:project 'degree9/boot-semgit
      :version (get-version)
      :description "Semantic Git usage for boot projects."
      :url         "https://github.com/degree9/boot-semgit"
      :scm {:url "https://github.com/degree9/boot-semgit"}}
 target {:dir #{"target"}})

(deftask develop
  "Build boot-semgit for development."
  []
  (comp
   (watch)
   (version :develop true
            :minor 'inc
            :patch 'zero
            :pre-release 'snapshot
            :generate 'degree9.boot-semgit.version)
   (target)
   (build-jar)))

(deftask deploy
  "Build boot-semgit and deploy to clojars."
  []
  (comp
   (version)
   (target)
   (build-jar)
   (push-release)))

(deftask testing
  "Test mute/unmute of task output."
  []
  (comp
    (with-quiet
      (with-pass-thru fs
        (util/info "Can't see me!")))
    (with-pass-thru fs
      (util/info "You can see me!"))))
