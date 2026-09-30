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
        .binaryTarget(
            name: "DeepdotsSDK",
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.5.0/DeepdotsSDK-0.5.0.xcframework.zip",
            checksum: "a6090f9f14cada71aa8b6057d858b1cdbe6357121ae1cbfc46e34aced26c694f"
        )
    ]
)
