# EWS Clojure API [![CircleCI](https://circleci.com/gh/pmensik/exchange-api/tree/master.svg?style=svg)](https://circleci.com/gh/pmensik/exchange-api/tree/master)

Clojure API for easy access to Microsoft Exchange services based on on [EWS Java API](https://github.com/OfficeDev/ews-java-api). This API also supports streaming notifications provided by EWS API in order to receive Exchange events.

## Configuration

API can read configuration (such as credentials) from environment variables. Supported configration options are

 - `EXCHANGE_USER` - username for login
 - `EXCHANGE_PASS` - password for login
 - `EXCHANGE_URL` - URL to authenticate to
 - `EXCHANGE_VERSION` - version of Exchange to which API is connecting. Defaults to 2010-SP2 (newest one in EWS)

## Documentation

Documentation is available under [Github](https://temify.github.io/ews-clojure-api/) pages.

## Logging in

You can connect either via provided URL or with Exchange autodiscover feature. Credentials will be read from environment
if you don't provide any parameters. See corresponding functions in `exchange.ews.authentication` namespace. Exchange
service object is than stored in an atom and used for all subsequent API calls.

## Examples

### Impersonate user

You can impersonate user who will be used for all subsequent calls to the API with
`(exchange.ews.authentication/impersonate-user "user@domain.com")`

### Send email

`(exchange.ews.emails/send-email "to@email.com" "Subject" "Body as a string")`

### Folders

Folders can be found via their known name (value from enum `WellKnownFolderName`) or id provided as a string. Same
applies for creating new folder where parent folder can also be specified as a known name or id.

#### Get inbox

`(exchange.ews.folders/get-inbox)`

#### Get folder

Get folder by name `(exchange.ews.folders/get-folder WellKnownFolderName/Inbox)`

#### New folder

`(exchange.ews.folders/create-folder "New folder" "parent-folder-id-in-string")`

### Items

You can set item importance and add or remove categories.

```
(in-ins 'exchange.ews.items)

(set-item-importance "string-id" :high)

(add-category "string-id" "category1")

(add-categories "string-id" ["cat1" "cat2"])

(remove-category "string-id" "category1")
```

### Searching

There are various options for searching but most importantly you create your own collections of filters and feed them to
the API. Fields available for filtering are specified in various enums in package`microsoft.exchange.webservices.data.core.service.schema`.
Some fields will accept only enum values for filtering, such as `importance`.

```
(in-ins 'exchange.ews.search)

(let [filter1 (create-search-filter :contains-substring EmailMessageSchema/From "george")
      filter2 (create-search-filter :is-greater-than ItemSchema/DateTimeSent (java.util.Date.))
      collection (create-filter-collection :or [filter1 filter2])]
  (-> (get-items-with-filter collection) ; optionally specify parent folder or limit
      (transform-search-result))) ; Transforms result from Java objects to Clojure map
```

### Calendar

You can both search and create appointments via Exchange API.

```
(in-ins 'exchange.ews.calendar)

(create-appointment "Meeting" (java.util.Date.) (java.util.Date.) :location "London" :body
"Invitation")
```

Searching defaults to beginning and end of the current month (but you can of course specify both parameters).

```
(in-ins 'exchange.ews.calendar)

(transform-appointments (get-all-appointments))
```


### Streaming notifications

There are two functions to start notification listener, one listen to changes on all folders, the other takes list of
folders on which will listen to events.

#### Example
First you need to implement your notification handler function which will receive an event. You can also implememnt disconnect function (default one just connects back to the API). Also check `on-notification-event` and `on-disconnect` functions defined in namespace.
```
(def my-notification-event
  (reify StreamingSubscriptionConnection$INotificationEventDelegate ; you have implement the EWS interface
    (notificationEventDelegate [_ sender event-args]
      (run! (fn [event]
              (log/info "Event type" (.name (.getEventType event))) ; Check for EventType (enum)
              (log/info "Item id" (.getUniqueId (.getItemId event))) ; Check for Exchange ItemId which have changed
              ) (.getEvents event-args)))))
```
Then call function with following args
```
(let [events #{:created :deleted} ; Events to listen to
      folders [(FolderId. (WellKnownFolderName/Calendar))]] ; Vector of folders
  (create-streaming-notification events folders my-notification-event))
```
