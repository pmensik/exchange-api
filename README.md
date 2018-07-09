# EWS Clojure API

Clojure API for easy access to Microsoft Exchange services.

## Configuration

API can read configration variables (such as credentials) from environment variables. Supported configration options are

 - `EXCHANGE_USER` - username for login
 - `EXCHANGE_PASS` - password for login
 - `EXCHANGE_URL` - URL to authenticate to

## Documentation

Documentation is available under [Github](https://temify.github.io/ews-clojure-api/) pages.

## Examples

### Impersonate user

You can impersonate user who will be used for all subsequent calls to the API with
`(exchange.ews.authentication/impersonate-user "user@domain.com")`
