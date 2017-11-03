# MONITor
## Android app to monitor [Monit](https://mmonit.com/monit/).

---
## KJ PC | [kjpc.tech](https://kjpc.tech/) | [kyle@kjpc.tech](mailto:kyle@kjpc.tech)
---

## Warning
MONITor is in early development and doesn't work very well.. :(

## Motivation
How do you monitor [Monit](https://mmonit.com/monit/) and make sure it is running? You need some device that is always running to periodically check on it. The only device I have that is always on (besides the server where Monit is) is my Android phone. Hence the Android app to monitor Monit.

## What MONITor Does
MONITor's primary purpose is to periodically check on Monit connections and make sure they are running. It assumes that if they are running, Monit does the rest of the work of notifying for errors, etc. Therefore, MONITor only notifys you if it can't connect to a Monit connection. MONITor also provides a [WebView](https://developer.android.com/reference/android/webkit/WebView.html) displaying the Monit interface for each connection.

### License
[MIT License](LICENSE)
