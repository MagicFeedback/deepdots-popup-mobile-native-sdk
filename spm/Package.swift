// swift-tools-version: 5.7
import PackageDescription

let package = Package(
    name: "DeepdotsSDK",
    platforms: [
        .iOS(.v13)
    ],
    products: [
        .library(name: "ComposeApp", targets: ["ComposeApp"]) // módulo consumido desde Swift
    ],
    targets: [
        .binaryTarget(
            name: "ComposeApp",
            url: "https://github.com/MagicFeedback/DeepdotsSDK-SPM/releases/download/0.1.7/DeepdotsSDK-0.1.7.xcframework.zip",
            checksum: "5aac5c5253e92c76434e35d911f6c7d3ea416d2e33dabda1cdaf411c9915a860"
        )
    ]
)
