// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDKLocal",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "DeepdotsSDK", targets: ["DeepdotsSDK"])
    ],
    targets: [
        .binaryTarget(
            name: "DeepdotsSDK",
            path: "../dist/spm-local/DeepdotsSDK.xcframework"
        )
    ]
)
