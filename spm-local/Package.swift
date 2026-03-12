// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDKLocal",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "ComposeApp", targets: ["ComposeApp"])
    ],
    targets: [
        .binaryTarget(
            name: "ComposeApp",
            path: "../dist/spm-local/ComposeApp.xcframework"
        )
    ]
)
