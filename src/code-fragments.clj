(defn counter []  
  (let [tick (atom 0)]
    #(swap! tick inc)))

;;(def tick (counter))  

(defn addpnum2
  [pdgmvec]
  (let [tick (counter)]
      (for [pdgmrow pdgmvec]
        (let [pnum (tick)]
          (str " \r\nP-"  (tick) "-")))))
       ;;(clojure.string/replace pdgmrow #"\r\n(\S)" (str "\r\nP-"  (swap! pnum inc) "-$1")))))

(defn addpnum3
  [pdgmvec]
  (let [counter (atom 0)
        inc-counter (swap! counter inc)]
      (for [pdgmrow pdgmvec]
        (let [pnum (@counter)]
          (str " \r\nP-"  "-")))))

(defn addpnum4
    [pdgmvec pnum]
    (let [newnum (swap! pnum inc)
          pdgm (count pdgmvec)]
      (for [pdgmrow pdgmvec]
          (str " \r\nP-"  pdgm "-"))))

(defn addpnum
    [pdgmvec]
    (let [pnum (atom 0)]
      (for [pdgmrow pdgmvec]
        (let [pnum (swap! pnum inc)]
          (str " \r\nP-"  pnum "-")))))
      
        ;;(clojure.string/replace pdgmrow #"\r\n(\S)" (str "\r\nP-"  (swap! pnum inc) "-$1")))))

