// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDK",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "DeepdotsSDK", targets: ["DeepdotsSDK"]) // módulo consumido desde Swift
    ],
    targets: [
        // RENAME (ComposeApp -> DeepdotsSDK): the url/checksum below still point at the
        // 0.2.2 release whose archive contains the OLD `ComposeApp.xcframework`. They MUST be
        // regenerated for the next release: build the framework with the new baseName, produce
        // `DeepdotsSDK-<version>.xcframework.zip`, upload it, then update the url and checksum
        // here. Until then this published manifest will not resolve against the new name.
        .binaryTarget(
            name: "DeepdotsSDK",
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.2.2/DeepdotsSDK-0.2.2.xcframework.zip",
            checksum: "cebee8ae4921d00b8dbec8fe21eba577cdbe3a481bfaf54c678d8327f1776db5"
        )
    ]
)
