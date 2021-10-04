#!/bin/sh

set -euo pipefail

mkdir -p MARS.app/Contents/MacOS
echo "
#import <Foundation/Foundation.h>

int main() {
	const char *path = strdup([[[[NSBundle mainBundle] resourcePath] stringByAppendingString:@\"/Java/Mars.jar\"] UTF8String]);
	execl(\"/usr/bin/java\", \"java\", \"-Xdock:name=MARS\", \"-jar\", path, NULL);
}
" | clang -x objective-c -framework Foundation -o Mars.app/Contents/MacOS/MARS -
./CreateMarsJar.sh
mkdir -p MARS.app/Contents/Resources/Java
cp Mars.jar MARS.app/Contents/Resources/Java
echo "APPL????" > MARS.app/Contents/PkgInfo
generate-app-icons images/mars.png -m -f MARS.iconset
iconutil -c icns MARS.iconset
cp MARS.icns MARS.app/Contents/Resources
SetFile -a B MARS.app
echo "
<?xml version=\"1.0\" ?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
  <dict>
    <key>CFBundleDevelopmentRegion</key>
    <string>English</string>
    <key>CFBundleExecutable</key>
    <string>MARS</string>
    <key>CFBundleIconFile</key>
    <string>MARS.icns</string>
    <key>CFBundleIdentifier</key>
    <string>com.saagarjha.MARS</string>
    <key>CFBundleDisplayName</key>
    <string>MARS</string>
    <key>CFBundleInfoDictionaryVersion</key>
    <string>6.0</string>
    <key>CFBundleName</key>
    <string>MARS</string>
    <key>CFBundlePackageType</key>
    <string>APPL</string>
    <key>CFBundleShortVersionString</key>
    <string>4.5</string>
    <key>CFBundleVersion</key>
    <string>45</string>
    <key>CFBundleSignature</key>
    <string>????</string>
    <key>NSHumanReadableCopyright</key>
    <string>© 2003-2013, Pete Sanderson and Kenneth Vollmar</string>
    <key>NSHighResolutionCapable</key>
    <true/>
    <key>Java</key>
    <dict>
    	<key>MainClass</key>
    	<string>Mars</string>
    </dict>
  </dict>
</plist>" > MARS.app/Contents/Info.plist

