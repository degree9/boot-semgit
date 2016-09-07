(set-env!
 :dependencies  '[[org.clojure/clojure     "1.7.0"]
                  [boot/core               "2.6.0"]
                  [adzerk/bootlaces        "0.1.13" :scope "test"]
                  [clj-time                "0.11.0"]
                  [seancorfield/boot-new   "0.4.6"]
                  [degree9/boot-exec       "0.4.0"]
                  [degree9/boot-semver     "1.3.6"]]
 :resource-paths   #{"src"})

(require
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
   (version :no-update true
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
