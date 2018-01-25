# IPEX
Library for communication with IPEX call centrum

## Usage in a project

Create a file in `/src/spc/ipex/routes.clj` and paste needed routes for the call.

```
(ns spc.api.ipex.routes
  (:require [compojure.api.sweet :refer :all]
            [spc.config.api-config :refer [no-response-coercion]]
            [ipex.core :as ipex]
            [ring.util.http-response :refer [ok]]
            spc.lib.protocols.restructure))

(defroutes api-routes-ipex
  (context "/ipex" []
    :tags ["Ipex"]

    (GET "/history/:number" []
      :path-params [number :- Long]
      :summary "Returns ipex history"
      (ok (ipex/get-calls-history number)))

    (POST "/call/:number" []
      :path-params [number :- Long]
      :summary "Makes call"
      :current-user user
      (ok (ipex/make-new-call number (:email user))))))
```
