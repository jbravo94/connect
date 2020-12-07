# Create Connector Extension

* Create Donkey classes via `ant -buildfile mirth-build.xml build-donkey`
* Add `lib/donkey/donkey-server.jar` and `lib/donkey/donkey-model.jar` as libraries to IntelliJ if needed
* Run `ant -Dversion=3.9.0 create-connectors`
* Run `ant -Dversion=3.9.0 create-extension-zips`
* Install `dist/extension/astmlight-3.9.0.zip` via mirth client (Installed plugins can be found in `<INSTALL-DIR>/extensions`)
* Restart mirth server `<INSTALL-DIR>/mcservice restart`