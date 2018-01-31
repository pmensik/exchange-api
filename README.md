# IPEX
Library for communication with IPEX call centrum

## Configuration

You need to export following environment variables in order connect to the IPEX

 - `IPEX_URL` - domain of the switch-board (for example `vh656.ipex.cz`)
 - `IPEX_API_KEY` - API KEY (similar to the AWS one) provided by the IPEX
 - `IPEX_API_SECRET` - API SECRET (similar to the AWS one) provided by the IPEX

## Usage in a project

Create a file in `/src/spc/ipex/routes.clj` and paste needed routes for the call. Then reference these routes in
`src/spc/handler.clj`.

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
