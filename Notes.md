# How to build ASTM Light Extension

* Execute `ant -f mirth-build`
* Then zip `build/extensions/astmlight` and upload it via mirth client

# Configuration in Database

* Each channel gets a new ID which can be found in the `channel` table.
* There is also the configuration stored as XML in the `channel` column.
* Other information such es logged messages are stored in tables such as `d_mmX` where `X` is the channel ID

# Misc
* The protocol type is set in property files and needed for lookup.