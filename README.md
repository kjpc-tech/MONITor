# MONITor
## Android<sup>TM</sup> app to monitor [Monit](https://mmonit.com/monit/).

---
## KJ PC | [kjpc.tech](https://kjpc.tech/) | [kyle@kjpc.tech](mailto:kyle@kjpc.tech)
---

## Play Store
[MONITor is available in the Play Store](https://play.google.com/store/apps/details?id=tech.kjpc.monitorapp).

## Motivation
How do you monitor [Monit](https://mmonit.com/monit/) and make sure it is running? You need some device that is always running to periodically check on it. Maybe the only device you have that is always on (besides the device running Monit) is an Android phone. This is where MONITor comes in handy.

## What MONITor Does
MONITor's primary purpose is to periodically check on Monit connections and make sure they are running. It assumes that if they are running, Monit does the rest of the work of notifying for errors, etc. Therefore, MONITor only notifys you if it can't connect to a Monit connection. MONITor also provides a [WebView](https://developer.android.com/reference/android/webkit/WebView.html) displaying the Monit interface for each connection.

### License
[MIT License](LICENSE)
  
  
<sup><sub>Android, Google Play and the Google Play logo are trademarks of Google Inc.</sub></sup>
