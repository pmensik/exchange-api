# EWS Clojure API

Clojure API for easy access to Microsoft Exchange services.

## Configuration

API can read configuration (such as credentials) from environment variables. Supported configration options are

 - `EXCHANGE_USER` - username for login
 - `EXCHANGE_PASS` - password for login
 - `EXCHANGE_URL` - URL to authenticate to

## Documentation

Documentation is available under [Github](https://temify.github.io/ews-clojure-api/) pages.

## Logging in

You can connect either via provided URL or with Exchange autodiscover feature. Credentials will be read from environment
if you don't provide any parameters. See corresponding functions in `exchange.ews.authentication` namespace.

## Examples

### Impersonate user

You can impersonate user who will be used for all subsequent calls to the API with
`(exchange.ews.authentication/impersonate-user "user@domain.com")`

### Send email

`(exchange.ews.emails/send-email "to@email.com" "Subject" "Body as a string")`

### Folders

Folder manipulation

#### Get folder

Get folder by name `(exchange.ews.folders/get-folder WellKnownFolderName/Inbox)`

#### New folder

`(exchange.ews.folders/create-folder "New folder" "parent-folder-id-in-string")`

### Items

You can set item importance and add or remove categories.

`(exchange.ews.items/set-item-importance "string-id" :high)`

### Searching

There are various options for searching but most importantly you create your own collections of filters and feed them to
the API.

```
(let [filter1 (create-search-filter :contains-substring EmailMessageSchema/From "george")
      filter2 (create-search-filter :is-greater-than ItemSchema/DateTimeSent (java.util.Date.))
      collection (create-filter-collection :or [filter1 filter2])]
  (-> (get-items-with-filter collection) ; optionally specify parent folder or limit
      (transform-search-result))) ; Transforms result from Java objects to Clojure map
```
